package com.roome.admin.roomeadminbe.domain.admin.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateAdminInfoRequest {

    private String username;
    private String phoneNumber;
}
