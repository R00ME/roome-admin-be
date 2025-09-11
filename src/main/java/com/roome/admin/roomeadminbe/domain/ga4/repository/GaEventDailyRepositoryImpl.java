package com.roome.admin.roomeadminbe.domain.ga4.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roome.admin.roomeadminbe.domain.ga4.dto.response.ActivityHourResponse;
import com.roome.admin.roomeadminbe.domain.ga4.dto.response.ChartResponse;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.roome.admin.roomeadminbe.domain.ga4.entity.QGaEventDaily.gaEventDaily;

@RequiredArgsConstructor
public class GaEventDailyRepositoryImpl implements GaEventDailyRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ActivityHourResponse> getActivityByCustomRange(LocalDate date) {
        NumberExpression<Integer> hour =
                Expressions.numberTemplate(Integer.class, "HOUR({0})", gaEventDaily.eventAt);

        // 시간대 분류
        NumberExpression<Integer> range = new CaseBuilder()
                .when(hour.goe(0).and(hour.lt(6))).then(0)   // 0 <= hour < 6 → 새벽
                .when(hour.goe(6).and(hour.lt(12))).then(1)  // 6 <= hour < 12 → 오전
                .when(hour.goe(12).and(hour.lt(14))).then(2) // 12 <= hour < 14 → 점심시간
                .when(hour.goe(14).and(hour.lt(18))).then(3) // 14 <= hour < 18 → 오후
                .otherwise(4);                               // 18 <= hour < 24 → 저녁                     // 저녁 (18~23)

        // 집계 쿼리
        List<Tuple> result = jpaQueryFactory
                .select(range, gaEventDaily.id.count())
                .from(gaEventDaily)
                .where(gaEventDaily.statDate.eq(date))
                .groupBy(range)
                .fetch();

        // 전체 합계
        long total = result.stream().mapToLong(r -> r.get(gaEventDaily.id.count())).sum();

