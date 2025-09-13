package com.roome.admin.roomeadminbe.domain.ga4.repository;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roome.admin.roomeadminbe.domain.ga4.dto.response.UserPatternResponse;
import com.roome.admin.roomeadminbe.global.exception.BusinessException;
import com.roome.admin.roomeadminbe.global.exception.enumeration.ErrorCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.roome.admin.roomeadminbe.domain.ga4.entity.QGaUserPattern.gaUserPattern;

@RequiredArgsConstructor
public class GaUserPatternRepositoryImpl implements GaUserPatternRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<UserPatternResponse> getUserFeatureUsage(String userId) {
        try {
            return jpaQueryFactory
                    .select(Projections.fields(UserPatternResponse.class,
                            gaUserPattern.customUserId.as("customUserId"),
                            gaUserPattern.featureName.as("featureName"),
                            gaUserPattern.eventCount.sum().as("eventCount"),
                            gaUserPattern.totalDuration.sum().as("usageTimeSec"),
                            ExpressionUtils.as(Expressions.constant(""), "usageTime") // ⭐ 여기!
                    ))
                    .from(gaUserPattern)
                    .where(
                            gaUserPattern.customUserId.eq(userId),
                            gaUserPattern.featureName.isNotNull()
                    )
                    .groupBy(gaUserPattern.customUserId, gaUserPattern.featureName)
                    .fetch();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.DATABASE_QUERY_FAILED);
        }
    }
}
