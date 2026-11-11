## 1. Backend

- [x] 1.1 Migrazione V37 solo additiva (comuni di sede legale, sede e residenza, numero civico) con FK e indici con
  nome; vincoli testati per nome
- [x] 1.2 Azienda: sede legale con il comune, validazione della versione (D3), audit con i valori; test di integrazione
- [x] 1.3 Scheda del tirocinio: sede con il comune, residenza in comune, via e numero civico; completezza per l'avvio,
  audit, appuntamento fallito e anonimizzazione; test di integrazione
- [x] 1.4 Documenti: indirizzi composti «Via Roma 1, Firenze (FI)» per sede legale, sede e residenza; test
- [x] 1.5 Demo: comune delle aziende dimostrative dopo il caricamento dell'elenco; test

## 2. Frontend

- [x] 2.1 Molecola `MunicipalityField` (combobox APG): ricerca, tastiera, clic, messaggio di stato; test di tastiera e
  axe
- [x] 2.2 Modulo dell'azienda e dettaglio dell'abbinamento con la sede legale e il comune; test
- [x] 2.3 Scheda del tirocinio: sede con il comune, residenza in comune, via e numero civico; test con axe

## 3. Consegna

- [x] 3.1 Verifiche: `./mvnw verify`; lint, formato, test e build del frontend
- [x] 3.2 Controllo in Chrome prima della PR (A26): tastiera, axe, tema scuro, smartphone
- [x] 3.3 Collaudo (T-23.1) e PR con la CI verde
