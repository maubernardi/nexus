package it.nexus.domain.id;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.LongStream;

import org.hibernate.generator.EventType;
import org.junit.jupiter.api.Test;

import io.hypersistence.tsid.TSID;

class TsidIdGeneratorTest {

    private final TsidIdGenerator generator = new TsidIdGenerator();

    @Test
    void generate_returnsStrictlyIncreasingIds_inSingleThread() {
        long[] ids = LongStream.range(0, 10_000)
                .map(i -> (long) generator.generate(null, null, null, EventType.INSERT))
                .toArray();

        for (int i = 1; i < ids.length; i++) {
            assertThat(ids[i]).isGreaterThan(ids[i - 1]);
        }
    }

    @Test
    void generate_returnsUniqueIds_acrossThreads() throws Exception {
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        int threads = 8;
        int perThread = 5_000;
        try (var executor = Executors.newFixedThreadPool(threads)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                futures.add(executor.submit(() -> {
                    for (int i = 0; i < perThread; i++) {
                        ids.add(TsidIdGenerator.nextId());
                    }
                }));
            }
            for (var future : futures) {
                future.get();
            }
        }

        assertThat(ids).hasSize(threads * perThread);
    }

    @Test
    void generatedId_hasThirteenCharExternalFormAndRoundTrips() {
        long id = TsidIdGenerator.nextId();

        String external = TSID.from(id).toString();

        assertThat(external).hasSize(13);
        assertThat(TSID.from(external).toLong()).isEqualTo(id);
    }

    @Test
    void generator_runsOnInsertOnly() {
        assertThat(generator.getEventTypes()).containsExactly(EventType.INSERT);
    }
}
