# Sprint 10 — 7 – 9 novembre 2026

**Stato:** 🎉 concluso l'08/11: impegno completato, **22 punti** (US-1303 da 5 a 8 con le decisioni del PO), demo a
`0c42db6`; nessun extra iniziato. **Planning:** 06/11, nella [review dello Sprint 9](sprint-09-review.md) (§9); proposta del team
confermata dal Product Owner.

## Obiettivo

> NEXUS è più sicuro per i dati reali: il deploy si ripristina da solo se una release non parte, chi dimentica la
> password la reimposta da solo, gli accessi sono registrati, i dati si anonimizzano alla scadenza e un avviso dice se
> l'app non risponde.

## Impegno (19 punti, poi 22)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| A16 | Ripristino del deploy, provato di proposito (azione, senza punti) | — | 🎉 Done (06/11) | — | [#110](https://github.com/maubernardi/nexus/pull/110), [#111](https://github.com/maubernardi/nexus/pull/111) |
| US-1308 | Recupero password, email di sicurezza, oggetto e firma di tutte le email | 2 | 🎉 Done | `add-password-reset`, `add-email-signature` | [#112](https://github.com/maubernardi/nexus/pull/112), correzioni [#113](https://github.com/maubernardi/nexus/pull/113)–[#117](https://github.com/maubernardi/nexus/pull/117) |
| US-113 | Registro degli accessi, con la pagina dei tentativi falliti | 7 | 🎉 Done | `add-access-log` | [#118](https://github.com/maubernardi/nexus/pull/118) |
| US-1303 | Anonimizzazione a scadenza, con conferma | 8 | 🎉 Done | `add-beneficiary-anonymization` | [#119](https://github.com/maubernardi/nexus/pull/119) |
| US-1307 | Monitoraggio (8–20) e revisione di sicurezza | 5 | 🎉 Done | `add-uptime-and-security-review` | [#120](https://github.com/maubernardi/nexus/pull/120) |

**Velocità:** 22 punti. Migrazione V31. Deploy della demo: `7d80d91`, `092b7d4` (US-113), `0c42db6` (US-1303), più
quelli intermedi di US-1308; nessun deploy fallito. La #120 (solo workflow e documenti) non richiede deploy.

**Extra:** US-205 *Curriculum del beneficiario* (5), non iniziata.

Ordine: A16 → US-1308 → US-113 → US-1303 → US-1307. Una PR per storia; gli stati del backlog si aggiornano nella PR di
documentazione dopo i merge. US-113 richiede un passo sul server (lista completa nella PR, A15). US-1302 *Backup* resta
nel backlog (review dello Sprint 9, §8.6). Prossima migrazione: **V31** (la V30 è quella vuota della prova di A16).

## Decisioni del planning (review dello Sprint 9)

- **US-112** (§8.1): confermate le scelte del team sul Coordinatore.
- **A16** (§8.3): prova del ripristino sulla demo a qualunque ora; fatta il 06/11, riuscita.
- **Registro degli accessi** (§8.4): registra Keycloak, NEXUS mostra; con la pagina dell'ADMIN di tutti i tentativi
  falliti (US-113 da 5 a 7 punti); conservazione 12 mesi.
- **Collaudo** (§8.5): prima la sezione 20, poi le sezioni 16–17.
- **Monitoraggio** (§8.7): controllo dalla CI di GitHub ogni 10 minuti, solo dalle 8 alle 20.

## Decisioni durante lo sprint

- **Recupero della password** (US-1308): collegamento valido 30 minuti (scelta del team, come il codice di US-110).
- **Email** (PO, 07/11), dopo la prova di T-21.1:
  - email di sicurezza a ogni cambio di password, con «<username>, le tue credenziali sono state cambiate il <data> alle
    <ora> da questo indirizzo IP: <ip>» e l'ora italiana; nessuna email per le password sbagliate;
  - **tutte** le email (Keycloak e NEXUS) con l'oggetto «PORTALE NEXUS - …» e la firma «Portale Nexus».
- **Campo password con «mostra»** del tema di Keycloak: contorno e bordo d'errore corretti (segnalazione del PO).
- **Anonimizzazione** (US-1303, PO 07/11): 2 anni dall'ultima attività; restano le iniziali e i dati statistici; il job
  notturno segna, decidono il Tutor o l'ADMIN (anonimizzare o tenere i dati con un motivo); una nuova attività toglie il
  segno. Stima da 5 a 8 punti; impegno da 19 a 22.
- **Registro degli accessi** (US-113): lo tiene Keycloak (12 mesi), NEXUS lo legge con il permesso `view-events`.
- **Revisione ASVS livello 1** (US-1307): nessuna lacuna bloccante; da chiudere prima della produzione L1 (password
  comuni) e L2 (avvisi di Dependabot, impostazione del PO).

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (06/11, sera; A16 già fatta) | 19 |
| US-1308 unita con email e firma (07/11) | 17 |
| US-113 unita (07/11) | 10 |
| US-1303 unita, stima da 5 a 8 (07/11) | 5 |
| US-1307 unita (08/11) | 0 |

## Per il PO durante lo sprint

- collaudo: T-21.2 (registro degli accessi), T-21.3 (anonimizzazione: «Demo Otto» è segnato dalla notte del 07/11),
  T-21.4 (controllo della demo, notifiche delle Actions e avvisi di Dependabot); poi sezione 20 (T-20.2–T-20.8),
  T-13.4, sezioni 16–17, T-19.2;
- US-1301 (PWA su smartphone, T-10.1).
