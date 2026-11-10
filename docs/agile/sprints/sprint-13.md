# Sprint 13 — 11 – 13 novembre 2026

**Stato:** 🔨 in corso. **Planning:** 10/11, nella [review dello Sprint 12](sprint-12-review.md) (§9); proposta del
team confermata dal Product Owner, con EN-9 (§8.3).

## Obiettivo

> Aziende e beneficiari hanno indirizzi con il comune ufficiale, il Tutor registra la convenzione con uno o più progetti
> formativi, la fine effettiva del tirocinio e il ruolo dei tutor aziendali, e l'ADMIN carica i modelli Word con le loro
> condizioni: dati e modelli per i documenti reali sono pronti.

## Impegno (22 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| EN-9 | Test del frontend stabili | 1 | ✅ Ready | — (solo test, nessun comportamento nuovo) | |
| US-1007 | Indirizzi con il comune | 3 | ✅ Ready | `add-municipality-addresses` | |
| US-1009 | Fine effettiva del tirocinio e ruolo del tutor aziendale | 2 | ✅ Ready | `add-internship-actual-end` | |
| US-1008 | Convenzione e progetti formativi | 8 | ✅ Ready | `add-training-projects` | |
| US-1010 | Modelli dei documenti caricati dall'ADMIN | 8 | ✅ Ready | `add-document-templates` | |

Ordine: EN-9 → US-1007 → US-1009 → US-1008 → US-1010. Prima i test stabili, così le verifiche delle storie non hanno
falsi rossi; poi le due storie piccole, che aggiungono dati ai moduli esistenti; poi US-1008, il cambiamento più grande
del modello dei dati; per ultima US-1010, che non dipende dalle altre. Una PR per storia; i nomi delle change sono
indicativi; gli stati del backlog si aggiornano nella PR di documentazione dopo i merge. Prossima migrazione: **V37**.

Ogni storia con interfaccia nuova si controlla in Chrome **prima** del merge (A26): se l'estensione è scollegata, lo
segnalo prima di aprire la PR.

## Decisioni del planning (10/11)

- **Tirocinio aperto** (US-206, confermato): segnalazione con la scheda del tirocinio e non ancora in
  FORM_RESTITUZIONE.
- **Segnaposto noti** (US-1010): quelli che NEXUS sa compilare oggi; i nuovi arrivano con US-1011 nello Sprint 14,
  quindi i modelli reali di GOL (EN-8) si caricano nello Sprint 14. Nei test si usano modelli finti.
- **Test instabili** (EN-9): la causa si corregge, non si nasconde con timeout più lunghi o ripetizioni.

## Dopo questo sprint

| Sprint | Storie | Punti |
|---|---|---|
| 14 | US-1011 Documenti sui modelli reali · EN-8 Preparazione dei modelli | 11 |
| poi | US-1305 Revisione UX/UI · US-1302 Backup · US-1306 Passaggio in produzione | — |

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (10/11, dopo il planning) | 22 |

## Per il PO durante lo sprint

- Collaudo dello Sprint 12 sulla demo `ed260ad` (T-22.1 … T-22.4).
- Tenere collegata l'estensione di Chrome mentre lavoro alle storie con interfaccia.
- T-21.6: lunedì 16/11, le prime PR di Dependabot, una alla volta, con il deploy.
