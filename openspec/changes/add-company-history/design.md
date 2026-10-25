## Decisions

### D1 — Come si contano le voci del riepilogo
| Voce | Evento |
|---|---|
| Proposte | `PROPOSTA_INVIATA` |
| Accettate | `PROPOSTA_ACCOLTA` |
| Appuntamenti falliti | `APPUNTAMENTO_FALLITO` |
| Tirocini | `TIROCINIO_AVVIATO` |
| Assunzioni | `TIROCINIO_CONCLUSO` con dettaglio `assunto: true` |
| Esiti negativi | `TIROCINIO_CONCLUSO` senza assunzione |

Le storie di R2 e R3 che registrano questi eventi useranno `CompanyEventType`; il riepilogo non andrà toccato.
"Esiti negativi" = tirocini conclusi senza assunzione (proposta del team, da confermare dal PO).

### D2 — Esclusioni nello stesso elenco
Le esclusioni (`ticket_company_blacklist`) non sono eventi dello storico, ma raccontano il rapporto con l'azienda: entrano
nell'elenco come tipo `ESCLUSIONE` con il motivo. Non contano nel riepilogo.

### D3 — Dalla più recente
Per valutare l'azienda contano gli ultimi fatti: l'elenco parte dal più recente, come il diario delle note.

### D4 — Calcolo in memoria
Gli eventi di un'azienda sono pochi: si leggono in una query (con la segnalazione collegata) e si contano nel servizio.
