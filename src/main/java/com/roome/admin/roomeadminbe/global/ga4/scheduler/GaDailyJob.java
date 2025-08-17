package com.roome.admin.roomeadminbe.global.ga4.scheduler;

import com.roome.admin.roomeadminbe.global.ga4.service.GaIngestService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class GaDailyJob {

    private final GaIngestService ingest;

    // 매일 02:15 KST 전일 데이터 수집
    @Scheduled(cron = "0 15 2 * * *", zone = "Asia/Seoul")
    public void collectYesterday() {
        LocalDate y = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
        ingest.upsertDay(y);
    }

    // 매일 02:25 KST 최근 3일 재수집(보고 지연 대비, 업서트로 안전)
    @Scheduled(cron = "0 25 2 * * *", zone = "Asia/Seoul")
    public void reCollectLast3Days() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        for (int i = 1; i <= 3; i++) {
            ingest.upsertDay(today.minusDays(i));
        }
    }
}
