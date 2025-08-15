package com.capston.matching_app.service;

import com.capston.matching_app.dto.IdealMatchDTO;
import com.capston.matching_app.entity.IdealMatchResult;
import com.capston.matching_app.repository.IdealMatchResultRepository;
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

@Service
@RequiredArgsConstructor
public class IdealMatchingService {

    private final IdealTypeService idealTypeService;
    private final IdealMatchResultRepository matchRepo;
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

    /** 파이썬 결과를 가져와 캐시 갱신 후 반환(폴링) */
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
                m.setCity(d.getCity());
                m.setRankOrder(rank++);
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
            d.setCity(r.getCity());
            d.setRank(r.getRankOrder());
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
                m.setCity(d.getCity());
                m.setRankOrder(rank++);
                matchRepo.save(m);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 payload", e);
        }
    }

    // List<Map> → DTO 변환
    @SuppressWarnings("unchecked")
    private List<IdealMatchDTO> toDTOList(List raw) {
        List<IdealMatchDTO> out = new ArrayList<>();
        int rank = 1;
        for (Object o : raw) {
            Map<String, Object> m = (Map<String, Object>) o;
            IdealMatchDTO d = new IdealMatchDTO();
            d.setUserId((Integer) m.get("user_id"));
            d.setProfilePhotos((List<String>) m.get("profile_photos"));
            d.setHeight((Integer) m.get("height"));
            d.setCity((String) m.get("city"));
            d.setRank(rank++);
            out.add(d);
        }
        return out;
    }
}
