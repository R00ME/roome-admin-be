package com.roome.admin.roomeadminbe.domain.ga4.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roome.admin.roomeadminbe.domain.ga4.dto.EventDailyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.roome.admin.roomeadminbe.domain.ga4.entity.QFeatureEventDaily.featureEventDaily;

@RequiredArgsConstructor
public class FeatureEventDailyRepositoryImpl implements FeatureEventDailyRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<EventDailyDto> searchDaily(LocalDate start, LocalDate end,
                                           String eventName, String eventCategory,
                                           String featureName, String userId,
                                           Pageable pageable) {

        BooleanExpression where = featureEventDaily.statDate.between(start, end)
                .and(eq(featureEventDaily.eventName, eventName))
                .and(eq(featureEventDaily.eventCategory, eventCategory))
                .and(eq(featureEventDaily.featureName, featureName))
                .and(eq(featureEventDaily.userId, userId));

        // content
        List<EventDailyDto> content = jpaQueryFactory.select(Projections.constructor(EventDailyDto.class,
                        featureEventDaily.statDate, featureEventDaily.eventName, featureEventDaily.eventCategory, featureEventDaily.featureName, featureEventDaily.userId,
                        featureEventDaily.eventCount, featureEventDaily.valueSum, featureEventDaily.durationMsSum))
                .from(featureEventDaily)
                .where(where)
                .orderBy(toOrderSpec(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // total
        long total = jpaQueryFactory.select(featureEventDaily.count())
                .from(featureEventDaily)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<EventDailyDto> sumByDay(LocalDate start, LocalDate end,
                                        String eventName, String featureName) {
        BooleanExpression where = featureEventDaily.statDate.between(start, end)
                .and(eq(featureEventDaily.eventName, eventName))
                .and(eq(featureEventDaily.featureName, featureName));

        // 날짜별 합계
        return jpaQueryFactory.select(Projections.constructor(EventDailyDto.class,
                        featureEventDaily.statDate,
                        featureEventDaily.eventName,
                        featureEventDaily.eventCategory,
                        featureEventDaily.featureName,
                        featureEventDaily.userId.max().nullif(""),
                        featureEventDaily.eventCount.sum(),
                        featureEventDaily.valueSum.sum(),
                        featureEventDaily.durationMsSum.sum()
                ))
                .from(featureEventDaily)
                .where(where)
                .groupBy(featureEventDaily.statDate, featureEventDaily.eventName, featureEventDaily.eventCategory, featureEventDaily.featureName)
                .orderBy(featureEventDaily.statDate.asc())
                .fetch();
    }

    // === helpers ===
    private BooleanExpression eq(StringPath path, String value) {
        return value == null ? null : path.eq(value);
    }

    private OrderSpecifier<?>[] toOrderSpec(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        if (sort == null || sort.isUnsorted()) {
            orders.add(featureEventDaily.statDate.asc());
            orders.add(featureEventDaily.eventName.asc());
            return orders.toArray(OrderSpecifier[]::new);
        }

        Map<String, Expression<? extends Comparable<?>>> columns = Map.of(
                "statDate", featureEventDaily.statDate,
                "eventName", featureEventDaily.eventName,
                "eventCategory", featureEventDaily.eventCategory,
                "featureName", featureEventDaily.featureName,
                "userId", featureEventDaily.userId,
                "eventCount", featureEventDaily.eventCount,
                "valueSum", featureEventDaily.valueSum,
                "durationMsSum", featureEventDaily.durationMsSum
        );

        for (Sort.Order o : sort) {
            Expression<? extends Comparable<?>> expr = columns.get(o.getProperty());
            if (expr == null) continue; // 허용하지 않은 정렬 필드는 무시

            orders.add(o.isAscending()
                    ? new OrderSpecifier<>(Order.ASC, expr)
                    : new OrderSpecifier<>(Order.DESC, expr));
        }

        if (orders.isEmpty()) { // 아무것도 매칭 안 됐으면 기본 정렬
            orders.add(featureEventDaily.statDate.asc());
            orders.add(featureEventDaily.eventName.asc());
        }
        return orders.toArray(OrderSpecifier[]::new);
    }
}
