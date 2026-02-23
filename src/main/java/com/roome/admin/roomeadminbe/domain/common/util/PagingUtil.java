package com.roome.admin.roomeadminbe.domain.common.util;

import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class PagingUtil {

    private final long totalElements;
    private final int totalPages;
    private final int pageNumber;
    private final int pageSize;
    private final int totalPageGroups;
    private final int pageGroupSize = 5;
    private final int pageGroup;
    private final int startPage;
    private final int endPage;
    private final boolean existPrePageGroup;
    private final boolean existNextPageGroup;

    private PagingUtil(long totalElements, int totalPages, int pageNumber, int pageSize) {
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.pageNumber = pageNumber + 1;
        this.pageSize = pageSize;
        this.totalPageGroups = calculateTotalPageGroups();
        this.pageGroup = calculatePageGroup();
        this.startPage = calculateStartPage();
        this.endPage = calculateEndPage();
        this.existPrePageGroup = pageGroup > 1;
        this.existNextPageGroup = pageGroup < totalPageGroups;
    }

    public static PagingUtil from(Page<?> page) {
        return new PagingUtil(
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }

    private int calculateTotalPageGroups() {
        return (int) Math.ceil((double) totalPages / pageGroupSize);
    }

    private int calculatePageGroup() {
        return (int) Math.ceil((double) pageNumber / pageGroupSize);
    }

    private int calculateStartPage() {
        return (pageGroup - 1) * pageGroupSize + 1;
    }

    private int calculateEndPage() {
        return Math.min(pageGroup * pageGroupSize, totalPages);
    }
}
