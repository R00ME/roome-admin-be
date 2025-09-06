package com.roome.admin.roomeadminbe.domain.ga4.scheduler;

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

    // 매일 새벽 2시(KST) 전일 데이터 수집
    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Seoul")
    public void collectYesterday() {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
        gaEventService.collectDailyEvents(yesterday);
    }
}
