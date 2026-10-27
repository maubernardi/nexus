package it.nexus.services;

import it.nexus.domain.AppUser;
import it.nexus.domain.Ticket;
import it.nexus.domain.dto.NotificationsDTO;
import it.nexus.domain.enumeration.NotificationType;

public interface NotificationService {

    /** Avvisa il destinatario; nessuna notifica se manca (es. segnalazione non ancora in carico) o se è chi agisce. */
    void notify(AppUser recipient, NotificationType type, Ticket ticket);

    /** Le notifiche più recenti dell'utente corrente e il numero delle non lette. */
    NotificationsDTO mine();

    /** Numero delle non lette dell'utente corrente (per la campanella). */
    long unreadCount();

    void markRead(String notificationId);

    void markAllRead();
}
