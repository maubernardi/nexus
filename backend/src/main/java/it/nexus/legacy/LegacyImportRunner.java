package it.nexus.legacy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Istanza dell'importazione dal database attuale (US-1309), con il profilo {@code legacy-import}: importa il file,
 * scrive il resoconto e chiude l'applicazione (0 = riuscita, 1 = fallita). Gira per ultima, dopo l'elenco dei comuni
 * della demo. Nel log solo conteggi e messaggi senza dati personali.
 */
@Slf4j
@Component
@Profile("legacy-import")
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class LegacyImportRunner implements ApplicationRunner {

    private final LegacyImportService service;
    private final LegacyImportProperties properties;
    private final ConfigurableApplicationContext context;

    @Override
    public void run(ApplicationArguments args) {
        int code = 1;
        try {
            Path file = properties.file();
            if (file == null || !Files.isReadable(file)) {
                throw new LegacyImportException("Indica il file Access leggibile in nexus.legacy-import.file");
            }
            LegacyImportReport report = service.importFile(file);
            Path out = properties.report() != null ? properties.report()
                    : file.resolveSibling(file.getFileName() + "-resoconto.txt");
            Files.writeString(out, report.toText(), StandardCharsets.UTF_8);
            log.info(report.summary());
            log.info("Resoconto dell'importazione: {}", out);
            code = 0;
        } catch (LegacyImportException e) {
            log.error("Importazione non eseguita: {}", e.getMessage());
        } catch (IOException | RuntimeException e) {
            // il tipo dell'errore e il punto del codice: il messaggio potrebbe citare dati del file
            log.error("Importazione fallita, nessun dato scritto: {} in {}", e.getClass().getSimpleName(), where(e));
        }
        int exit = code;
        System.exit(SpringApplication.exit(context, () -> exit));
    }

    /** Il primo punto del nostro codice nella pila dell'errore, oppure il primo in assoluto. */
    static String where(Throwable e) {
        StackTraceElement[] stack = e.getStackTrace();
        return Arrays.stream(stack).filter(f -> f.getClassName().startsWith("it.nexus.")).findFirst()
                .or(() -> Arrays.stream(stack).findFirst())
                .map(f -> f.getClassName() + ":" + f.getLineNumber()).orElse("?");
    }
}
