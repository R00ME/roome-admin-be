package com.roome.admin.roomeadminbe.domain.ga4.controller;

import com.roome.admin.roomeadminbe.domain.ga4.dto.response.AiSummaryResponse;
import com.roome.admin.roomeadminbe.domain.ga4.dto.response.ChartResponse;
import com.roome.admin.roomeadminbe.domain.ga4.dto.response.SummaryResponse;
import com.roome.admin.roomeadminbe.domain.ga4.service.GaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class GaController {
    private final GaService gaService;

    @PreAuthorize("hasAnyRole('OPERATION_MANAGER', 'SUPER_ADMIN')")
    @GetMapping("/summary")
    public ResponseEntity<List<SummaryResponse>> summary() {
        List<SummaryResponse> list = gaService.getSumaary();
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PreAuthorize("hasAnyRole('OPERATION_MANAGER', 'SUPER_ADMIN')")
    @GetMapping("/chart")
    public ResponseEntity<List<ChartResponse>> chart(@RequestParam String typeId) {
        List<ChartResponse> list = gaService.getChart(typeId);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PreAuthorize("hasAnyRole('OPERATION_MANAGER', 'SUPER_ADMIN')")
    @GetMapping("/ai-summary")
    public ResponseEntity<AiSummaryResponse> aiSummary() {
        AiSummaryResponse aiSummaryResponse = gaService.getAiSummary();
        return ResponseEntity.status(HttpStatus.OK).body(aiSummaryResponse);
    }
}
