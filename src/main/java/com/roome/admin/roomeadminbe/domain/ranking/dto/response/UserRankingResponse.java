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
    private int rank;             // 순위
    private Long userId;          // 사용자 ID
    private String nickname;      // 닉네임
    private String profileImage;  // 프로필 이미지 URL
    private int score;            // 점수 (API 요청 수 등)
    private boolean isTopRank;    // 상위 랭커 여부(1~3위 true)
}
