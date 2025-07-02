package com.roome.admin.roomeadminbe.domain.admin.dto.response;

import com.roome.admin.roomeadminbe.domain.common.util.PagingUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AdminListResponse {
    private List<AdminResponse> content;
    private PagingUtil paging;

    public static AdminListResponse from(Page<AdminResponse> page) {
        return AdminListResponse.builder()
                .content(page.getContent())
                .paging(PagingUtil.from(page))
                .build();
    }
}
