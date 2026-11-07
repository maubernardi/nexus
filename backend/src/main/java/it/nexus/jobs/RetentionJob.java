package it.nexus.jobs;

import java.time.Clock;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import it.nexus.services.BeneficiaryRetentionService;
import lombok.RequiredArgsConstructor;

/**
 * Controllo notturno della conservazione dei dati dei beneficiari (US-1303): orario in {@code nexus.retention.cron}, ora
 * di Roma. Segna soltanto: l'anonimizzazione la conferma una persona (decisione del PO, 07/11).
 */
@Component
@RequiredArgsConstructor
public class RetentionJob {

    private final BeneficiaryRetentionService beneficiaryRetentionService;
    private final Clock clock;

    @Scheduled(cron = "${nexus.retention.cron}", zone = "Europe/Rome")
    public void run() {
        beneficiaryRetentionService.review(clock.instant());
    }
}
