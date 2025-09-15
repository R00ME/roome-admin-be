package com.roome.admin.roomeadminbe.domain.ga4.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roome.admin.roomeadminbe.domain.ga4.dto.response.ChartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

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
                        gaUserStat.statDate.eq(LocalDate.now().minusDays(1))
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
                        gaUserStat.statDate.eq(LocalDate.now().minusDays(1))
                )
                .fetchOne();
        today = today == null ? 0L : today;

        Long yesterday = jpaQueryFactory
                .select(gaUserStat.mau.sum().coalesce(0L))
                .from(gaUserStat)
                .where(
                        gaUserStat.statDate.eq(LocalDate.now().minusMonths(2))
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
                        gaUserStat.statDate.eq(LocalDate.now().minusDays(1)) // 오늘 날짜
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
                        gaUserStat.statDate.eq(LocalDate.now().minusDays(1)) // 오늘 날짜
                )
                .fetchOne();

        today = today == null ? 0L : today;

        Long yesterday = jpaQueryFactory
                .select(gaUserStat.dau.sum().coalesce(0L))
                .from(gaUserStat)
                .where(
                        gaUserStat.statDate.eq(LocalDate.now().minusDays(2)) // 어제 날짜
                )
                .fetchOne();

        yesterday = yesterday == null ? 0L : yesterday;
        if (yesterday == 0L) return null;
        Double result = ((double) (today - yesterday) * 100.0 / (double) yesterday);

        return String.valueOf(result);
    }

    @Override
    public List<ChartResponse> getMauChart() {
        List<ChartResponse> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 오늘 ~ 6개월 전까지(총 7개) 대상 일자 생성
        for (int i = 0; i <= 6; i++) {
            List<LocalDate> targets = new ArrayList<>();
            targets.add(today.minusDays(1).minusMonths(i));
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

            List<ChartResponse> oneRow = rows.stream()
                    .map(t -> new ChartResponse(
                            t.get(gaUserStat.statDate).format(fmt),                // xLabels
                            String.valueOf(t.get(sumEventCount))                     // value
                    ))
                    .toList();

            if (ObjectUtils.isEmpty(oneRow)) {
                result.add(new ChartResponse(today.minusDays(1).minusMonths(i).format(fmt)
                        , String.valueOf(0)));
            } else {
                result.addAll(oneRow);
            }

        }
        return result;


        // SUM(event_count) 별칭을 잡아두면 Tuple에서 안전하게 꺼낼 수 있어요
    }

    @Override
    public List<ChartResponse> getDauChart() {
        List<ChartResponse> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 오늘 ~ 6일 전까지 날짜 리스트 만들기
        for (int i = 1; i <= 7; i++) {
            List<LocalDate> targets = new ArrayList<>();
            targets.add(today.minusDays(i));

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

            List<ChartResponse> oneRow = rows.stream()
                    .map(t -> new ChartResponse(
                            t.get(gaUserStat.statDate).format(fmt),   // xLabels
                            String.valueOf(t.get(sumEventCount))        // value
                    ))
                    .toList();

            if (ObjectUtils.isEmpty(oneRow)) {
                result.add(new ChartResponse(today.minusDays(i).format(fmt)
                        , String.valueOf(0)));
            } else {
                result.addAll(oneRow);
            }


        }
        return result;
    }
}
