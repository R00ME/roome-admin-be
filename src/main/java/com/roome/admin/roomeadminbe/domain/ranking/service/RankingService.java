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

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {


    private static final String RANKING_KEY = "user:ranking";
    private static final String PREV_KEY = "user:ranking:prev";
    @Qualifier("rankingRedisTemplate")
    private final RedisTemplate<String, String> rankingRedisTemplate;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserRankingResponse> getRankingSnapshot() {
        // 현재 TOP10
        Set<ZSetOperations.TypedTuple<String>> todayRankings =
                rankingRedisTemplate.opsForZSet().reverseRangeWithScores(RANKING_KEY, 0, 9);

        // 이전 TOP10
        Set<ZSetOperations.TypedTuple<String>> prevRankings =
                rankingRedisTemplate.opsForZSet().reverseRangeWithScores(PREV_KEY, 0, 9);

        if (todayRankings == null || todayRankings.isEmpty()) {
            return Collections.emptyList();
        }

        // 이전 순위 맵핑
        Map<Long, Integer> prevRankMap = new HashMap<>();
        Map<Long, Integer> prevScoreMap = new HashMap<>();
        if (prevRankings != null) {
            int idx = 1;
            for (ZSetOperations.TypedTuple<String> tuple : prevRankings) {
                if (tuple.getValue() == null || tuple.getScore() == null) continue;
                prevRankMap.put(Long.valueOf(tuple.getValue()), idx++);
                prevScoreMap.put(Long.valueOf(tuple.getValue()), tuple.getScore().intValue());
            }
        }

        // 현재 순위 계산
        List<UserRankingResponse> items = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : todayRankings) {
            if (tuple.getValue() == null || tuple.getScore() == null) continue;

            Long userId = Long.valueOf(tuple.getValue());
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) continue;

            int score = tuple.getScore().intValue();

            // 이전 데이터 비교
            Integer prevRank = prevRankMap.get(userId);
            Integer prevScore = prevScoreMap.get(userId);

            Integer rankDiff = null;
            Integer scoreDiff = null;
            String status;

            if (prevRank == null) {
                status = "NEW"; // 신규 진입
            } else {
                rankDiff = prevRank - rank; // +값이면 순위 상승
                scoreDiff = score - prevScore;
                if (rankDiff > 0) status = "UP";
                else if (rankDiff < 0) status = "DOWN";
                else status = "SAME";
            }

            items.add(UserRankingResponse.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .profileImage(user.getProfileImage())
                    .score(score)
                    .rank(rank)
                    .rankDiff(rankDiff)
                    .scoreDiff(scoreDiff)
                    .status(status)
                    .isTopRank(rank <= 3)
                    .build());

            rank++;
        }

        return items;
    }
}
