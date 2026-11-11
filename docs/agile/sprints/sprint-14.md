# Sprint 14 — 12 – 14 novembre 2026

**Stato:** 📋 pianificato, 11 punti; si completa con le modifiche funzionali che il PO propone dopo la chiusura dello
Sprint 13. **Planning:** 11/11, nella [review dello Sprint 13](sprint-13-review.md) (§8.2, §9).

## Obiettivo

> Il Tutor scarica convenzione, progetti formativi e relazione finale compilati sui modelli reali della cooperativa,
> senza ritoccarli.

## Impegno (11 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-1011 | Documenti sui modelli reali (GOL) | 8 | ✅ Ready | `add-real-documents` | |
| EN-8 | Preparazione dei modelli reali | 3 | ✅ Ready | — (modelli fuori dal repository) | |

Ordine: prima US-1011, perché EN-8 usa i suoi segnaposto (review dello Sprint 12, §8.4); poi EN-8. I 6 modelli di GOL
(3 per l'Extracurriculare, 3 per l'Inclusione) restano fuori dal repository (`documenti_base/`) e li carica l'ADMIN
dalla pagina «Modelli dei documenti» (US-1010). Nei test si usano modelli finti. Il backup (US-1302) è rinviato (PO,
11/11). Prossima migrazione: **V41**.

Ogni storia con interfaccia nuova si controlla in Chrome **prima** della PR (A26), e un difetto si segnala solo dopo
aver escluso che dipenda dalla simulazione (A29).

## Dati da preparare

Servono a US-1011 per compilare i documenti senza righe vuote. Li prepara il PO mentre sviluppo; i valori stanno già
nei modelli reali della cooperativa.

### Soggetto promotore: nel `.env` del server

Come deciso il 09/11, vanno nel file `/opt/nexus/.env` del server, non nel repository: contengono anche dati personali
del legale rappresentante. Una variabile per riga, nella forma `NOME="valore"`. Con US-1011 il deploy le passa al
backend; finché una variabile manca, il documento lascia la riga vuota da completare a mano.

| Variabile | Contenuto | Esempio di forma |
|---|---|---|
| `NEXUS_PROMOTER_NAME` | Denominazione | `Cooperativa sociale "Il Girasole"` |
| `NEXUS_PROMOTER_ADDRESS` | Sede legale: via, numero civico e comune | `Via …, n. …, Firenze` |
| `NEXUS_PROMOTER_FISCAL_CODE` | Codice fiscale / partita IVA | 11 cifre |
| `NEXUS_PROMOTER_REPRESENTATIVE` | Nome e cognome del legale rappresentante | |
| `NEXUS_PROMOTER_REPRESENTATIVE_ROLE` | Ruolo, come lo scrivono le convenzioni di inclusione | `Rappresentante Legale` |
| `NEXUS_PROMOTER_REPRESENTATIVE_BIRTH_PLACE` | Luogo di nascita del legale rappresentante | |
| `NEXUS_PROMOTER_REPRESENTATIVE_BIRTH_DATE` | Data di nascita del legale rappresentante | `gg/mm/aaaa` |
| `NEXUS_PROMOTER_PHONE` | Telefono | |
| `NEXUS_PROMOTER_EMAIL` | Email | |
| `NEXUS_PROMOTER_PEC` | PEC | |
| `NEXUS_PROMOTER_INAIL_POSITION` | Posizione INAIL (i due modelli la scrivono in modo diverso: va indicata quella giusta) | |
| `NEXUS_PROMOTER_RC_POLICY` | Numero della polizza di responsabilità civile | |
| `NEXUS_PROMOTER_RC_INSURER` | Compagnia assicuratrice | |
| `NEXUS_PROMOTER_RC_EXPIRY` | Scadenza della polizza | `22/07/2027` |

Il tutore del soggetto promotore è il Tutor della segnalazione: non serve una variabile.

### Enti di presa in carico: nei Cataloghi

In «Cataloghi» → «Enti di presa in carico», per ognuno dei quattro enti (Comune di Firenze e tre Società della Salute):
- tipologia;
- codice fiscale;
- sede legale;
- rappresentante con ruolo, luogo e data di nascita;
- referente con telefono e email.

Rappresentante e sede si trovano nelle convenzioni di inclusione della cooperativa.

### Progetto GOL: con US-1011

Due dati per progetto, che l'ADMIN potrà modificare dopo US-1011:
- il **riferimento all'avviso**: proporrò come testo predefinito quello dei modelli («Avviso pubblico n. 10 per
  l'attuazione del Programma Garanzia Occupabilità dei Lavoratori…»);
- la **riga con codice e CUP** (es. «Firenze in Rete 2 – Codice 9014358 – CUP B81D24000440006»).

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (11/11, dopo la review) | 11 |

## Per il PO durante lo sprint

- Le modifiche funzionali da proporre per completare lo sprint.
- I dati del soggetto promotore nel `.env` del server e quelli degli enti nei Cataloghi (sopra).
- Collaudo dello Sprint 13 sulla demo `8d43ad3` (T-23.1 … T-23.4).
- T-21.6: lunedì 16/11, le prime PR di Dependabot, una alla volta, con il deploy.
