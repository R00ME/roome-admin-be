package com.roome.admin.roomeadminbe.domain.ga4.scheduler;

import com.roome.admin.roomeadminbe.domain.ga4.service.GaEventCollectorService;
import com.roome.admin.roomeadminbe.domain.ga4.service.GaIngestService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class GaDailyJob {

    private final GaIngestService ingest;                // Raw 이벤트 수집
    private final GaEventCollectorService gaEventCollector; // Daily 집계 수집

    // 매일 02:15 KST 전일 데이터 수집
    @Scheduled(cron = "0 15 2 * * *", zone = "Asia/Seoul")
    public void collectYesterday() {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);

        // Daily 집계 저장
        gaEventCollector.collectDailyEvents(yesterday);

        // Raw 이벤트 저장
        ingest.ingestRawEvents(yesterday);
    }

    // 매일 02:25 KST 최근 3일 재수집(보고 지연 대비, 업서트로 안전)
    @Scheduled(cron = "0 25 2 * * *", zone = "Asia/Seoul")
    public void reCollectLast3Days() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        for (int i = 1; i <= 3; i++) {
            LocalDate target = today.minusDays(i);

            // Daily 재집계
            gaEventCollector.collectDailyEvents(target);

            // Raw 재수집
            ingest.ingestRawEvents(target);
        }
    }
}
