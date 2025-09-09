package com.roome.admin.roomeadminbe.domain.ga4.scheduler;

import com.roome.admin.roomeadminbe.domain.ga4.service.GaAggregationService;
import com.roome.admin.roomeadminbe.domain.ga4.service.GaEventCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class GaDailyJob {

    private final GaEventCollectorService gaEventService;
    private final GaAggregationService aggregationService;

    // 매일 새벽 2시(KST) 전일 데이터 수집
    @Scheduled(cron = "0 50 5 * * *", zone = "Asia/Seoul")
    public void collectYesterday() {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
        gaEventService.collectDailyEvents(yesterday);
    }

    // 매일 새벽 3시 KST, 전일 데이터 집계
    @Scheduled(cron = "0 55 5 * * *", zone = "Asia/Seoul")
    public void aggregateYesterday() {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
        aggregationService.aggregate(yesterday);
    }
}
