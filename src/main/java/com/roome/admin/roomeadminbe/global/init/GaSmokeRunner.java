package com.roome.admin.roomeadminbe.global.init;

import com.roome.admin.roomeadminbe.domain.ga4.service.GaEventCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
class GaSmokeRunner implements CommandLineRunner {
    private final GaEventCollectorService gaEventCollector;

    public void run(String... args) {
        gaEventCollector.collectDailyEvents(LocalDate.now().minusDays(30));
    }
}
