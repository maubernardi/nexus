## Why

Storia **EN-8** (Sprint 14): preparare i 6 modelli di GOL (Extracurriculare: convenzione, progetto formativo, relazione
finale; Inclusione: Allegati A, B e C) con i segnaposto di US-1011, così l'ADMIN li carica (US-1010) e il Tutor scarica
documenti pronti da firmare (Q5, planning del 09/11).

Preparandoli sono emersi due ostacoli nel codice:
- il motore dei segnaposto riscrive tutto il paragrafo che ne contiene uno in un solo run, con la formattazione del
  primo. Nei modelli reali questo toglie grassetti, tabulazioni e simboli dalle righe compilate (per esempio
  «denominato “**soggetto promotore**”»);
- l'Allegato B ha la tipologia dell'ente a caselle (Comune, Società della Salute…). NEXUS la conosce (US-115), ma non
  ha un segnaposto. Senza, il modello non può valere per tutti e quattro gli enti, come deciso dal PO il 09/11.

## What Changes

- **Formattazione dei modelli conservata**: il motore sostituisce solo il testo del segnaposto; il resto del
  paragrafo (grassetti, tabulazioni, simboli, campi) resta com'è. Un segnaposto spezzato in più run prende la
  formattazione del run in cui comincia; un a capo nel valore resta nello stesso run.
- **Tipologia dell'ente a caselle**: sette segnaposto `{TIPO_ENTE_…}` (`COMUNE`, `UNIONE_COMUNI`, `COMUNITA_MONTANA`,
  `SOCIETA_DELLA_SALUTE`, `AZIENDA_SANITARIA`, `UIEPE`, `ALTRO`): `☒` sulla tipologia dell'ente del beneficiario, `☐`
  sulle altre; senza ente tutte `☐`. Compaiono nel gruppo «Ente di presa in carico» della pagina dei modelli.
- **I 6 modelli** preparati fuori dal repository (`documenti_base/modelli-nexus/`), secondo le decisioni del PO:
  - segnaposto in tutti i campi che NEXUS conosce, anche dove l'originale aveva solo righe vuote;
  - dati del promotore e degli enti sostituiti dai segnaposto;
  - bollo: resta solo «non è soggetta a imposta di bollo» (PO, 16/11);
  - firma «Il Tirocinante» nel progetto personalizzato (Allegato B);
  - «Legale rappresentante» come qualifica fissa dell'ospitante;
  - la casella della L. 68/99 resta da spuntare a mano (PO, 16/11).

## Non-goals

- **Modelli nel repository**: restano in `documenti_base/` (`.gitignore`), con lo script che li prepara; nei test si
  usano modelli finti.
- **Caricamento sulla demo**: lo fa l'ADMIN dalla pagina «Modelli dei documenti» (collaudo T-24.5).
- **Dati da compilare a mano**: CCNL, dipendenti, ADA/ISTAT, obiettivi, attività, permesso di soggiorno, situazione
  occupazionale, categorie svantaggiate (PO, 09/11 e 16/11).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `internship-documents`: formattazione dei modelli conservata, tipologia dell'ente a caselle.

## Impact

- Backend: `DocxTemplate` (sostituzione nei nodi di testo), `DocumentPlaceholders` e `DocumentValues` (sette
  segnaposto). Nessuna migrazione, nessuna API nuova; `GET /document-templates/placeholders` ha sette voci in più.
- Frontend: nessuna modifica; la pagina dei modelli mostra i segnaposto nuovi dall'API.
- Sul server: dopo il deploy l'ADMIN carica i 6 modelli (T-24.5). I modelli generici nel codice non cambiano.
