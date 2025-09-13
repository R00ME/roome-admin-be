package com.roome.admin.roomeadminbe.domain.apiUsage.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.ApiUsageSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.ApiUsageResponse;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.DomainCountResponse;
import com.roome.admin.roomeadminbe.domain.ga4.dto.response.FeatureUsageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static com.roome.admin.roomeadminbe.domain.apiUsage.entity.QUserApiUsage.userApiUsage;
import static com.roome.admin.roomeadminbe.domain.common.entity.QUser.user;

@RequiredArgsConstructor
public class UserApiUsageRepositoryImpl implements UserApiUsageRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ApiUsageResponse> findAllBeforeDate(ApiUsageSearchRequest apiUsageSearchRequest, Pageable pageable) {
        List<ApiUsageResponse> list = jpaQueryFactory
                .select(Projections.constructor(ApiUsageResponse.class,
                        userApiUsage.userId,
                        userApiUsage.domain,
                        userApiUsage.apiUri,
                        userApiUsage.count,
                        userApiUsage.date
                ))
                .from(userApiUsage)
                .join(user).on(userApiUsage.userId.eq(user.id))
                .where(
                        userEq(apiUsageSearchRequest.getUserId()),
                        domainEq(apiUsageSearchRequest.getDomain()),
                        apiUriEq(apiUsageSearchRequest.getApiUri()),
                        dateBetween(apiUsageSearchRequest.getStartDate(), apiUsageSearchRequest.getEndDate())
                )
                .orderBy(userApiUsage.date.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = jpaQueryFactory
                .select(userApiUsage.id.count())
                .from(userApiUsage)
                .join(user).on(userApiUsage.userId.eq(user.id))
                .where(
                        userEq(apiUsageSearchRequest.getUserId()),
                        domainEq(apiUsageSearchRequest.getDomain()),
                        apiUriEq(apiUsageSearchRequest.getApiUri()),
                        dateBetween(apiUsageSearchRequest.getStartDate(), apiUsageSearchRequest.getEndDate())
                )
                .fetchOne();

        return new PageImpl<>(list, pageable, count != null ? count : 0);
    }

    public List<DomainCountResponse> findDomainCounts(Long userId, LocalDate from, LocalDate to) {
        return jpaQueryFactory
                .select(Projections.constructor(DomainCountResponse.class,
                        userApiUsage.domain,
                        userApiUsage.count.sum()
                ))
                .from(userApiUsage)
                .where(
                        userApiUsage.userId.eq(userId),
                        userApiUsage.date.between(from, to)
                )
                .groupBy(userApiUsage.domain)
                .fetch();
    }


    @Override
    public List<FeatureUsageResponse> getApiUsageByUser(Long userId) {
        return jpaQueryFactory
                .select(Projections.constructor(FeatureUsageResponse.class,
                        userApiUsage.domain,                      // feature
                        userApiUsage.count.sum().coalesce(0L),   // apiRequestCount
                        Expressions.constant(""),                 // usageTime (Service에서 세팅)
                        Expressions.constant(0L),                 // usageTimeSec → 0 기본값
                        userApiUsage.date.max(),                  // lastUsedAt
                        Expressions.constant(0L)                  // contentCount (옵션)
                ))
                .from(userApiUsage)
                .where(userApiUsage.userId.eq(userId))
                .groupBy(userApiUsage.domain)
                .fetch();
    }

    private BooleanExpression userEq(Long userId) {
        return userId != null ? userApiUsage.userId.eq(userId) : null;
    }

    private BooleanExpression domainEq(String domain) {
        return domain != null ? userApiUsage.domain.eq(domain) : null;
    }

    private BooleanExpression apiUriEq(String apiUri) {
        return apiUri != null ? userApiUsage.apiUri.eq(apiUri) : null;
    }

    private BooleanExpression dateBetween(LocalDate start, LocalDate end) {
        if (start != null && end != null) {
            return userApiUsage.date.between(start, end);
        } else if (start != null) {
            return userApiUsage.date.goe(start);
        } else if (end != null) {
            return userApiUsage.date.loe(end);
        } else {
            return null;
        }
    }
}
