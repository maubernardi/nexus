package it.nexus.jobs;

import java.time.Clock;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import it.nexus.services.InternshipEndReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Avvisi di fine tirocinio (US-1203): ogni mattina, orario in {@code nexus.internship-end-reminders.cron}, ora di Roma.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InternshipEndReminderJob {

    private final InternshipEndReminderService internshipEndReminderService;
    private final Clock clock;

    @Scheduled(cron = "${nexus.internship-end-reminders.cron}", zone = "Europe/Rome")
    public void run() {
        int sent = internshipEndReminderService.sendDue(clock.instant());
        if (sent > 0) {
            log.info("Avvisi di fine tirocinio: {} inviati", sent);
        }
    }
}
