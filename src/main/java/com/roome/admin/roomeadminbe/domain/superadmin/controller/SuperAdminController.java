package com.roome.admin.roomeadminbe.domain.superadmin.controller;

import com.roome.admin.roomeadminbe.domain.admin.dto.request.AdminListRequest;
import com.roome.admin.roomeadminbe.domain.admin.dto.response.AdminListResponse;
import com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse;
import com.roome.admin.roomeadminbe.domain.superadmin.dto.request.InviteAdminRequest;
import com.roome.admin.roomeadminbe.domain.superadmin.service.SuperAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse.ofDataWithHttpStatus;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/super")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/invite")
    public ResponseEntity<CommonResponse<String>> inviteAdmin(@AuthenticationPrincipal UserDetails userDetails, @RequestBody @Validated InviteAdminRequest inviteAdminRequestDto) {
        superAdminService.inviteAdmin(inviteAdminRequestDto);
        return ofDataWithHttpStatus("관리자 초대 완료", HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/admins")
    public ResponseEntity<CommonResponse<AdminListResponse>> getAdminList(@AuthenticationPrincipal UserDetails userDetails, @ModelAttribute AdminListRequest adminListRequest) {
        AdminListResponse adminListResponse = superAdminService.getAdminList(adminListRequest);
        return ofDataWithHttpStatus(adminListResponse, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/admins/{adminId}/delete")
    public ResponseEntity<CommonResponse<String>> deleteAdminRole(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("adminId") Long adminId) {
        superAdminService.deleteAdminRole(adminId);
        return ofDataWithHttpStatus("관리자 권한 회수 완료", HttpStatus.OK);
    }
}