        // 결과 매핑
        return result.stream()
                .map(r -> {
                    int rangeIdx = r.get(range);
                    String label = switch (rangeIdx) {
                        case 0 -> "새벽 (0~6시)";
                        case 1 -> "오전 (6~12시)";
                        case 2 -> "점심시간 (12~14시)";
                        case 3 -> "오후 (14~18시)";
                        default -> "저녁 (18~24시)";
                    };
                    long count = r.get(gaEventDaily.id.count());
                    double percent = total > 0 ? (count * 100.0 / total) : 0.0;
                    return ActivityHourResponse.builder()
                            .timeRange(label)
                            .eventCount(count)
                            .percentage(percent)
                            .build();
                })
                .toList();
    }

    @Override
    public String getMauValue() {
        Long sum = jpaQueryFactory
                .select(gaEventDaily.eventCount.sum().coalesce(0L))
                .from(gaEventDaily)
                .where(
                        gaEventDaily.eventName.eq("active28DayUsers"),
                        gaEventDaily.statDate.eq(LocalDate.now())
                )
                .fetchOne();

        return String.valueOf(sum == null ? 0L : sum);
    }

    @Override
    public String getMauChangeRate() {
        Long today = jpaQueryFactory
                .select(gaEventDaily.eventCount.sum().coalesce(0L))
                .from(gaEventDaily)
                .where(
                        gaEventDaily.eventName.eq("active28DayUsers"),
                        gaEventDaily.statDate.eq(LocalDate.now())
                )
                .fetchOne();
        today = today == null ? 0L : today;

        Long yesterday = jpaQueryFactory
                .select(gaEventDaily.eventCount.sum().coalesce(0L))
                .from(gaEventDaily)
                .where(
                        gaEventDaily.eventName.eq("active28DayUsers"),
                        gaEventDaily.statDate.eq(LocalDate.now().minusDays(1))
                )
                .fetchOne();

        yesterday = yesterday == null ? 0L : yesterday;
        if(yesterday == 0L ) return null;
        Double result = ((double) (today - yesterday) / (double) yesterday);

        return String.valueOf(result);
    }

    @Override
    public String getDauValue() {
        Long sum = jpaQueryFactory
                .select(gaEventDaily.eventCount.sum().coalesce(0L))
                .from(gaEventDaily)
                .where(
                        gaEventDaily.eventName.eq("activeUsers"),
                        gaEventDaily.statDate.eq(LocalDate.now()) // 오늘 날짜
                )
                .fetchOne();

        return String.valueOf(sum == null ? 0L : sum);
    }

    @Override
    public String getDauChangeRate() {

        Long today = jpaQueryFactory
                .select(gaEventDaily.eventCount.sum().coalesce(0L))
                .from(gaEventDaily)
                .where(
                        gaEventDaily.eventName.eq("activeUsers"),
                        gaEventDaily.statDate.eq(LocalDate.now()) // 오늘 날짜
                )
                .fetchOne();

        today = today == null ? 0L : today;

        Long yesterday = jpaQueryFactory
                .select(gaEventDaily.eventCount.sum().coalesce(0L))
                .from(gaEventDaily)
                .where(
                        gaEventDaily.eventName.eq("activeUsers"),
                        gaEventDaily.statDate.eq(LocalDate.now().minusDays(1)) // 어제 날짜
                )
                .fetchOne();

        yesterday = yesterday == null ? 0L : yesterday;
        if(yesterday == 0L ) return null;
        Double result = ((double) (today - yesterday) / (double) yesterday);

        return String.valueOf(result);
    }

    @Override
    public String getInflowValue() {
        Long sum = jpaQueryFactory
                .select(gaEventDaily.eventCount.sum().coalesce(0L))
                .from(gaEventDaily)
                .where(
                        gaEventDaily.eventName.eq("first_visit"),
                        gaEventDaily.statDate.eq(LocalDate.now()) // 오늘 날짜
                )
                .fetchOne();

        return String.valueOf(sum == null ? 0L : sum);
    }

    @Override
    public String getInflowChangeRate() {
        Long today = jpaQueryFactory
                .select(gaEventDaily.eventCount.sum().coalesce(0L))
                .from(gaEventDaily)
                .where(
                        gaEventDaily.eventName.eq("first_visit"),
                        gaEventDaily.statDate.eq(LocalDate.now()) // 오늘 날짜
                )
                .fetchOne();

        today = today == null ? 0L : today;

        Long yesterday = jpaQueryFactory
                .select(gaEventDaily.eventCount.sum().coalesce(0L))
                .from(gaEventDaily)
                .where(
                        gaEventDaily.eventName.eq("first_visit"),
                        gaEventDaily.statDate.eq(LocalDate.now().minusDays(1)) // 어제 날짜
                )
                .fetchOne();

        yesterday = yesterday == null ? 0L : yesterday;
        if(yesterday == 0L ) return null;
        Double result = ((double) (today - yesterday) / (double) yesterday);

        return String.valueOf(result);
    }

    @Override
    public String getReferralValue() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate startOfNextMonth = startOfMonth.plusMonths(1);

        NumberExpression<Long> sumEventCount = gaEventDaily.eventCount.sum();

        String topSource = jpaQueryFactory
                .select(gaEventDaily.source)
                .from(gaEventDaily)
                .where(
                        gaEventDaily.eventName.eq("traffic_source"),
                        gaEventDaily.statDate.goe(startOfMonth),
                        gaEventDaily.statDate.lt(startOfNextMonth)
                )
                .groupBy(gaEventDaily.source)
                .orderBy(sumEventCount.desc()) // ORDER BY SUM(event_count) DESC
                .fetchFirst();                 // LIMIT 1

        return topSource == null ? "" : topSource;
    }

    @Override
    public List<ChartResponse> getMauChart() {
        LocalDate today = LocalDate.now();

        // 오늘 ~ 6개월 전까지(총 7개) 대상 일자 생성
        List<LocalDate> targets = new ArrayList<>();
        for (int i = 0; i <= 6; i++) {
            targets.add(today.minusMonths(i));
        }

        // SUM(event_count) 별칭을 잡아두면 Tuple에서 안전하게 꺼낼 수 있어요
        NumberExpression<Long> sumEventCount = gaEventDaily.eventCount.sum();

        List<Tuple> rows = jpaQueryFactory
                .select(gaEventDaily.statDate, sumEventCount)
                .from(gaEventDaily)
                .where(
                        gaEventDaily.eventName.eq("active28DayUsers"),
                        gaEventDaily.statDate.in(targets)
                )
                .groupBy(gaEventDaily.statDate)
                .orderBy(gaEventDaily.statDate.asc())
                .fetch();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return rows.stream()
                .map(t -> new ChartResponse(
                        t.get(gaEventDaily.statDate).format(fmt),                // xLabels
                        String.valueOf(t.get(sumEventCount))                     // value
                ))
                .toList();
    }

    @Override
    public List<ChartResponse> getDauChart() {
        LocalDate today = LocalDate.now();

        // 오늘 ~ 6일 전까지 날짜 리스트 만들기
        List<LocalDate> targets = new ArrayList<>();
        for (int i = 0; i <= 6; i++) {
            targets.add(today.minusDays(i));
        }

        NumberExpression<Long> sumEventCount = gaEventDaily.eventCount.sum();

        List<Tuple> rows = jpaQueryFactory
                .select(gaEventDaily.statDate, sumEventCount)
                .from(gaEventDaily)
                .where(
                        gaEventDaily.eventName.eq("activeUsers"),
                        gaEventDaily.statDate.in(targets)
                )
                .groupBy(gaEventDaily.statDate)
                .orderBy(gaEventDaily.statDate.asc())
                .fetch();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return rows.stream()
                .map(t -> new ChartResponse(
                        t.get(gaEventDaily.statDate).format(fmt),   // xLabels
                        String.valueOf(t.get(sumEventCount))        // value
                ))
                .toList();
    }

    @Override
    public List<ChartResponse> getInflowChart() {
        LocalDate today = LocalDate.now();

        // 오늘 ~ 6일 전까지 날짜 리스트
        List<LocalDate> targets = new ArrayList<>();
        for (int i = 0; i <= 6; i++) {
            targets.add(today.minusDays(i));
        }

        NumberExpression<Long> sumEventCount = gaEventDaily.eventCount.sum();

        List<Tuple> rows = jpaQueryFactory
                .select(gaEventDaily.statDate, sumEventCount)
                .from(gaEventDaily)
                .where(
                        gaEventDaily.eventName.eq("first_visit"),
                        gaEventDaily.statDate.in(targets)
                )
                .groupBy(gaEventDaily.statDate)
                .orderBy(gaEventDaily.statDate.asc())
                .fetch();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return rows.stream()
                .map(t -> new ChartResponse(
                        t.get(gaEventDaily.statDate).format(fmt),   // xLabels
                        String.valueOf(t.get(sumEventCount))        // value
                ))
                .toList();
    }

    @Override
    public List<ChartResponse> getContentChart() {
        LocalDate today = LocalDate.now();
        // 이번 달부터 6개월 전까지 총 7개월
        List<YearMonth> targetMonths = new ArrayList<>();
        for (int i = 0; i <= 6; i++) {
            targetMonths.add(YearMonth.from(today.minusMonths(i)));
        }

        NumberExpression<Long> sumEventCount = gaEventDaily.eventCount.sum();

        List<Tuple> rows = jpaQueryFactory
                .select(
                        gaEventDaily.statDate, // 일단 날짜 단위로 뽑아서 이후 YearMonth로 변환
                        sumEventCount
                )
                .from(gaEventDaily)
                .where(
                        gaEventDaily.eventName.like("%usage"),
                        gaEventDaily.statDate.goe(today.withDayOfMonth(1).minusMonths(6)) // 6개월 전 1일부터
                )
                .groupBy(gaEventDaily.statDate)
                .fetch();

        // statDate → YearMonth 변환 후 집계
        Map<YearMonth, Long> aggregated = rows.stream()
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.get(gaEventDaily.statDate)),
                        Collectors.summingLong(t -> t.get(sumEventCount))
                ));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");

        return targetMonths.stream()
                .sorted()
                .map(ym -> new ChartResponse(
                        ym.format(fmt), // xLabels: yyyy-MM
                        String.valueOf(aggregated.getOrDefault(ym, 0L)) // value: 합계 (없으면 0)
                ))
                .toList();
    }

    @Override
    public List<ChartResponse> getReferralChart() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter ymFmt = DateTimeFormatter.ofPattern("yyyy-MM");

        List<ChartResponse> results = new ArrayList<>();

        // 0 ~ 6개월 전까지 반복
        for (int i = 0; i <= 6; i++) {
            LocalDate startOfMonth = today.withDayOfMonth(1).minusMonths(i);
            LocalDate startOfNextMonth = startOfMonth.plusMonths(1);

            NumberExpression<Long> sumEventCount = gaEventDaily.eventCount.sum();

            Tuple row = jpaQueryFactory
                    .select(gaEventDaily.source, sumEventCount)
                    .from(gaEventDaily)
                    .where(
                            gaEventDaily.eventName.eq("traffic_source"),
                            gaEventDaily.statDate.goe(startOfMonth),
                            gaEventDaily.statDate.lt(startOfNextMonth)
                    )
                    .groupBy(gaEventDaily.source)
                    .orderBy(sumEventCount.desc())
                    .fetchFirst(); // limit 1

            if (row != null) {
                String topSource = row.get(gaEventDaily.source);
                Long count = row.get(sumEventCount);

                results.add(new ChartResponse(
                        startOfMonth.format(ymFmt),          // xLabels = 해당 월 (yyyy-MM)
                        topSource        // value = 소스명
                ));
            }
        }

        // 과거→현재 순으로 정렬
        return results.stream()
                .sorted(Comparator.comparing(ChartResponse::getXLabels))
                .toList();
    }

    @Override
    public String getMostEntryPath() {    LocalDate today = LocalDate.now();
        LocalDate startOfThisMonth = today.withDayOfMonth(1);
        LocalDate startOfNextMonth = startOfThisMonth.plusMonths(1);
        LocalDate startOfPrevMonth = startOfThisMonth.minusMonths(1);

        // 이번 달 합계: [이번달1일, 다음달1일)
        NumberExpression<Long> currentMonthTotal =
                new CaseBuilder()
                        .when(gaEventDaily.statDate.goe(startOfThisMonth)
                                .and(gaEventDaily.statDate.lt(startOfNextMonth)))
                        .then(gaEventDaily.eventCount)
                        .otherwise(0L)
                        .sum();

        // 저번 달 합계: [저번달1일, 이번달1일)
        NumberExpression<Long> previousMonthTotal =
                new CaseBuilder()
                        .when(gaEventDaily.statDate.goe(startOfPrevMonth)
                                .and(gaEventDaily.statDate.lt(startOfThisMonth)))
                        .then(gaEventDaily.eventCount)
                        .otherwise(0L)
                        .sum();

        // (current - previous) / previous  (previous=0이면 0)
        NumberExpression<Double> changeRatio = Expressions.numberTemplate(
                Double.class,
                "CASE WHEN {0} = 0 THEN 0 ELSE (CAST({1} AS double) - CAST({0} AS double)) / CAST({0} AS double) END",
                previousMonthTotal, currentMonthTotal
        );

        Tuple row = jpaQueryFactory
                .select(
                        gaEventDaily.source,
                        currentMonthTotal,
                        previousMonthTotal,
                        changeRatio
                )
                .from(gaEventDaily)
                .where(
                        gaEventDaily.eventName.eq("traffic_source"),
                        // 두 달 범위를 한 번에 커버: [저번달1일, 다음달1일)
                        gaEventDaily.statDate.goe(startOfPrevMonth),
                        gaEventDaily.statDate.lt(startOfNextMonth)
                )
                .groupBy(gaEventDaily.source)
                .orderBy(changeRatio.desc())
                .fetchFirst(); // LIMIT 1

        if (row == null) {
            return null;
        }
        return row.get(gaEventDaily.source);
    }
}
