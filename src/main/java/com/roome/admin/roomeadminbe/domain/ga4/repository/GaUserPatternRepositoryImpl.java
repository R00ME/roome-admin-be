package com.roome.admin.roomeadminbe.domain.ga4.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roome.admin.roomeadminbe.domain.ga4.dto.response.UserPatternResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.roome.admin.roomeadminbe.domain.ga4.entity.QGaUserPattern.gaUserPattern;

@RequiredArgsConstructor
public class GaUserPatternRepositoryImpl implements GaUserPatternRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<UserPatternResponse> getUserFeatureUsage(String userId) {
        return jpaQueryFactory
                .select(Projections.constructor(UserPatternResponse.class,
                        gaUserPattern.customUserId,
                        gaUserPattern.featureName,
                        gaUserPattern.eventCount.sum(),
                        gaUserPattern.totalDuration.sum(),
                        Expressions.constant((String) null) // 응답에 필요한 값 -> 계산 후 return 할 때 필요(서비스 처리)
                ))
                .from(gaUserPattern)
                .where(
                        gaUserPattern.customUserId.eq(userId),
                        gaUserPattern.featureName.isNotNull()
                )
                .groupBy(gaUserPattern.customUserId, gaUserPattern.featureName)
                .fetch();
    }
}
