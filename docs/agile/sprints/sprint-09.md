# Sprint 9 — 6 – 8 novembre 2026

**Stato:** 🎉 concluso il 06/11, in anticipo: impegno di 25 punti completato (con la ripianificazione), demo a
`e30269f`; nessun extra iniziato. **Planning:** 05/11, nella [review dello Sprint 8](sprint-08-review.md) (§9); proposta del team
confermata dal Product Owner.

## Obiettivo

> L'ADMIN gestisce progetti e utenti da NEXUS, senza la console di Keycloak: crea gli utenti, li assegna ai progetti,
> sposta le segnalazioni di un Tutor su un collega, li disattiva o cambia il ruolo; i Coordinatori riorganizzano le
> segnalazioni dei loro progetti.

## Impegno (25 punti, ripianificato il 06/11)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| A16 | Ripristino del deploy e comandi di sola lettura `logs`/`status` (azione, senza punti) | — | 🔶 In parte | — | [#100](https://github.com/maubernardi/nexus/pull/100) |
| US-102 | Gestione progetti | 3 | 🎉 Done | `add-project-catalog` | [#101](https://github.com/maubernardi/nexus/pull/101) |
| US-103 | Creazione utente | 8 | 🎉 Done | `add-user-creation` | [#102](https://github.com/maubernardi/nexus/pull/102) |
| US-104 | Assegnazione utenti ai progetti | 3 | 🎉 Done | `add-user-projects` | [#103](https://github.com/maubernardi/nexus/pull/103) |
| US-111 | Spostamento delle segnalazioni tra Tutor | 5 | 🎉 Done | `add-ticket-transfer` | [#105](https://github.com/maubernardi/nexus/pull/105) |
| US-105 | Disattivazione e cambio ruolo | 3 | 🎉 Done | `add-user-lifecycle` | [#106](https://github.com/maubernardi/nexus/pull/106) |
| US-112 | Coordinatore di progetto | 3 | 🎉 Done | `add-project-coordinator` | [#107](https://github.com/maubernardi/nexus/pull/107) |

**Velocità:** 25 punti. A16: i comandi di sola lettura sono attivi; la causa del ripristino fallito del 05/11 è ancora
da trovare e passa allo Sprint 10. Ripianificazione nella [PR #104](https://github.com/maubernardi/nexus/pull/104).

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

## Decisioni durante lo sprint

- **Disattivazione e cambio di ruolo** (US-105): l'ADMIN non tocca il proprio utente; chi diventa Tutor lascia in coda i
  ticket in carico, chi passa da Call Center ad ADMIN li tiene. Un cambio di ruolo chiude le sessioni dell'utente; un
  token emesso prima dell'ultima modifica non riallinea più il ruolo.
- **Coda allargata** (US-105): «Da prendere in carico» comprende i ticket liberi in qualunque fase; chi li prende li
  segue dalla fase in cui erano.
- **Coordinatore** (US-112), scelte del team da confermare nella review (azione A14): il Coordinatore può spostare le
  segnalazioni anche su se stesso; nella sua pagina nessuna segnalazione è già selezionata. Il coordinamento decade
  togliendo il progetto o cambiando ruolo.
- **Registro degli accessi** (nuova US-113, 5 punti, R4; appunto del PO del 06/11): data, ora, IP ed esito di ogni
  accesso, anche falliti; lo vedono l'ADMIN e l'utente stesso; conservazione 12 mesi. Proposta per lo Sprint 10.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (05/11, sera) | 19 |
| Ripianificazione (06/11): +US-111, +US-112, −US-1308; fatte US-102, US-103, US-104 | 11 |
| US-111 unita (06/11) | 6 |
| US-105 unita (06/11) | 3 |
| US-112 unita, demo a `e30269f` (06/11) | 0 |

## Per il PO durante lo sprint

- collaudo: T-13.4, sezioni 16–17, T-19.2, T-20.2–T-20.8 (T-20.1 superata il 06/11);
- US-1301 (PWA su smartphone, T-10.1);
- SMTP in Keycloak, quando arriva US-103: lista dei passi nella PR.
