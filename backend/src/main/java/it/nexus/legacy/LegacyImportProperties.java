package it.nexus.legacy;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Importazione dal database attuale (US-1309), con il profilo {@code legacy-import}: il file Access da leggere e, se
 * indicato, dove scrivere il resoconto (altrimenti accanto al file).
 */
@ConfigurationProperties(prefix = "nexus.legacy-import")
public record LegacyImportProperties(Path file, Path report) {
}
