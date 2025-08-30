package com.roome.admin.roomeadminbe.domain.ranking.controller;

import com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse;
import com.roome.admin.roomeadminbe.domain.ranking.dto.response.UserRankingResponse;
import com.roome.admin.roomeadminbe.domain.ranking.service.RankingService;
import com.roome.admin.roomeadminbe.global.security.model.AdminDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse.ofDataWithHttpStatus;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @PreAuthorize("hasRole('OPERATION_MANAGER')")
    @GetMapping
    public ResponseEntity<List<UserRankingResponse>> getTopRankings(@AuthenticationPrincipal AdminDetails adminDetails) {
        List<UserRankingResponse> rankings = rankingService.getRankingSnapshot();
        return ResponseEntity.ok(rankings);
    }
}
