package com.roome.admin.roomeadminbe.domain.admin.repository;

import com.roome.admin.roomeadminbe.domain.admin.dto.request.AdminListRequest;
import com.roome.admin.roomeadminbe.domain.admin.dto.response.AdminResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminRepositoryCustom {

    Page<AdminResponse> findAll(AdminListRequest adminListRequest, Pageable pageable);
}
