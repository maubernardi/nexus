# Sprint 9 — 6 – 8 novembre 2026

**Stato:** 🔨 in corso. **Planning:** 05/11, nella [review dello Sprint 8](sprint-08-review.md) (§9); proposta del team
confermata dal Product Owner.

## Obiettivo

> L'ADMIN gestisce progetti e utenti da NEXUS, senza la console di Keycloak: crea gli utenti, li assegna ai progetti,
> sposta le segnalazioni di un Tutor su un collega, li disattiva o cambia il ruolo; i Coordinatori riorganizzano le
> segnalazioni dei loro progetti.

## Impegno (25 punti, ripianificato il 06/11)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| A16 | Ripristino del deploy e comandi di sola lettura `logs`/`status` (azione, senza punti) | — | ✅ Ready | — | — |
| US-102 | Gestione progetti | 3 | ✅ Ready | — | — |
| US-103 | Creazione utente | 8 | ✅ Ready | — | — |
| US-104 | Assegnazione utenti ai progetti | 3 | ✅ Ready | — | — |
| US-111 | Spostamento delle segnalazioni tra Tutor | 5 | ✅ Ready | — | — |
| US-105 | Disattivazione e cambio ruolo | 3 | ✅ Ready | — | — |
| US-112 | Coordinatore di progetto | 3 | ✅ Ready | — | — |

**Extra**, nell'ordine: US-1302 *Backup del database* (5), US-205 *Curriculum del beneficiario* (5).

Ordine: A16 → US-102 → US-103 → US-104 → US-111 → US-105 → US-112. Una PR per storia; gli stati del backlog si aggiornano
nella PR di documentazione dopo i merge. Prima dello sprint, fuori piano: US-110 *Cambio del proprio indirizzo email*
(8 punti, non conteggiati nella velocity, §8.3 della review).

## Ripianificazione del 06/11

Definendo US-105 il PO ha deciso che un Tutor con segnalazioni aperte non si disattiva: prima le segnalazioni passano
a un altro Tutor attivo dello stesso progetto. Ne nascono due storie, entrate nello sprint su decisione del PO:

- **US-111** *Spostamento delle segnalazioni tra Tutor* (5): lo fa l'ADMIN; con la segnalazione passa anche il
  beneficiario, con tirocinio e documenti.
- **US-112** *Coordinatore di progetto* (3): un Tutor segnato dall'ADMIN come Coordinatore di un progetto sposta le
  segnalazioni di quel progetto. Si definisce per progetto, senza un nuovo ruolo in Keycloak.

Altre decisioni su US-105: un operatore del Call Center con ticket in carico si può disattivare dopo un avviso, e i
ticket tornano nella coda da prendere in carico. **US-1308** *Recupero password* (2) passa allo Sprint 10. L'impegno
sale da 19 a 25 punti, sopra la media: se non si chiude, US-112 slitta allo Sprint 10.

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
| Ripianificazione (06/11): +US-111, +US-112, −US-1308; fatte US-102, US-103, US-104 | 11 |

## Per il PO durante lo sprint

- collaudo: T-13.4, sezioni 16–17, T-19.2;
- US-1301 (PWA su smartphone, T-10.1);
- SMTP in Keycloak, quando arriva US-103: lista dei passi nella PR.
