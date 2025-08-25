package com.capston.matching_app.service;

import com.capston.matching_app.dto.IdealMatchDTO;
import com.capston.matching_app.entity.IdealMatchResult;
import com.capston.matching_app.entity.Region;
import com.capston.matching_app.entity.Subregion;
import com.capston.matching_app.entity.UserProfile;
import com.capston.matching_app.repository.IdealMatchResultRepository;
import com.capston.matching_app.repository.RegionRepository;
import com.capston.matching_app.repository.SubregionRepository;
import com.capston.matching_app.repository.UserProfileRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class IdealMatchingService {

    private final IdealTypeService idealTypeService;
    private final IdealMatchResultRepository matchRepo;
    private final UserProfileRepository userProfileRepository;
    private final RegionRepository regionRepository;
    private final SubregionRepository subregionRepository;
    private final ObjectMapper om = new ObjectMapper();

    @Value("${external.python.base-url}")
    private String pythonBaseUrl;

    private RestTemplate restTemplate() {
        var f = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        f.setConnectTimeout(10_000);
        f.setReadTimeout(60_000);
        return new RestTemplate(f);
    }

    private HttpHeaders textHeader() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.TEXT_PLAIN);
        return h;
    }

    /** 등록: 사진+성별+범위를 파이썬으로 그대로 포워드(임베딩 저장하지 않음) */
    @Transactional
    public void registerAndForward(Integer userId, MultipartFile photo, String matchingGender, Integer olderThan, Integer youngerThan) {
        // (선택) 로컬에 선호만 저장
        idealTypeService.savePreferencesOnly(userId, matchingGender, olderThan, youngerThan);

        try {
            ByteArrayResource res = new ByteArrayResource(photo.getBytes()) {
                @Override public String getFilename() { return photo.getOriginalFilename(); }
            };
            HttpHeaders ph = new HttpHeaders();
            MediaType mt = (photo.getContentType() != null)
                    ? MediaType.parseMediaType(photo.getContentType())
                    : MediaType.APPLICATION_OCTET_STREAM;
            ph.setContentType(mt);
            ph.setContentDispositionFormData("photo", photo.getOriginalFilename());

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("userId", new HttpEntity<>(String.valueOf(userId), textHeader()));
            body.add("matchingGender", new HttpEntity<>(idealTypeService.normalizeGender(matchingGender), textHeader()));
            body.add("olderThan", new HttpEntity<>(String.valueOf(Math.max(0, olderThan == null ? 0 : olderThan)), textHeader()));
            body.add("youngerThan", new HttpEntity<>(String.valueOf(Math.max(0, youngerThan == null ? 0 : youngerThan)), textHeader()));
            body.add("photo", new HttpEntity<>(res, ph));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            String url = pythonBaseUrl + "/ideal/register"; // 파이썬은 비동기 처리 후 OK만 반환
            ResponseEntity<Map> r = restTemplate().postForEntity(url, new HttpEntity<>(body, headers), Map.class);
            if (!r.getStatusCode().is2xxSuccessful()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Python ideal/register failed: " + r.getStatusCode());
            }
        } catch (HttpStatusCodeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Python ideal/register error: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString(), ex);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "파이썬 전송 실패(ideal/register)", e);
        }
    }

    /** 결과 조회(폴링): 파이썬에서 가져와 캐시 갱신 후 반환, 실패 시 캐시 반환 */
    @Transactional
    public List<IdealMatchDTO> fetchAndCacheFromPython(Integer ownerUserId) {
        try {
            String url = pythonBaseUrl + "/ideal/results/" + ownerUserId;
            ResponseEntity<List> r = restTemplate().getForEntity(url, List.class);
            if (!r.getStatusCode().is2xxSuccessful() || r.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Python results empty");
            }
            List<IdealMatchDTO> items = toDTOList(r.getBody());

            // 캐시 덮어쓰기
            matchRepo.deleteByOwnerUserId(ownerUserId);
            int rank = 1;
            for (IdealMatchDTO d : items) {
                IdealMatchResult m = new IdealMatchResult();
                m.setOwnerUserId(ownerUserId);
                m.setMatchedUserId(d.getUserId());
                m.setProfilePhotosJson(om.writeValueAsString(d.getProfilePhotos()));
                m.setHeight(d.getHeight());
                m.setRankOrder(rank++);

                // 지역 세팅: 1) DTO에 id가 있으면 그대로 사용  2) 없으면 matched user의 프로필에서 보강
                Region region = null;
                Subregion subregion = null;

                if (d.getRegionId() != null) {
                    region = regionRepository.findById(d.getRegionId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid regionId in python result"));
                }
                if (d.getSubregionId() != null) {
                    subregion = subregionRepository.findById(d.getSubregionId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid subregionId in python result"));
                }

                if (region == null || subregion == null) {
                    UserProfile up = userProfileRepository.findById(d.getUserId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No user profile for matched user: " + d.getUserId()));
                    region = up.getRegion();
                    subregion = up.getSubregion();
                }

                // 최종 검증: 소속 일치
                if (!subregion.getRegion().getId().equals(region.getId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "subregion does not belong to region (python result mismatch)");
                }

                m.setRegion(region);
                m.setSubregion(subregion);

                matchRepo.save(m);
            }
            return items;
        } catch (HttpStatusCodeException ex) {
            // 아직 준비 안됨 → 캐시 반환
            return getCached(ownerUserId);
        } catch (Exception e) {
            return getCached(ownerUserId);
        }
    }

    /** 캐시 조회 */
    @Transactional(readOnly = true)
    public List<IdealMatchDTO> getCached(Integer ownerUserId) {
        var rows = matchRepo.findByOwnerUserIdOrderByRankOrderAsc(ownerUserId);
        List<IdealMatchDTO> out = new ArrayList<>();
        for (IdealMatchResult r : rows) {
            IdealMatchDTO d = new IdealMatchDTO();
            d.setUserId(r.getMatchedUserId());
            try {
                d.setProfilePhotos(om.readValue(r.getProfilePhotosJson(), new TypeReference<List<String>>(){}));
            } catch (Exception ignore) {
                d.setProfilePhotos(List.of());
            }
            d.setHeight(r.getHeight());
            d.setRank(r.getRankOrder());

            // ✅ 캐시에 저장해 둔 region/subregion에서 ID+이름 모두 내려줌
            Region reg = r.getRegion();
            Subregion sub = r.getSubregion();
            if (reg != null) {
                d.setRegionId(reg.getId());
                d.setRegionName(reg.getNameKo());
            }
            if (sub != null) {
                d.setSubregionId(sub.getId());
                d.setSubregionName(sub.getNameKo());
            }

            out.add(d);
        }
        return out;
    }

    /** 파이썬 웹훅: 결과 수신 시 캐시 갱신 */
    @Transactional
    public void upsertFromWebhook(Integer ownerUserId, List<IdealMatchDTO> items) {
        matchRepo.deleteByOwnerUserId(ownerUserId);
        int rank = 1;
        try {
            for (IdealMatchDTO d : items) {
                IdealMatchResult m = new IdealMatchResult();
                m.setOwnerUserId(ownerUserId);
                m.setMatchedUserId(d.getUserId());
                m.setProfilePhotosJson(om.writeValueAsString(d.getProfilePhotos()));
                m.setHeight(d.getHeight());
                m.setRankOrder(rank++);

                // 지역 세팅: webhook payload에 id가 없으면 matched user의 프로필로 보강
                Region region = null;
                Subregion subregion = null;

                if (d.getRegionId() != null) {
                    region = regionRepository.findById(d.getRegionId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid regionId in webhook"));
                }
                if (d.getSubregionId() != null) {
                    subregion = subregionRepository.findById(d.getSubregionId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid subregionId in webhook"));
                }

                if (region == null || subregion == null) {
                    UserProfile up = userProfileRepository.findById(d.getUserId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No user profile for matched user: " + d.getUserId()));
                    region = up.getRegion();
                    subregion = up.getSubregion();
                }

                if (!subregion.getRegion().getId().equals(region.getId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "subregion does not belong to region (webhook mismatch)");
                }

                m.setRegion(region);
                m.setSubregion(subregion);

                matchRepo.save(m);
            }
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 payload", e);
        }
    }

    // Python 응답(List<Map>) → DTO 변환
    @SuppressWarnings("unchecked")
    private List<IdealMatchDTO> toDTOList(List raw) {
        List<IdealMatchDTO> out = new ArrayList<>();
        int rank = 1;
        for (Object o : raw) {
            Map<String, Object> m = (Map<String, Object>) o;
            IdealMatchDTO d = new IdealMatchDTO();

            d.setUserId(getAsInteger(m.get("user_id")));
            d.setProfilePhotos((List<String>) m.getOrDefault("profile_photos", List.of()));
            d.setHeight(getAsInteger(m.get("height")));
            d.setRank(rank++);

            // ✅ 지역: python이 제공하면 사용, 아니면 null (이후 캐싱 단계에서 프로필로 보강)
            Long regionId = getAsLong(m.get("region_id"));          // python이 줄 경우
            Long subregionId = getAsLong(m.get("subregion_id"));    // python이 줄 경우
            String regionName = getAsString(m.get("region_name"));  // 선택
            String subregionName = getAsString(m.get("subregion_name"));

            d.setRegionId(regionId);
            d.setSubregionId(subregionId);
            d.setRegionName(regionName);
            d.setSubregionName(subregionName);

            out.add(d);
        }
        return out;
    }

    // ----- helpers -----
    private Integer getAsInteger(Object v) {
        if (v == null) return null;
        if (v instanceof Integer i) return i;
        if (v instanceof Number n) return n.intValue();
        return Integer.valueOf(v.toString());
    }
    private Long getAsLong(Object v) {
        if (v == null) return null;
        if (v instanceof Long l) return l;
        if (v instanceof Number n) return n.longValue();
        return Long.valueOf(v.toString());
    }
    private String getAsString(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}
