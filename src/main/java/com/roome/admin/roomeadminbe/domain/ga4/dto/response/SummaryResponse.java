package com.roome.admin.roomeadminbe.domain.ga4.dto.response;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SummaryResponse {
    private String label;
    private String value;
    private String unit;
    private String changeRate;

    public SummaryResponse(String label, String value, String unit, String changeRate) {
        this.label = label;
        this.value = value;
        this.unit = unit;
        this.changeRate = changeRate;
    }
}
