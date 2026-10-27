package it.nexus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Attività pianificate (controllo orario del timer delle proposte, US-704/705). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
