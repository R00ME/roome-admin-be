package com.roome.admin.roomeadminbe.domain.ga4.scheduler;

import com.roome.admin.roomeadminbe.domain.ga4.service.GaEventCollectorService;
import com.roome.admin.roomeadminbe.domain.ga4.service.GaIngestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class GaDailyJob {

    private final GaIngestService ingest;                // Raw 이벤트 수집
    private final GaEventCollectorService gaEventCollector; // Daily 집계 수집

    // 매일 07:00 KST 전일 데이터 수집
    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Seoul")
    public void collectYesterday() {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
        log.info("[GA Scheduler] collectYesterday 시작 - targetDate={}", yesterday);

        try {
            gaEventCollector.collectDailyEvents(yesterday);
            ingest.ingestRawEvents(yesterday);
            log.info("[GA Scheduler] collectYesterday 완료 - targetDate={}", yesterday);
        } catch (Exception e) {
            log.error("[GA Scheduler] collectYesterday 실패 - targetDate={}", yesterday, e);
        }
    }

    // 매일 07:10 KST 최근 3일 재수집(보고 지연 대비, 업서트 사용)
    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Seoul")
    public void reCollectLast3Days() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        log.info("[GA Scheduler] reCollectLast3Days 시작 - 기준일={}", today);

        for (int i = 1; i <= 3; i++) {
            LocalDate target = today.minusDays(i);
            try {
                gaEventCollector.collectDailyEvents(target);
                ingest.ingestRawEvents(target);
                log.info("[GA Scheduler] reCollectLast3Days 완료 - targetDate={}", target);
            } catch (Exception e) {
                log.error("[GA Scheduler] reCollectLast3Days 실패 - targetDate={}", target, e);
            }
        }

        log.info("[GA Scheduler] reCollectLast3Days 전체 종료 - 기준일={}", today);
    }
}
