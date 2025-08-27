//package com.roome.admin.roomeadminbe.domain.apiUsage.service;
//
//import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.UserMostUsedDomainSearchRequest;
//import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.GetUserMostDomainResponse;
//import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.UserDomainAndRankingResponse;
//import com.roome.admin.roomeadminbe.domain.common.dto.response.ListResponse;
//import com.roome.admin.roomeadminbe.domain.ranking.dto.response.UserRankingResponse;
//import com.roome.admin.roomeadminbe.domain.ranking.service.RankingService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class UserStatisticsTupleService {
//
//    private final ApiUsageService apiUsageService;
//    private final RankingService rankingService;
//
//    @Transactional(readOnly = true)
//    public UserDomainAndRankingResponse getDomainAndRanking(UserMostUsedDomainSearchRequest request) {
//        log.info("getDomainAndRanking called with request = {}", request);
//
//        // 페이지네이션 적용된 사용자별 최다 사용 도메인
//        ListResponse<GetUserMostDomainResponse> mostUsed = apiUsageService.getUsersMostUsedDomain(request);
//        log.info("mostUsed result size = {}", mostUsed.getPagingUtil().getPageSize());
//
//        // Redis ZSet에서 Top10 랭킹 조회
//        List<UserRankingResponse> topRankings = rankingService.getTopRankings();
//        log.info("topRankings size = {}", topRankings.size());
//
//        UserDomainAndRankingResponse response = UserDomainAndRankingResponse.builder()
//                .mostUsedDomains(mostUsed)
//                .topRankings(topRankings)
//                .build();
//
//        log.info("returning response = {}", response);
//
//        return response;
//    }
//}
