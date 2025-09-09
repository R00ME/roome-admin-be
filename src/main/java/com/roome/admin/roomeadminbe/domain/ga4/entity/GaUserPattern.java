//package com.roome.admin.roomeadminbe.domain.ga4.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//@Entity
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor
//@Builder
//@Table(name = "ga_user_pattern")
//public class GaUserPattern {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private LocalDate statDate;
//    private String customUserId;
//    private String featureName;
//
//    private Long totalDuration;   // 사용자별 총 사용시간
//    private Long eventCount;      // 사용자별 이벤트 발생 수
//    private LocalDateTime firstUseAt;
//    private LocalDateTime lastUseAt;
//}