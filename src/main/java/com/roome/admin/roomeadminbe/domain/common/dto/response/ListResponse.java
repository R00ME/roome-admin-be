package com.roome.admin.roomeadminbe.domain.common.dto.response;

import com.roome.admin.roomeadminbe.domain.common.util.PagingUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ListResponse<T> {
    private PagingUtil pagingUtil;
    private List<T> content;

    public static <T> ListResponse<T> from(Page<T> page) {
        return ListResponse.<T>builder()
                .pagingUtil(PagingUtil.from(page))
                .content(page.getContent())
                .build();
    }
}
