package com.roome.admin.roomeadminbe.domain.admin.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roome.admin.roomeadminbe.domain.admin.dto.request.AdminListRequest;
import com.roome.admin.roomeadminbe.domain.admin.dto.response.AdminResponse;
import com.roome.admin.roomeadminbe.domain.admin.entity.ActivationStatus;
import com.roome.admin.roomeadminbe.domain.admin.entity.Admin;
import com.roome.admin.roomeadminbe.domain.admin.entity.AdminRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.roome.admin.roomeadminbe.domain.admin.entity.QAdmin.admin;
import static org.springframework.util.ObjectUtils.isEmpty;

@RequiredArgsConstructor
public class AdminRepositoryImpl implements AdminRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<AdminResponse> findAll(AdminListRequest adminListRequest, Pageable pageable) {
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(pageable);

        List<AdminResponse> list = jpaQueryFactory
                .select(Projections.constructor(AdminResponse.class,
                        admin.adminId,
                        admin.adminName,
                        admin.adminEmail,
                        admin.adminRole))
                .from(admin)
                .where(admin.activationStatus.eq(ActivationStatus.ACTIVE))
                .where(adminRoleEq(adminListRequest.getRole()))
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = jpaQueryFactory
                .select(admin.count())
                .from(admin)
                .fetchOne();

        return new PageImpl<>(list, pageable, count);
    }

    private BooleanExpression adminRoleEq(AdminRole adminRole) {
        return isEmpty(adminRole) ? null : admin.adminRole.eq(adminRole);
    }

    private OrderSpecifier<?> getOrderSpecifier(Pageable pageable) {
        if (pageable.getSort().isEmpty()) {
            return admin.createdAt.desc(); // 기본값
        }

        Sort.Order sortOrder = pageable.getSort().iterator().next();
        Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;
        PathBuilder<?> entityPath = new PathBuilder<>(Admin.class, "admin");

        return switch (sortOrder.getProperty()) {
            case "adminName" -> new OrderSpecifier<>(direction, admin.adminName);
            case "createdAt" -> // 가입순
                    new OrderSpecifier<>(direction, admin.createdAt);
            default -> new OrderSpecifier<>(Order.DESC, admin.createdAt);
        };
    }
}
