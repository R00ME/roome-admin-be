package com.roome.admin.roomeadminbe.domain.ga4.dto.response;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ChartResponse {
    private String xLabels;
    private String value;

    public ChartResponse(String xLabels, String value) {
        this.xLabels = xLabels;
        this.value = value;
    }
}
