package com.roome.admin.roomeadminbe.domain.ranking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRankingResponse {
    private Long userId;
    private String nickname;
    private String profileImage;
    private Integer score;
    private Integer rank;
    private Object rankDiff;   // -1, +3, "NEW"
    private Integer scoreDiff; // 50, 32, null
    private String status;     // NORMAL, NEW
    private boolean isTopRank;
}
