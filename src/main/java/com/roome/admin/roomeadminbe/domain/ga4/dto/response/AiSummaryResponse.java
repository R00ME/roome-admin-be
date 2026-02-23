package com.roome.admin.roomeadminbe.domain.ga4.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class AiSummaryResponse {
    public String mostUsedFeature;
    public String mostDroppedFeature;
    public String mostEntryPath;

    public AiSummaryResponse(String mostUsedFeature, String mostDroppedFeature, String mostEntryPath) {
        this.mostUsedFeature = mostUsedFeature;
        this.mostDroppedFeature = mostDroppedFeature;
        this.mostEntryPath = mostEntryPath;
    }
}
