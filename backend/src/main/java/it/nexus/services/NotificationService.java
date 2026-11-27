package it.nexus.services;

import it.nexus.domain.AppUser;
import it.nexus.domain.Company;
import it.nexus.domain.ContactRequest;
import it.nexus.domain.Ticket;
import it.nexus.domain.dto.NotificationsDTO;
import it.nexus.domain.enumeration.NotificationType;

public interface NotificationService {

    /** Avvisa il destinatario; nessuna notifica se manca (es. segnalazione non ancora in carico) o se è chi agisce. */
    default void notify(AppUser recipient, NotificationType type, Ticket ticket) {
        notify(recipient, type, ticket, null);
    }

    /** Come {@link #notify(AppUser, NotificationType, Ticket)}, con l'avviso che porta alla scheda dell'azienda (US-515). */
    void notify(AppUser recipient, NotificationType type, Ticket ticket, Company company);

    /** Avviso che porta alla richiesta di contatto (US-512), con numero, esito e nota nei testi. */
    void notify(AppUser recipient, NotificationType type, ContactRequest contactRequest);

    /** Le notifiche più recenti dell'utente corrente e il numero delle non lette. */
    NotificationsDTO mine();

    /** Numero delle non lette dell'utente corrente (per la campanella). */
    long unreadCount();

    void markRead(String notificationId);

    void markAllRead();
}
