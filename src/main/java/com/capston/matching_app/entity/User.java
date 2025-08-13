package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.*;

//실제 구현이 아니라, MatchRequest 테스트용 임시 버전
// 따라서 id, name, email 정도만 넣어둠
//나중에 팀원이 만든 User로 갈아끼우면 됨

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId; // PK

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // 비밀번호나 기타 컬럼은 생략
}
