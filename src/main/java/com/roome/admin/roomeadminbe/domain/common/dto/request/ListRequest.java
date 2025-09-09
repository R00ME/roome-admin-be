package com.roome.admin.roomeadminbe.domain.common.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ListRequest {

    @Builder.Default
    private Integer page = 1; // 사용자는 1부터 시작한다고 가정

    @Builder.Default
    private Integer pageSize = 10;

    @Builder.Default
    private Sort.Direction sortDirection = Sort.Direction.DESC;

    private String column;

    public Pageable toPageable() {
        int safePage = (page != null && page > 0) ? page - 1 : 0;
        String sortBy = (column != null && !column.isBlank()) ? column : "id";
        return PageRequest.of(safePage, pageSize, sortDirection, sortBy);
    }

    public void setSortDirection(String sortDirection) {
        if ("asc".equalsIgnoreCase(sortDirection)) {
            this.sortDirection = Sort.Direction.ASC;
        } else if ("desc".equalsIgnoreCase(sortDirection)) {
            this.sortDirection = Sort.Direction.DESC;
        }
    }
}
