package com.roome.admin.roomeadminbe.global.ga4.controller;

import com.roome.admin.roomeadminbe.global.ga4.dto.EventDailyDto;
import com.roome.admin.roomeadminbe.global.ga4.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/events/daily")
    public Page<EventDailyDto> list(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required=false) String eventName,
            @RequestParam(required=false) String eventCategory,
            @RequestParam(required=false) String featureName,
            @RequestParam(required=false) String userId,
            Pageable pageable // ?page=0&size=20&sort=statDate,asc&sort=eventName,asc
    ) {
        return analyticsService.getDailyPage(
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                eventName, eventCategory, featureName, userId,
                pageable
        );
    }

    @GetMapping("/events/daily/sum")
    public List<EventDailyDto> sum(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required=false) String eventName,
            @RequestParam(required=false) String featureName
    ) {
        return analyticsService.getDailySums(
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                eventName, featureName
        );
    }
}
