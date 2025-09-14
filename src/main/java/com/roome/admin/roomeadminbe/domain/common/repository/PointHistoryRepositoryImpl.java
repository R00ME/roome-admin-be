package com.roome.admin.roomeadminbe.domain.common.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.PointTrendResponse;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.roome.admin.roomeadminbe.domain.common.entity.QPointHistory.pointHistory;

@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<PointTrendResponse> getUserPointTrend(Long userId) {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);

        // MySQL 기준: %Y-%m → "2023-07"
        DateTemplate<String> month = Expressions.dateTemplate(
                String.class,
                "DATE_FORMAT({0}, {1})",
                pointHistory.createdAt,
                ConstantImpl.create("%Y-%m")
        );

        NumberExpression<Integer> earnedExpr = new CaseBuilder()
                .when(pointHistory.amount.gt(0)).then(pointHistory.amount)
                .otherwise(0).sum();

        NumberExpression<Integer> usedExpr = new CaseBuilder()
                .when(pointHistory.amount.lt(0)).then(pointHistory.amount.abs())
                .otherwise(0).sum();

        List<Tuple> results = jpaQueryFactory
                .select(month, earnedExpr, usedExpr)
                .from(pointHistory)
                .where(
                        pointHistory.user.id.eq(userId),
                        pointHistory.createdAt.goe(sixMonthsAgo)
                )
                .groupBy(month)
                .orderBy(month.asc())
                .fetch();

        return results.stream()
                .map(r -> new PointTrendResponse(
                        r.get(month),
                        r.get(earnedExpr),
                        r.get(usedExpr)
                ))
                .toList();
    }
}
