# Sprint 9 — 6 – 8 novembre 2026

**Stato:** 🔨 in corso. **Planning:** 05/11, nella [review dello Sprint 8](sprint-08-review.md) (§9); proposta del team
confermata dal Product Owner.

## Obiettivo

> L'ADMIN gestisce progetti e utenti da NEXUS, senza la console di Keycloak: crea gli utenti, li assegna ai progetti,
> li disattiva o cambia il ruolo; chi dimentica la password la reimposta da solo.

## Impegno (19 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| A16 | Ripristino del deploy e comandi di sola lettura `logs`/`status` (azione, senza punti) | — | ✅ Ready | — | — |
| US-102 | Gestione progetti | 3 | ✅ Ready | — | — |
| US-103 | Creazione utente | 8 | ✅ Ready | — | — |
| US-104 | Assegnazione utenti ai progetti | 3 | ✅ Ready | — | — |
| US-105 | Disattivazione e cambio ruolo | 3 | ✅ Ready | — | — |
| US-1308 | Recupero password | 2 | ✅ Ready | — | — |

**Extra**, nell'ordine: US-1302 *Backup del database* (5), US-205 *Curriculum del beneficiario* (5).

Ordine: A16 → US-102 → US-103 → US-104 → US-105 → US-1308. Una PR per storia; gli stati del backlog si aggiornano
nella PR di documentazione dopo i merge. Prima dello sprint, fuori piano: US-110 *Cambio del proprio indirizzo email*
(8 punti, non conteggiati nella velocity, §8.3 della review).

## Decisioni del planning (review dello Sprint 8)

- **Log del server** (§8.5): la chiave di deploy potrà chiedere anche `logs <servizio>` e `status`, in sola lettura,
  tramite un workflow lanciato a mano.
- **Email di password** (§8.7): le manda Keycloak ("imposta la password" per i nuovi utenti, "password dimenticata"),
  con lo stesso SMTP di IONOS; la password SMTP in Keycloak la inserisce il PO.
- **Username** (§8.8): lo scrive l'ADMIN, con il suggerimento `nome.cognome`; si accede anche con l'email.
- **Collaudo** (§8.6): a ogni review il PO dice quali prove ha fatto, il team le spunta.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (05/11, sera) | 19 |

## Per il PO durante lo sprint

- collaudo: T-13.4, sezioni 16–17, T-19.2;
- US-1301 (PWA su smartphone, T-10.1);
- SMTP in Keycloak, quando arriva US-103: lista dei passi nella PR.
