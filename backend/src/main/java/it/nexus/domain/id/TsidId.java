package it.nexus.domain.id;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.annotations.IdGeneratorType;

/**
 * Chiave primaria TSID (intero a 64 bit ordinato nel tempo), generata dall'applicazione prima dell'INSERT.
 * Verso l'esterno va esposta come stringa di 13 caratteri ({@link io.hypersistence.tsid.TSID#toString()}),
 * mai come numero: JavaScript non rappresenta interi oltre 2^53.
 */
@IdGeneratorType(TsidIdGenerator.class)
@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface TsidId {
}
