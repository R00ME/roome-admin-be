package com.roome.admin.roomeadminbe.domain.apiUsage.dto.response;

import com.roome.admin.roomeadminbe.domain.common.dto.response.ListResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class ApiUsageListResponse extends ListResponse<ApiUsageResponse>{
}
