package com.capston.matching_app.service;

import com.capston.matching_app.dto.FaceEmbeddingResponseDTO;
import com.capston.matching_app.entity.FaceData;
import com.capston.matching_app.repository.FaceDataRepository;
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

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FaceService {

    private final FaceDataRepository faceDataRepository;
    private final UserPhotoService userPhotoService; // same=true 저장에 사용
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${external.python.base-url}")
    private String pythonBaseUrl;

    private RestTemplate restTemplate() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(60_000);
        return new RestTemplate(factory);
    }

    // ========== 유틸 ==========

    private HttpEntity<ByteArrayResource> filePart(MultipartFile file, String partName) throws Exception {
        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override public String getFilename() { return file.getOriginalFilename(); }
        };
        HttpHeaders ph = new HttpHeaders();
        MediaType mt = (file.getContentType() != null)
                ? MediaType.parseMediaType(file.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        ph.setContentType(mt);
        ph.setContentDispositionFormData(partName, file.getOriginalFilename());
        return new HttpEntity<>(resource, ph);
    }

    private HttpEntity<String> textPart(String value) {
        HttpHeaders th = new HttpHeaders();
        th.setContentType(MediaType.TEXT_PLAIN);
        return new HttpEntity<>(value, th);
    }

    private String toJson(List<Float> list) {
        try { return objectMapper.writeValueAsString(list); }
        catch (Exception e) { throw new RuntimeException("임베딩 직렬화 실패", e); }
    }

    private List<Float> fromJson(String json) {
        try { return objectMapper.readValue(json, new TypeReference<List<Float>>() {}); }
        catch (Exception e) { throw new RuntimeException("임베딩 역직렬화 실패", e); }
    }

    // ========== Python 연동 ==========

    private FaceEmbeddingResponseDTO requestEmbeddingsFromPython(MultipartFile front, MultipartFile left, MultipartFile right) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("front", filePart(front, "front"));
            body.add("left",  filePart(left,  "left"));
            body.add("right", filePart(right, "right"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            String url = pythonBaseUrl + "/embeddings";
            ResponseEntity<FaceEmbeddingResponseDTO> res =
                    restTemplate().postForEntity(url, new HttpEntity<>(body, headers), FaceEmbeddingResponseDTO.class);

            if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Python embedding API failed: " + res.getStatusCode());
            }
            return res.getBody();
        } catch (HttpStatusCodeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Python embedding API error: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString(), ex);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "파이썬 서버 연동 실패", e);
        }
    }

    /** threshold는 자바에서 다루지 않음(파이썬에서만 결정) */
    private boolean requestCompareFromPython(MultipartFile profile, List<Float> embF, List<Float> embL, List<Float> embR) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("profile", filePart(profile, "profile"));
            body.add("embeddingFront", textPart(toJson(embF)));
            body.add("embeddingLeft",  textPart(toJson(embL)));
            body.add("embeddingRight", textPart(toJson(embR)));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            String url = pythonBaseUrl + "/compare";
            ResponseEntity<Map> res = restTemplate().postForEntity(url, new HttpEntity<>(body, headers), Map.class);

            if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Python compare API failed: " + res.getStatusCode());
            }

            Object v = res.getBody().get("samePerson");
            if (v instanceof Boolean b) return b;
            if (v instanceof String s)  return Boolean.parseBoolean(s);
            return false;
        } catch (HttpStatusCodeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Python compare API error: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString(), ex);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "파이썬 비교 호출 실패", e);
        }
    }

    // ========== DB 연동 ==========

    @Transactional
    public void upsertEmbeddingsFromImages(Integer userId, MultipartFile front, MultipartFile left, MultipartFile right) {
        FaceEmbeddingResponseDTO emb = requestEmbeddingsFromPython(front, left, right);

        FaceData entity = faceDataRepository.findByUserId(userId)
                .orElseGet(() -> {
                    FaceData f = new FaceData();
                    f.setUserId(userId);
                    return f;
                });

        entity.setEmbeddingFront(toJson(emb.getEmbeddingFront()));
        entity.setEmbeddingLeft(toJson(emb.getEmbeddingLeft()));
        entity.setEmbeddingRight(toJson(emb.getEmbeddingRight()));
        faceDataRepository.saveAndFlush(entity);
    }

    @Transactional(readOnly = true)
    public FaceEmbeddingResponseDTO getEmbeddings(Integer userId) {
        return faceDataRepository.findByUserId(userId)
                .map(e -> new FaceEmbeddingResponseDTO(
                        fromJson(e.getEmbeddingFront()),
                        fromJson(e.getEmbeddingLeft()),
                        fromJson(e.getEmbeddingRight())
                ))
                .orElse(null);
    }

    @Transactional
    public void deleteByUserId(Integer userId) {
        faceDataRepository.deleteByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean compareProfileWithUserEmbeddings(Integer userId, MultipartFile profile) {
        FaceEmbeddingResponseDTO emb = getEmbeddings(userId);
        if (emb == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No embeddings for userId=" + userId);
        return requestCompareFromPython(profile, emb.getEmbeddingFront(), emb.getEmbeddingLeft(), emb.getEmbeddingRight());
    }

    /** same=true면 user_photo에 즉시 저장 (isProfile로 대표/앨범 결정) */
    @Transactional
    public boolean compareAndSaveIfMatch(Integer userId, MultipartFile profile, boolean isProfile) {
        boolean same = compareProfileWithUserEmbeddings(userId, profile);
        if (same) {
            try {
                userPhotoService.upload(userId, profile, isProfile);
            } catch (Exception e) {
                // 저장 실패는 판정 결과와 분리 (로그 권장)
            }
        }
        return same;
    }
}
