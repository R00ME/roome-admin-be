package com.roome.admin.roomeadminbe.domain.ga4.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roome.admin.roomeadminbe.domain.ga4.dto.response.ChartResponse;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.roome.admin.roomeadminbe.domain.ga4.entity.QGaUserStat.gaUserStat;

@RequiredArgsConstructor
public class GaUserStatRepositoryImpl implements GaUserStatRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public String getMauValue() {
        Long sum = jpaQueryFactory
                .select(gaUserStat.mau.sum().coalesce(0L))
                .from(gaUserStat)
                .where(
                        gaUserStat.statDate.eq(LocalDate.now())
                )
                .fetchOne();

        return String.valueOf(sum == null ? 0L : sum);
    }

    @Override
    public String getMauChangeRate() {
        Long today = jpaQueryFactory
                .select(gaUserStat.mau.sum().coalesce(0L))
                .from(gaUserStat)
                .where(
                        gaUserStat.statDate.eq(LocalDate.now())
                )
                .fetchOne();
        today = today == null ? 0L : today;

        Long yesterday = jpaQueryFactory
                .select(gaUserStat.mau.sum().coalesce(0L))
                .from(gaUserStat)
                .where(
                        gaUserStat.statDate.eq(LocalDate.now().minusMonths(1))
                )
                .fetchOne();

        yesterday = yesterday == null ? 0L : yesterday;
        if (yesterday == 0L) return null;
        Double result = ((double) (today - yesterday) * 100.0 / (double) yesterday);

        return String.valueOf(result);
    }

    @Override
    public String getDauValue() {
        Long sum = jpaQueryFactory
                .select(gaUserStat.dau.sum().coalesce(0L))
                .from(gaUserStat)
                .where(
                        gaUserStat.statDate.eq(LocalDate.now()) // 오늘 날짜
                )
                .fetchOne();

        return String.valueOf(sum == null ? 0L : sum);
    }

    @Override
    public String getDauChangeRate() {

        Long today = jpaQueryFactory
                .select(gaUserStat.dau.sum().coalesce(0L))
                .from(gaUserStat)
                .where(
                        gaUserStat.statDate.eq(LocalDate.now()) // 오늘 날짜
                )
                .fetchOne();

        today = today == null ? 0L : today;

        Long yesterday = jpaQueryFactory
                .select(gaUserStat.dau.sum().coalesce(0L))
                .from(gaUserStat)
                .where(
                        gaUserStat.statDate.eq(LocalDate.now().minusDays(1)) // 어제 날짜
                )
                .fetchOne();

        yesterday = yesterday == null ? 0L : yesterday;
        if (yesterday == 0L) return null;
        Double result = ((double) (today - yesterday) * 100.0 / (double) yesterday);

        return String.valueOf(result);
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
        NumberExpression<Long> sumEventCount = gaUserStat.mau.sum();

        List<Tuple> rows = jpaQueryFactory
                .select(gaUserStat.statDate, sumEventCount)
                .from(gaUserStat)
                .where(
                        gaUserStat.statDate.in(targets)
                )
                .groupBy(gaUserStat.statDate)
                .orderBy(gaUserStat.statDate.asc())
                .fetch();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return rows.stream()
                .map(t -> new ChartResponse(
                        t.get(gaUserStat.statDate).format(fmt),                // xLabels
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

        NumberExpression<Long> sumEventCount = gaUserStat.dau.sum();

        List<Tuple> rows = jpaQueryFactory
                .select(gaUserStat.statDate, sumEventCount)
                .from(gaUserStat)
                .where(
                        gaUserStat.statDate.in(targets)
                )
                .groupBy(gaUserStat.statDate)
                .orderBy(gaUserStat.statDate.asc())
                .fetch();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return rows.stream()
                .map(t -> new ChartResponse(
                        t.get(gaUserStat.statDate).format(fmt),   // xLabels
                        String.valueOf(t.get(sumEventCount))        // value
                ))
                .toList();
    }
}
