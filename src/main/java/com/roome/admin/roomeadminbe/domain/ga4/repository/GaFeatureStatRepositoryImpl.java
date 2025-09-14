package com.roome.admin.roomeadminbe.domain.ga4.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import static com.roome.admin.roomeadminbe.domain.ga4.entity.QGaFeatureStat.gaFeatureStat;

@RequiredArgsConstructor
public class GaFeatureStatRepositoryImpl implements GaFeatureStatRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public String getContentValue() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate startOfNextMonth = startOfMonth.plusMonths(1);

        Long sum = jpaQueryFactory
                .select(gaFeatureStat.eventCount.sum().coalesce(0L))
                .from(gaFeatureStat)
                .where(
                        gaFeatureStat.eventName.like("%usage"), // '%usage'
                        gaFeatureStat.statDate.goe(startOfMonth),
                        gaFeatureStat.statDate.lt(startOfNextMonth)
                )
                .fetchOne();

        return String.valueOf(sum == null ? 0L : sum);
    }

    @Override
    public String getContentChangeRate() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate startOfNextMonth = startOfMonth.plusMonths(1);

        Long thisMonth = jpaQueryFactory
                .select(gaFeatureStat.eventCount.sum().coalesce(0L))
                .from(gaFeatureStat)
                .where(
                        gaFeatureStat.eventName.like("%usage"), // '%usage'
                        gaFeatureStat.statDate.goe(startOfMonth),
                        gaFeatureStat.statDate.lt(startOfNextMonth)
                )
                .fetchOne();
        thisMonth = thisMonth == null ? 0L : thisMonth;

        // 저번 달의 시작일과 끝일 구하기
        LocalDate startOfLastMonth = LocalDate.now().withDayOfMonth(1).minusMonths(1);
        LocalDate startOfThisMonth = startOfLastMonth.plusMonths(1);

        Long lastMonth = jpaQueryFactory
                .select(gaFeatureStat.eventCount.sum().coalesce(0L))
                .from(gaFeatureStat)
                .where(
                        gaFeatureStat.eventName.like("%usage"), // '%usage'
                        gaFeatureStat.statDate.goe(startOfLastMonth),
                        gaFeatureStat.statDate.lt(startOfThisMonth)
                )
                .fetchOne();
        lastMonth = lastMonth == null ? 0L : lastMonth;
        if (lastMonth == 0L) return null;
        Double result = ((double) (thisMonth - lastMonth) / (double) lastMonth);

        return String.valueOf(result);
    }

    @Override
    public String getMostUsedFeature() {

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate startOfNextMonth = startOfMonth.plusMonths(1);

        NumberExpression<Long> sumDuration = gaFeatureStat.totalDuration.sum();

        String topEventName = jpaQueryFactory
                .select(gaFeatureStat.eventName)
                .from(gaFeatureStat)
                .where(
                        gaFeatureStat.eventName.like("%usage"),
                        gaFeatureStat.statDate.goe(startOfMonth),
                        gaFeatureStat.statDate.lt(startOfNextMonth)
                )
                .groupBy(gaFeatureStat.eventName)
                .orderBy(sumDuration.desc())
                .fetchFirst();

        return topEventName == null ? "" : topEventName;
    }

    @Override
    public String getMostDroppedFeature() {
        LocalDate today = LocalDate.now();
        LocalDate startOfThisMonth = today.withDayOfMonth(1);
        LocalDate startOfNextMonth = startOfThisMonth.plusMonths(1);
        LocalDate startOfPrevMonth = startOfThisMonth.minusMonths(1);

        // 이번 달 합계: stat_date ∈ [이번달1일, 다음달1일)
        NumberExpression<Long> currentMonthTotal =
                new CaseBuilder()
                        .when(gaFeatureStat.statDate.goe(startOfThisMonth)
                                .and(gaFeatureStat.statDate.lt(startOfNextMonth)))
                        .then(gaFeatureStat.totalDuration)
                        .otherwise(0L)
                        .sum();

        // 저번 달 합계: stat_date ∈ [저번달1일, 이번달1일)
        NumberExpression<Long> previousMonthTotal =
                new CaseBuilder()
                        .when(gaFeatureStat.statDate.goe(startOfPrevMonth)
                                .and(gaFeatureStat.statDate.lt(startOfThisMonth)))
                        .then(gaFeatureStat.totalDuration)
                        .otherwise(0L)
                        .sum();

        // (current - previous) / previous  (previous=0이면 0으로 처리)
        NumberExpression<Double> durationDiff = Expressions.numberTemplate(
                Double.class,
                "CASE WHEN {0} = 0 THEN 0 ELSE (CAST({1} AS double) - CAST({0} AS double)) / CAST({0} AS double) END",
                previousMonthTotal, currentMonthTotal
        );

        Tuple row = jpaQueryFactory
                .select(
                        gaFeatureStat.eventName,
                        currentMonthTotal,
                        previousMonthTotal,
                        durationDiff
                )
                .from(gaFeatureStat)
                .where(
                        gaFeatureStat.eventName.like("%usage"),
                        // 저번 달 1일 ~ 다음 달 1일 직전까지 범위(두 달 모두 포함)
                        gaFeatureStat.statDate.goe(startOfPrevMonth),
                        gaFeatureStat.statDate.lt(startOfNextMonth)
                )
                .groupBy(gaFeatureStat.eventName)
                .orderBy(durationDiff.asc())
                .fetchFirst(); // LIMIT 1

        if (row == null) {
            return null;
        }

        return row.get(gaFeatureStat.eventName);
    }
}
