package it.nexus.jobs;

import java.time.Clock;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import it.nexus.services.TimerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Controllo orario del timer delle proposte accettate (US-704): orario in {@code nexus.timer.cron}, ora di Roma. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimerJob {

    private final TimerService timerService;
    private final Clock clock;

    @Scheduled(cron = "${nexus.timer.cron}", zone = "Europe/Rome")
    public void run() {
        int reminders = timerService.sendDueReminders(clock.instant());
        if (reminders > 0) {
            log.info("Timer delle proposte: {} promemoria inviati", reminders);
        }
    }
}
