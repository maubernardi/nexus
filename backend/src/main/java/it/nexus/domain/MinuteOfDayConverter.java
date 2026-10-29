package it.nexus.domain;

import java.time.LocalTime;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Orario del giorno salvato come minuti dalla mezzanotte ({@code smallint}). Una colonna {@code time} subirebbe la
 * conversione di fuso di {@code hibernate.jdbc.time_zone: UTC} (pensata per gli istanti): 9:00 diventerebbe 8:00 o 7:00
 * a seconda dell'ora legale. Con i minuti l'orario resta quello scritto (US-1002).
 */
@Converter
public class MinuteOfDayConverter implements AttributeConverter<LocalTime, Short> {

    @Override
    public Short convertToDatabaseColumn(LocalTime time) {
        return time == null ? null : (short) (time.getHour() * 60 + time.getMinute());
    }

    @Override
    public LocalTime convertToEntityAttribute(Short minutes) {
        return minutes == null ? null : LocalTime.of(minutes / 60, minutes % 60);
    }
}
