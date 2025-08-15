package com.capston.matching_app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/datasource")
    public ResponseEntity<?> datasourceInfo() {
        try (Connection c = dataSource.getConnection()) {
            String url = c.getMetaData().getURL();
            String user = c.getMetaData().getUserName();
            return ResponseEntity.ok(Map.of("jdbcUrl", url, "user", user));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * userId 기준으로 face_data 테이블을 직접 조회하여
     * - 존재 여부
     * - 레코드 id / userId
     * - 임베딩 TEXT 길이(LENGTH) 요약
     * - 생성 시각(created_at)
     * 을 반환합니다.
     */
    @GetMapping("/face/{userId}")
    public ResponseEntity<?> faceSummary(@PathVariable Integer userId) {
        String sql =
                "SELECT id, user_id, " +
                        "       LENGTH(embedding_front) AS lenF, " +
                        "       LENGTH(embedding_left)  AS lenL, " +
                        "       LENGTH(embedding_right) AS lenR, " +
                        "       created_at " +
                        "  FROM face_data " +
                        " WHERE user_id = ? " +
                        " LIMIT 1";

        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, userId);

            int id    = ((Number) row.get("id")).intValue();
            int uId   = ((Number) row.get("user_id")).intValue();
            int lenF  = row.get("lenF") != null ? ((Number) row.get("lenF")).intValue() : 0;
            int lenL  = row.get("lenL") != null ? ((Number) row.get("lenL")).intValue() : 0;
            int lenR  = row.get("lenR") != null ? ((Number) row.get("lenR")).intValue() : 0;
            Object createdAt = row.get("created_at");

            return ResponseEntity.ok(Map.of(
                    "exists", true,
                    "id", id,
                    "userId", uId,
                    "lengths", Map.of("front", lenF, "left", lenL, "right", lenR),
                    "createdAt", createdAt
            ));
        } catch (EmptyResultDataAccessException noRow) {
            // 해당 userId의 임베딩이 없는 경우
            return ResponseEntity.ok(Map.of("exists", false));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
