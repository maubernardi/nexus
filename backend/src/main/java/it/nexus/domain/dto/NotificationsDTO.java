package it.nexus.domain.dto;

import java.util.List;

/** Notifiche dell'utente (le più recenti) e numero delle non lette. */
public record NotificationsDTO(long unread, List<NotificationDTO> items) {
}
