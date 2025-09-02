package com.capston.matching_app.service;

import com.capston.matching_app.dto.FaceEmbeddingRequestDTO;
import com.capston.matching_app.dto.FaceEmbeddingResponseDTO;
import com.capston.matching_app.entity.FaceData;
import com.capston.matching_app.entity.FacenetData;
import com.capston.matching_app.repository.FaceDataRepository;
import com.capston.matching_app.repository.FacenetDataRepository;
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
    private final FacenetDataRepository facenetDataRepository;

    // same=true 시 사진 저장 등에 사용 (이미 있다면 주입 / 없으면 제거)
    private final UserPhotoService userPhotoService;

    @Value("${external.python.base-url}")
    private String pythonBaseUrl;

    private static final String INSIGHT_EMBED_ENDPOINT   = "/embeddings"; // front/left/right → Insight(3)+FaceNet(1)
    private static final String INSIGHT_COMPARE_ENDPOINT = "/compare";    // 프로필 vs 저장 임베딩 비교 (InsightFace)

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RestTemplate restTemplate() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(60_000);
        return new RestTemplate(factory);
    }

    // ---------- 유틸 ----------
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

    // ---------- Python 연동 ----------
    /** 이미지 3장 → Python /embeddings → InsightFace 3 + FaceNet 1 수신 */
    private FaceEmbeddingResponseDTO requestEmbeddingsFromPython(MultipartFile front, MultipartFile left, MultipartFile right) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("front", filePart(front, "front"));
            body.add("left",  filePart(left,  "left"));
            body.add("right", filePart(right, "right"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            String url = pythonBaseUrl + INSIGHT_EMBED_ENDPOINT;
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
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "파이썬 서버 연동 실패(/embeddings)", e);
        }
    }

    /** 프로필 이미지 vs 저장 임베딩 비교 (samePerson boolean만 사용) */
    private boolean requestCompareFromPython(MultipartFile profile, List<Float> embF, List<Float> embL, List<Float> embR) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("profile", filePart(profile, "profile"));
            body.add("embeddingFront", textPart(toJson(embF)));
            body.add("embeddingLeft",  textPart(toJson(embL)));
            body.add("embeddingRight", textPart(toJson(embR)));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            String url = pythonBaseUrl + INSIGHT_COMPARE_ENDPOINT;
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

    // ---------- DB 연동 ----------
    /** (이미지 업로드 경로) front/left/right → /embeddings → 두 테이블 동시 UPSERT */
    @Transactional
    public void upsertEmbeddingsFromImages(Integer userId,
                                           MultipartFile front,
                                           MultipartFile left,
                                           MultipartFile right) {

        // Python 호출 (Insight 3 + FaceNet 1)
        FaceEmbeddingResponseDTO emb = requestEmbeddingsFromPython(front, left, right);

        // face_data (InsightFace 3개)
        FaceData fd = faceDataRepository.findByUserId(userId)
                .orElseGet(() -> {
                    FaceData f = new FaceData();
                    f.setUserId(userId);
                    return f;
                });
        fd.setEmbeddingFront(toJson(emb.getEmbeddingFront()));
        fd.setEmbeddingLeft(toJson(emb.getEmbeddingLeft()));
        fd.setEmbeddingRight(toJson(emb.getEmbeddingRight()));
        faceDataRepository.saveAndFlush(fd);

        // facenet_data (FaceNet 정면 1개)
        // facenet 임베딩은 /embeddings 응답 JSON에 "facenetFront" 키로 포함되어 있어야 함
        Map<?,?> raw = objectMapper.convertValue(emb, Map.class);
        Object fnRaw = raw.get("facenetFront");
        if (fnRaw instanceof List<?> list) {
            @SuppressWarnings("unchecked")
            List<Float> fnFront = (List<Float>) list;
            FacenetData fn = facenetDataRepository.findByUserId(userId)
                    .orElseGet(() -> FacenetData.builder().userId(userId).build());
            fn.setEmbeddingFront(toJson(fnFront));
            facenetDataRepository.saveAndFlush(fn);
        } else {
            // facenetFront가 누락됐으면 로그 정도 남기고 넘어가거나, 예외로 처리
            // throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "facenetFront 누락");
        }
    }

    /** (JSON 경로) 클라이언트가 임베딩 배열을 직접 전달 → 두 테이블 동시 UPSERT */
    @Transactional
    public void saveEmbeddingsFromClient(FaceEmbeddingRequestDTO dto) {
        Integer userId = dto.getUserId();

        // face_data
        FaceData fd = faceDataRepository.findByUserId(userId)
                .orElseGet(() -> {
                    FaceData f = new FaceData();
                    f.setUserId(userId);
                    return f;
                });
        fd.setEmbeddingFront(toJson(dto.getEmbeddingFront()));
        fd.setEmbeddingLeft(toJson(dto.getEmbeddingLeft()));
        fd.setEmbeddingRight(toJson(dto.getEmbeddingRight()));
        faceDataRepository.saveAndFlush(fd);

        // facenet_data
        FacenetData fn = facenetDataRepository.findByUserId(userId)
                .orElseGet(() -> FacenetData.builder().userId(userId).build());
        fn.setEmbeddingFront(toJson(dto.getFacenetFront()));
        facenetDataRepository.saveAndFlush(fn);
    }

    /** 조회: userId 기준 저장된 InsightFace 임베딩 3개 반환 */
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

    /** 삭제: userId 기준 face_data + facenet_data 함께 정리 */
    @Transactional
    public void deleteByUserId(Integer userId) {
        faceDataRepository.deleteByUserId(userId);
        facenetDataRepository.deleteByUserId(userId);
    }

    /** 비교: 프로필 이미지 vs 저장 임베딩(InsightFace) */
    @Transactional(readOnly = true)
    public boolean compareProfileWithUserEmbeddings(Integer userId, MultipartFile profile) {
        FaceEmbeddingResponseDTO emb = getEmbeddings(userId);
        if (emb == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No embeddings for userId=" + userId);
        return requestCompareFromPython(profile, emb.getEmbeddingFront(), emb.getEmbeddingLeft(), emb.getEmbeddingRight());
    }

    /** 비교+저장(옵션): 매칭되면 사진 저장 */
    @Transactional
    public boolean compareAndSaveIfMatch(Integer userId, MultipartFile profile, boolean isProfile) {
        boolean same = compareProfileWithUserEmbeddings(userId, profile);
        if (same) {
            try { userPhotoService.upload(userId, profile, isProfile); }
            catch (Exception ignored) {}
        }
        return same;
    }
}
