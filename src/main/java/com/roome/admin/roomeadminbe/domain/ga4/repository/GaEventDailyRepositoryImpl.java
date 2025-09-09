//package com.roome.admin.roomeadminbe.domain.ga4.repository;
//
//import com.querydsl.core.Tuple;
//import com.querydsl.core.types.dsl.CaseBuilder;
//import com.querydsl.core.types.dsl.Expressions;
//import com.querydsl.core.types.dsl.NumberExpression;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import com.roome.admin.roomeadminbe.domain.ga4.dto.response.ActivityHourResponse;
//import lombok.RequiredArgsConstructor;
//
//import java.time.LocalDate;
//import java.util.List;
//
//import static com.roome.admin.roomeadminbe.domain.ga4.entity.QGaEventDaily.gaEventDaily;
//
//@RequiredArgsConstructor
//public class GaEventDailyRepositoryImpl implements GaEventDailyRepositoryCustom {
//
//    private final JPAQueryFactory jpaQueryFactory;
//
//    @Override
//    public List<ActivityHourResponse> getActivityByCustomRange(LocalDate date) {
//        NumberExpression<Integer> hour =
//                Expressions.numberTemplate(Integer.class, "HOUR({0})", gaEventDaily.eventAt);
//
//        // 시간대 분류
//        NumberExpression<Integer> range = new CaseBuilder()
//                .when(hour.goe(0).and(hour.lt(6))).then(0)   // 0 <= hour < 6 → 새벽
//                .when(hour.goe(6).and(hour.lt(12))).then(1)  // 6 <= hour < 12 → 오전
//                .when(hour.goe(12).and(hour.lt(14))).then(2) // 12 <= hour < 14 → 점심시간
//                .when(hour.goe(14).and(hour.lt(18))).then(3) // 14 <= hour < 18 → 오후
//                .otherwise(4);                               // 18 <= hour < 24 → 저녁                     // 저녁 (18~23)
//
//        // 집계 쿼리
//        List<Tuple> result = jpaQueryFactory
//                .select(range, gaEventDaily.id.count())
//                .from(gaEventDaily)
//                .where(gaEventDaily.statDate.eq(date))
//                .groupBy(range)
//                .fetch();
//
//        // 전체 합계
//        long total = result.stream().mapToLong(r -> r.get(gaEventDaily.id.count())).sum();
//
//        // 결과 매핑
//        return result.stream()
//                .map(r -> {
//                    int rangeIdx = r.get(range);
//                    String label = switch (rangeIdx) {
//                        case 0 -> "새벽 (0~6시)";
//                        case 1 -> "오전 (6~12시)";
//                        case 2 -> "점심시간 (12~14시)";
//                        case 3 -> "오후 (14~18시)";
//                        default -> "저녁 (18~24시)";
//                    };
//                    long count = r.get(gaEventDaily.id.count());
//                    double percent = total > 0 ? (count * 100.0 / total) : 0.0;
//                    return ActivityHourResponse.builder()
//                            .timeRange(label)
//                            .eventCount(count)
//                            .percentage(percent)
//                            .build();
//                })
//                .toList();
//    }
//}
