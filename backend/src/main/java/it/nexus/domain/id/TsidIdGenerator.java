package it.nexus.domain.id;

import java.util.EnumSet;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;

import io.hypersistence.tsid.TSID;

/**
 * Generatore Hibernate per {@link TsidId}. Usa la factory di default di hypersistence-tsid, thread-safe e
 * monotona: il nodo si configura con la system property {@code tsid.node} o la variabile {@code TSID_NODE}
 * (necessario con più istanze del backend), altrimenti è casuale.
 */
public class TsidIdGenerator implements BeforeExecutionGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue,
            EventType eventType) {
        return nextId();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }

    static long nextId() {
        return TSID.Factory.getTsid().toLong();
    }
}
