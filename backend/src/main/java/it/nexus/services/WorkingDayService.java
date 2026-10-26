package it.nexus.services;

import java.time.LocalDate;

/**
 * Giorni lavorativi secondo il calendario unico (US-703): non lo sono sabati, domeniche, festività nazionali, il patrono
 * e le chiusure della cooperativa.
 */
public interface WorkingDayService {

    boolean isWorkingDay(LocalDate day);

    /**
     * L'n-esimo giorno lavorativo dopo {@code from} (escluso {@code from}): con {@code days = 7} è la scadenza del timer
     * della proposta accettata (US-701), con {@code days = 4} il giorno del promemoria.
     */
    LocalDate plusWorkingDays(LocalDate from, int days);
}
