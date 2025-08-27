package com.roome.admin.roomeadminbe.domain.ranking.service;

import com.roome.admin.roomeadminbe.domain.common.entity.User;
import com.roome.admin.roomeadminbe.domain.common.repository.UserRepository;
import com.roome.admin.roomeadminbe.domain.ranking.dto.response.UserRankingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private static final String RANKING_KEY = "user:ranking";

    @Qualifier("rankingRedisTemplate")
    private final RedisTemplate<String, String> rankingRedisTemplate;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserRankingResponse> getTopRankings() {
        // Top10 ZSet 조회 (높은 점수 순서대로)
        Set<ZSetOperations.TypedTuple<String>> rankSet = rankingRedisTemplate.opsForZSet()
                .reverseRangeWithScores(RANKING_KEY, 0, 9);

        List<UserRankingResponse> validRankings = new ArrayList<>();

        if (rankSet == null || rankSet.isEmpty()) {
            return validRankings;
        }

        int rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : rankSet) {
            String userIdStr = tuple.getValue();
            Double score = tuple.getScore();

            if (userIdStr == null || score == null) continue;

            try {
                Long userId = Long.valueOf(userIdStr);
                User user = userRepository.findById(userId).orElse(null);

                if (user == null) {
                    // 탈퇴 유저는 제거
                    rankingRedisTemplate.opsForZSet().remove(RANKING_KEY, userIdStr);
                    continue;
                }

                validRankings.add(UserRankingResponse.builder()
                        .rank(rank)
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .profileImage(user.getProfileImage())
                        .score(score.intValue())
                        .isTopRank(rank <= 3)
                        .build());

                rank++;
            } catch (NumberFormatException e) {
                log.warn("랭킹 userId 변환 오류: {}", userIdStr);
            }
        }

        return validRankings;
    }
}
