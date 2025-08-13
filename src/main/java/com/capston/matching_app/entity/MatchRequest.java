package com.capston.matching_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

//매칭 신청 엔터티
@Entity //JPA 엔터티임을 표시, DB테이블과 매핑됨
@Table(name="match_request") //이 엔터티가 매핑될 실제 테이블 이름 지정
@Getter @Setter //모든 필드에 대한 Getter, Setter 메서드 자동 생성
@NoArgsConstructor //파라미터 없는 기본 생성자 자동 생성
@AllArgsConstructor //모든 필드 받는 생성자 자동 생성
@Builder //빌더 패턴을 사용해 객체 생성 가능하게 함
public class MatchRequest {
    @Id //pk 필드임을 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //pk 값 자동 생성, mysql의 auto_increment 전략 사용
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    // 다대일 관계 매핑, 매칭 신청자는 한 명의 user와 연결
    // lazy : 실제 user 객체는 필요할 때(DB 조회 시점) 로딩
    @JoinColumn(name = "from_user_id", nullable = false)
    // 외래 키 컬럼명 지정, null불가 (반드시 신청자는 있어야 함)
    private User fromUser; //매칭 신청을 건 사람

    @ManyToOne(fetch = FetchType.LAZY)
    // 매칭 신청 대상자도 한 명의 User와 연결됨
    @JoinColumn(name ="to_user_id", nullable = false)
    //외래 키 컬럼명 지정, null 불가 (반드시 신청 대상자는 있어야 함)
    private User toUser; // 매칭 신청 받은 사람

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @Column(name="request_time", nullable = false)
    private LocalDateTime requestTime; //매칭 신청한 시각

    //null허용 -> 매칭 이후 결과 없을 수도 있기 때문
    private  String meetingResult; //성사, 안됨, null

    //null허용 -> 매칭 유지 상태가 아직 정해지지 않을 수도 있기에
    private String matchKeepStatus; //매칭 유지 여부 : 유지, 해지, null



}


