package it.nexus.jobs;

import java.time.Clock;
import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import it.nexus.services.TimerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controllo orario del timer delle proposte accettate (US-704/705): orario in {@code nexus.timer.cron}, ora di Roma.
 * Prima le scadenze, poi i promemoria: una segnalazione già scaduta non riceve il promemoria.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimerJob {

    private final TimerService timerService;
    private final Clock clock;

    @Scheduled(cron = "${nexus.timer.cron}", zone = "Europe/Rome")
    public void run() {
        Instant now = clock.instant();
        int expired = timerService.expireDue(now);
        int reminders = timerService.sendDueReminders(now);
        if (expired + reminders > 0) {
            log.info("Timer delle proposte: {} scaduti, {} promemoria inviati", expired, reminders);
        }
    }
}
