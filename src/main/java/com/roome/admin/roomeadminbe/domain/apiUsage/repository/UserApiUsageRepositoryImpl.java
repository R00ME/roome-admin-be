package com.roome.admin.roomeadminbe.domain.apiUsage.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.ApiUsageSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.UserMostUsedDomainSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.ApiUsageResponse;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.GetUserMostDomainResponse;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.MostUsedDomainResponse;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

//    @Override
//    public Page<GetUserMostDomainResponse> findUsersWithMostUsedDomain(UserMostUsedDomainSearchRequest request, Pageable pageable) {
//
//        List<Tuple> tuples = jpaQueryFactory
//                .select(
//                        user.id,
//                        user.email,
//                        user.nickname,
//                        user.gender,
//                        user.lastLogin,
//                        user.createdAt,
//                        userApiUsage.domain,
//                        userApiUsage.count.sum()
//                )
//                .from(user)
//                .leftJoin(userApiUsage).on(userApiUsage.userId.eq(user.id))
//                .groupBy(user.id, userApiUsage.domain)
//                .orderBy(user.id.asc(), userApiUsage.count.sum().desc()) // 유저별 domain 합계 내림차순
//                .fetch();
//
//        // 유저별 가장 많이 사용한 도메인 1개만 추리기
//        Map<Long, GetUserMostDomainResponse> topDomainByUser = new LinkedHashMap<>();
//        for (Tuple tuple : tuples) {
//            Long userId = tuple.get(user.id);
//            if (!topDomainByUser.containsKey(userId)) { // 첫 번째(=최대 count)만 저장
//                topDomainByUser.put(userId,
//                        new GetUserMostDomainResponse(
//                                new UserInfoResponse(
//                                        tuple.get(user.id),
//                                        tuple.get(user.email),
//                                        tuple.get(user.nickname),
//                                        tuple.get(user.gender),
//                                        tuple.get(user.lastLogin),
//                                        tuple.get(user.createdAt)
//                                ),
//                                new MostUsedDomainResponse(
//                                        tuple.get(userApiUsage.domain),
//                                        tuple.get(userApiUsage.count.sum())
//                                )
//                        )
//                );
//            }
//        }
//
//        // 최종 결과 리스트
//        List<GetUserMostDomainResponse> content = new ArrayList<>(topDomainByUser.values());
//
//        // 페이지네이션 적용
//        int start = (int) pageable.getOffset();
//        int end = Math.min((start + pageable.getPageSize()), content.size());
//        List<GetUserMostDomainResponse> pagedContent =
//                content.subList(start, end);
//
//        return new PageImpl<>(pagedContent, pageable, content.size());
//    }

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
