package com.roome.admin.roomeadminbe.domain.apiUsage.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.ApiUsageSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.ApiUsageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static com.roome.admin.roomeadminbe.domain.apiUsage.entity.QUserApiUsage.userApiUsage;

@RequiredArgsConstructor
public class UserApiUsageRepositoryImpl implements UserApiUsageRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ApiUsageResponse> findAll(ApiUsageSearchRequest apiUsageSearchRequest, Pageable pageable) {
        List<ApiUsageResponse> list = jpaQueryFactory
                .select(Projections.constructor(ApiUsageResponse.class,
                        userApiUsage.userId,
                        userApiUsage.domain,
                        userApiUsage.apiUri,
                        userApiUsage.count,
                        userApiUsage.date
                ))
                .from(userApiUsage)
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
                .select(userApiUsage.count())
                .from(userApiUsage)
                .where(
                        userEq(apiUsageSearchRequest.getUserId()),
                        domainEq(apiUsageSearchRequest.getDomain()),
                        apiUriEq(apiUsageSearchRequest.getApiUri()),
                        dateBetween(apiUsageSearchRequest.getStartDate(), apiUsageSearchRequest.getEndDate())
                )
                .fetchOne();

        return new PageImpl<>(list, pageable, count != null ? count : 0);
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
