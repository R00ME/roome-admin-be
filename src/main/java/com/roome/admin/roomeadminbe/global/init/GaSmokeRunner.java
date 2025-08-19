package com.roome.admin.roomeadminbe.global.init;

import com.roome.admin.roomeadminbe.domain.ga4.service.GaIngestService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
class GaSmokeRunner implements CommandLineRunner {
    private final GaIngestService ingest;
    public void run(String... args) { ingest.upsertDay(LocalDate.now().minusDays(30)); }
}
