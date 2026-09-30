# NEXUS — Product Backlog

Ordinato per priorità dal Product Owner. Stime in story point (Fibonacci); stato: 📋 Backlog · ✅ Ready · 🔨 In corso · 👀 In review · 🎉 Done.
Metodo di lavoro, Definition of Ready e Definition of Done: [README](README.md). Release: [Roadmap](roadmap.md).

**Totale: 57 elementi, 208 punti.** La fondazione tecnica (Sprint 0) è già completata: vedi [sprint-00](sprints/sprint-00.md).

## Epiche

| Epica | Titolo | Obiettivo | Storie | Punti |
|---|---|---|---|---|
| E1 | Utenti e progetti | L'ADMIN gestisce utenti, ruoli, progetti e cataloghi senza passare dalla console di Keycloak. | 6 | 21 |
| E2 | Anagrafica candidati | Il Tutor registra e mantiene i dati dei tirocinanti, nel rispetto della minimizzazione GDPR. | 5 | 18 |
| E3 | Segnalazioni (fase 1) | Il Tutor segnala un candidato per un percorso di tirocinio e ne segue lo stato. | 7 | 27 |
| E4 | Coda Call Center e fast-track (fase 3) | Il Call Center lavora le segnalazioni in ordine FIFO, con priorità alle segnalazioni speciali. | 4 | 15 |
| E5 | Aziende e mansioni | Il Call Center mantiene il database delle aziende ospitanti e delle mansioni offerte. | 3 | 8 |
| E6 | Abbinamento (fase 4) | Il Call Center abbina il candidato a una mansione libera e la blocca. | 3 | 12 |
| E7 | Proposta e timer (fase 5) | Il Tutor valuta la proposta; i tempi di risposta sono presidiati da un timer di 7 giorni lavorativi. | 5 | 15 |
| E8 | Appuntamento e rilancio (fase 6) | Il Tutor registra l'esito dell'appuntamento; se fallisce, la mansione può essere rilanciata in bacheca. | 3 | 9 |
| E9 | Bacheca opportunità | Le mansioni disponibili sono visibili ai Tutor in forma anonima e gestite dal Call Center. | 4 | 13 |
| E10 | Tirocinio e documenti (fase 7) | I dati amministrativi del tirocinio producono Convenzione e Progetto Formativo in Word. | 5 | 19 |
| E11 | Chiusura e restituzione | Il percorso si chiude con un report e la traccia immodificabile nell'audit trail dell'azienda. | 2 | 7 |
| E12 | Notifiche | Le persone giuste sono avvisate al momento giusto, in app e via email. | 2 | 10 |
| E13 | Trasversali e produzione | Qualità, sicurezza, GDPR e passaggio dalla demo alla produzione. | 8 | 34 |

## Release

| Release | Obiettivo | Storie | Punti |
|---|---|---|---|
| R1 | Flusso base: dalla segnalazione alla proposta | 17 | 57 |
| R2 | Proposta, timer, appuntamenti, bacheca e fast-track | 18 | 60 |
| R3 | Tirocinio, documenti Word, chiusura, email | 10 | 38 |
| R4 | Produzione: utenti, backup, GDPR, UX, sicurezza | 12 | 53 |

## Domande aperte per il Product Owner

Bloccano la *Definition of Ready* delle storie indicate.

| # | Domanda | Storie | Risposta |
|---|---|---|---|
| Q1 | Rifiuto della segnalazione speciale: il tipo diventa NORMAL o resta SPECIAL senza fast-track? | US-404 | — |
| Q2 | Scadenza del timer: il ticket torna in IN_LAVORAZIONE o in RIAPERTO? | US-705 | — |
| Q3 | Il Tutor può rifiutare una proposta? Con quali effetti (es. azienda esclusa automaticamente)? | US-702 | — |
| Q4 | Festività da escludere: solo nazionali o anche il patrono locale? Chiusure della cooperativa? | US-703 | — |
| Q5 | Campi della scheda amministrativa e template Word reali di Convenzione e Progetto Formativo. | US-1001..1005 | — |
| Q6 | Voci esatte dei gradimenti 1–5 del report di restituzione. | US-1101 | — |
| Q7 | Durata di conservazione dei dati dei candidati (decisione della cooperativa/DPO). | US-1303 | — |
| Q8 | Chi approva la mansione a testo libero (ADMIN?) e se, approvata, entra nel catalogo. | US-302 | — |
| Q9 | Canali delle notifiche per evento (solo app, app + email) e destinatari. | US-1201, US-1202 | — |
| Q10 | Elenco reale di zone e tipologie di mansione. | US-106 | — |
| Q11 | Cosa vedono gli altri tutor dello stesso progetto del candidato (oltre al divieto su nome e cognome). | US-204 | — |

## Elenco delle storie

| ID | Epica | Storia | Priorità | Punti | Release | Stato |
|---|---|---|---|---|---|---|
| [EN-1](#en-1) | E3 | Motore delle transizioni del ticket | Must | 5 | R1 | 🔨 In corso (Sprint 1) |
| [US-101](#us-101) | E1 | Messaggio "utente non abilitato" | Should | 1 | R1 | 🔨 In corso (Sprint 1) |
| [US-102](#us-102) | E1 | Gestione progetti | Must | 3 | R4 | 📋 Backlog |
| [US-103](#us-103) | E1 | Creazione utente | Must | 8 | R4 | 📋 Backlog |
| [US-104](#us-104) | E1 | Assegnazione utenti ai progetti | Must | 3 | R4 | 📋 Backlog |
| [US-105](#us-105) | E1 | Disattivazione e cambio ruolo | Must | 3 | R4 | 📋 Backlog |
| [US-106](#us-106) | E1 | Cataloghi di zone e tipologie di mansione | Should | 3 | R4 | 📋 Backlog |
| [US-201](#us-201) | E2 | Inserimento candidato | Must | 5 | R1 | 👀 In review (Sprint 1) |
| [US-202](#us-202) | E2 | I miei candidati | Must | 3 | R1 | 📋 Backlog |
| [US-203](#us-203) | E2 | Modifica candidato | Must | 2 | R1 | 📋 Backlog |
| [US-204](#us-204) | E2 | Visibilità minimizzata dei candidati | Must | 3 | R1 | 📋 Backlog |
| [US-205](#us-205) | E2 | Curriculum del candidato | Could | 5 | R3 | 📋 Backlog |
| [US-301](#us-301) | E3 | Nuova segnalazione | Must | 5 | R1 | 🔨 In corso (Sprint 1) |
| [US-302](#us-302) | E3 | Mansione a testo libero e approvazione | Should | 5 | R2 | 📋 Backlog |
| [US-303](#us-303) | E3 | I miei ticket | Must | 3 | R1 | 📋 Backlog |
| [US-304](#us-304) | E3 | Ticket dei miei progetti | Should | 3 | R1 | 📋 Backlog |
| [US-305](#us-305) | E3 | Dettaglio e cronologia del ticket | Must | 3 | R1 | 📋 Backlog |
| [US-306](#us-306) | E3 | Segnalazione speciale dalla bacheca | Must | 3 | R2 | 📋 Backlog |
| [US-401](#us-401) | E4 | Coda FIFO | Must | 5 | R1 | 🔨 In corso (Sprint 1) |
| [US-402](#us-402) | E4 | Presa in carico | Must | 2 | R1 | 📋 Backlog |
| [US-403](#us-403) | E4 | Approvazione fast-track | Must | 5 | R2 | 📋 Backlog |
| [US-404](#us-404) | E4 | Rifiuto fast-track | Must | 3 | R2 | 📋 Backlog |
| [US-501](#us-501) | E5 | Anagrafica aziende | Must | 3 | R1 | 📋 Backlog |
| [US-502](#us-502) | E5 | Mansioni dell'azienda | Must | 3 | R1 | 📋 Backlog |
| [US-503](#us-503) | E5 | Audit trail dell'azienda | Should | 2 | R3 | 📋 Backlog |
| [US-601](#us-601) | E6 | Ricerca mansioni compatibili | Must | 5 | R1 | 📋 Backlog |
| [US-602](#us-602) | E6 | Abbinamento e proposta | Must | 5 | R1 | 📋 Backlog |
| [US-603](#us-603) | E6 | Esclusione di un'azienda | Should | 2 | R2 | 📋 Backlog |
| [US-701](#us-701) | E7 | Accettazione della proposta | Must | 3 | R2 | 📋 Backlog |
| [US-702](#us-702) | E7 | Rifiuto della proposta | Should | 3 | R2 | 📋 Backlog |
| [US-703](#us-703) | E7 | Calendario dei giorni lavorativi | Must | 3 | R2 | 📋 Backlog |
| [US-704](#us-704) | E7 | Promemoria al 4° giorno | Must | 3 | R2 | 📋 Backlog |
| [US-705](#us-705) | E7 | Scadenza del timer | Must | 3 | R2 | 📋 Backlog |
| [US-801](#us-801) | E8 | Appuntamento preso | Must | 2 | R2 | 📋 Backlog |
| [US-802](#us-802) | E8 | Appuntamento fallito | Must | 2 | R2 | 📋 Backlog |
| [US-803](#us-803) | E8 | Rilancio in bacheca | Must | 5 | R2 | 📋 Backlog |
| [US-901](#us-901) | E9 | Bacheca per il Tutor | Must | 3 | R2 | 📋 Backlog |
| [US-902](#us-902) | E9 | Gestione della bacheca | Must | 5 | R2 | 📋 Backlog |
| [US-903](#us-903) | E9 | Sblocco a tutti dopo 7 giorni | Must | 2 | R2 | 📋 Backlog |
| [US-904](#us-904) | E9 | Annuncio da una mansione libera | Should | 3 | R2 | 📋 Backlog |
| [US-1001](#us-1001) | E10 | Scheda amministrativa del tirocinio | Must | 5 | R3 | 📋 Backlog |
| [US-1002](#us-1002) | E10 | Griglia oraria e sospensioni | Must | 5 | R3 | 📋 Backlog |
| [US-1003](#us-1003) | E10 | Avvio del tirocinio | Must | 1 | R3 | 📋 Backlog |
| [US-1004](#us-1004) | E10 | Convenzione in Word | Must | 5 | R3 | 📋 Backlog |
| [US-1005](#us-1005) | E10 | Progetto Formativo in Word | Must | 3 | R3 | 📋 Backlog |
| [US-1101](#us-1101) | E11 | Report di restituzione | Must | 5 | R3 | 📋 Backlog |
| [US-1102](#us-1102) | E11 | Esito sulla mansione e audit trail | Must | 2 | R3 | 📋 Backlog |
| [US-1201](#us-1201) | E12 | Notifiche in app | Must | 5 | R2 | 📋 Backlog |
| [US-1202](#us-1202) | E12 | Email transazionali | Should | 5 | R3 | 📋 Backlog |
| [US-1301](#us-1301) | E13 | Verifica della PWA online | Must | 1 | R1 | 📋 Backlog |
| [US-1302](#us-1302) | E13 | Backup del database | Must | 5 | R4 | 📋 Backlog |
| [US-1303](#us-1303) | E13 | Anonimizzazione a scadenza | Must | 5 | R4 | 📋 Backlog |
| [US-1304](#us-1304) | E13 | Tema di Keycloak | Should | 3 | R4 | 📋 Backlog |
| [US-1305](#us-1305) | E13 | Revisione UX/UI | Should | 8 | R4 | 📋 Backlog |
| [US-1306](#us-1306) | E13 | Passaggio in produzione | Must | 5 | R4 | 📋 Backlog |
| [US-1307](#us-1307) | E13 | Monitoraggio e revisione di sicurezza | Must | 5 | R4 | 📋 Backlog |
| [US-1308](#us-1308) | E13 | Recupero password | Should | 2 | R4 | 📋 Backlog |

## Dettaglio per epica

### E1 — Utenti e progetti

_L'ADMIN gestisce utenti, ruoli, progetti e cataloghi senza passare dalla console di Keycloak._

#### US-101
**Messaggio "utente non abilitato"** · Should · 1 punti · R1 · 🔨 In corso (Sprint 1)

Come **utente autenticato ma non censito o disattivato** voglio vedere un messaggio chiaro invece di un errore generico per capire che devo rivolgermi all'amministratore.

Criteri di accettazione:
- Dato un utente che riceve 403 "Utente non abilitato a NEXUS", quando apre l'app, allora vede un messaggio dedicato con l'indicazione di contattare l'amministratore e il pulsante Esci, senza "Riprova".

#### US-102
**Gestione progetti** · Must · 3 punti · R4 · 📋 Backlog

Come **ADMIN** voglio creare, modificare e disattivare i progetti (es. GOL, POLIS) per abilitare nuovi finanziamenti senza interventi tecnici.

Criteri di accettazione:
- Quando creo un progetto con codice univoco e nome, allora compare negli elenchi di selezione.
- Quando disattivo un progetto, allora non è più selezionabile per nuove segnalazioni ma lo storico resta consultabile.

#### US-103
**Creazione utente** · Must · 8 punti · R4 · 📋 Backlog

Come **ADMIN** voglio creare un utente con ruolo in NEXUS e in Keycloak in un'unica operazione per abilitare nuovi tutor e operatori senza usare la console di Keycloak.

Criteri di accettazione:
- Quando creo un utente (nome, cognome, email, telefono, ruolo), allora esiste in NEXUS e in Keycloak con lo stesso identificativo e il ruolo scelto.
- Se la creazione in Keycloak riesce ma quella in NEXUS fallisce, allora l'account Keycloak viene rimosso (nessuno stato incoerente).
- L'utente riceve le istruzioni per impostare la password (dipende da US-1202 per l'email).

Dipendenze: US-1202 per l'invio email

#### US-104
**Assegnazione utenti ai progetti** · Must · 3 punti · R4 · 📋 Backlog

Come **ADMIN** voglio assegnare e rimuovere progetti a un utente per regolare la visibilità dei ticket tra tutor.

Criteri di accettazione:
- Quando assegno un progetto a un tutor, allora vede in sola lettura i ticket di quel progetto (US-304).

#### US-105
**Disattivazione e cambio ruolo** · Must · 3 punti · R4 · 📋 Backlog

Come **ADMIN** voglio disattivare/riattivare un utente e cambiarne il ruolo per gestire uscite e cambi di mansione senza perdere lo storico.

Criteri di accettazione:
- Quando disattivo un utente, allora non può più accedere (NEXUS e Keycloak) e i suoi ticket restano consultabili.
- Quando cambio il ruolo, allora il nuovo ruolo vale dal login successivo.

#### US-106
**Cataloghi di zone e tipologie di mansione** · Should · 3 punti · R4 · 📋 Backlog

Come **ADMIN** voglio gestire le zone e le tipologie di mansione per sostituire i valori segnaposto con quelli reali della cooperativa.

Criteri di accettazione:
- Quando disattivo una voce, allora non è più selezionabile ma resta sui dati esistenti.

Domande aperte: Q10

### E2 — Anagrafica candidati

_Il Tutor registra e mantiene i dati dei tirocinanti, nel rispetto della minimizzazione GDPR._

#### US-201
**Inserimento candidato** · Must · 5 punti · R1 · 🔨 In corso (Sprint 1)

Come **Tutor** voglio registrare un candidato con i dati previsti (anagrafica minima, patente, mezzi, L. 68/99, titolo di studio, vincoli, lingue) per poterlo segnalare per un tirocinio.

Criteri di accettazione:
- Quando compilo il modulo con dati validi, allora il candidato è salvato e ne sono il tutor proprietario.
- Quando un campo non è valido (es. anno di nascita futuro, livello linguistico non ammesso), allora vedo l'errore accanto al campo, annunciato agli screen reader.
- Il modulo è utilizzabile solo da tastiera e su smartphone a 320 px.

#### US-202
**I miei candidati** · Must · 3 punti · R1 · 📋 Backlog

Come **Tutor** voglio vedere l'elenco e il dettaglio dei candidati che ho inserito per ritrovarli rapidamente.

Criteri di accettazione:
- Vedo solo i candidati di cui sono proprietario, con ricerca per nome.

#### US-203
**Modifica candidato** · Must · 2 punti · R1 · 📋 Backlog

Come **Tutor** voglio correggere i dati di un mio candidato per tenerli aggiornati.

Criteri di accettazione:
- Posso modificare solo i miei candidati; le modifiche sono tracciate (autore e data).

#### US-204
**Visibilità minimizzata dei candidati** · Must · 3 punti · R1 · 📋 Backlog

Come **responsabile della privacy** voglio che nome e cognome del candidato siano visibili solo al tutor proprietario, al Call Center e all'ADMIN per rispettare la minimizzazione GDPR.

Criteri di accettazione:
- Dato un ticket di un progetto condiviso, quando lo apre un altro tutor, allora vede i dati del percorso ma non nome e cognome del candidato.
- Le API non restituiscono mai i campi nascosti (verifica lato server, non solo nell'interfaccia).

Domande aperte: Q11

#### US-205
**Curriculum del candidato** · Could · 5 punti · R3 · 📋 Backlog

Come **Tutor** voglio allegare il CV (PDF) al candidato per metterlo a disposizione del Call Center per l'abbinamento.

Criteri di accettazione:
- Accetta solo PDF fino a 5 MB; il file non è accessibile senza autorizzazione.
- Il CV si scarica solo da utenti autorizzati a vedere il candidato.

### E3 — Segnalazioni (fase 1)

_Il Tutor segnala un candidato per un percorso di tirocinio e ne segue lo stato._

#### EN-1
**Motore delle transizioni del ticket** · Must · 5 punti · R1 · 🔨 In corso (Sprint 1)

Come **team di sviluppo** voglio un servizio unico che applica le transizioni di stato ammesse per ruolo, registra lo storico e rifiuta le transizioni non valide per ogni storia delle fasi 1–7 aggiunga solo le proprie transizioni senza duplicare regole.

Criteri di accettazione:
- Dato un ticket in uno stato, quando si richiede una transizione non ammessa, allora il sistema risponde 409 e lo stato non cambia.
- Quando una transizione riesce, allora viene aggiunta una riga a `ticket_status_history` con autore e nota.
- Quando due richieste concorrenti modificano lo stesso ticket, allora la seconda riceve 409 (blocco ottimistico).

#### US-301
**Nuova segnalazione** · Must · 5 punti · R1 · 🔨 In corso (Sprint 1)

Come **Tutor** voglio segnalare un mio candidato per un progetto indicando la mansione desiderata dal catalogo per avviare il percorso di tirocinio.

Criteri di accettazione:
- Quando invio la segnalazione, allora il ticket nasce in stato NUOVA con un numero progressivo ed entra nella coda del Call Center.
- Posso scegliere solo tra i progetti a cui sono assegnato.

Dipendenze: EN-1, US-201

#### US-302
**Mansione a testo libero e approvazione** · Should · 5 punti · R2 · 📋 Backlog

Come **Tutor / ADMIN** voglio indicare una mansione non presente nel catalogo, che l'ADMIN approva o respinge per non bloccare segnalazioni atipiche.

Criteri di accettazione:
- Con mansione a testo libero il ticket nasce in IN_ATTESA_APPROVAZIONE_ADMIN.
- Se l'ADMIN approva, il ticket passa in NUOVA (ed entra in coda); se respinge, il tutor vede la motivazione.

Domande aperte: Q8

#### US-303
**I miei ticket** · Must · 3 punti · R1 · 📋 Backlog

Come **Tutor** voglio vedere l'elenco dei miei ticket con numero, candidato, stato e data per seguire i percorsi in corso.

Criteri di accettazione:
- L'elenco si filtra per stato e progetto; lo stato è indicato con testo, non solo colore.

#### US-304
**Ticket dei miei progetti** · Should · 3 punti · R1 · 📋 Backlog

Come **Tutor** voglio consultare in sola lettura i ticket degli altri tutor dei progetti a cui sono assegnato per coordinarmi con i colleghi.

Criteri di accettazione:
- Non vedo ticket di progetti a cui non sono assegnato.
- Sui ticket altrui non ho azioni di modifica e non vedo nome e cognome del candidato (US-204).

#### US-305
**Dettaglio e cronologia del ticket** · Must · 3 punti · R1 · 📋 Backlog

Come **Tutor / Call Center** voglio vedere il dettaglio del ticket e la cronologia degli stati per sapere cosa è successo e quando.

Criteri di accettazione:
- La cronologia mostra per ogni cambio di stato data, autore e nota.

#### US-306
**Segnalazione speciale dalla bacheca** · Must · 3 punti · R2 · 📋 Backlog

Come **Tutor** voglio candidare un mio candidato direttamente a un post #N della bacheca per ottenere una corsia prioritaria (fast-track).

Criteri di accettazione:
- Il ticket nasce SPECIAL, fast-track, in IN_LAVORAZIONE, collegato al post, con badge prioritario per il Call Center.

Dipendenze: US-901

### E4 — Coda Call Center e fast-track (fase 3)

_Il Call Center lavora le segnalazioni in ordine FIFO, con priorità alle segnalazioni speciali._

#### US-401
**Coda FIFO** · Must · 5 punti · R1 · 🔨 In corso (Sprint 1)

Come **operatore Call Center** voglio vedere la coda delle segnalazioni da lavorare, prima le speciali poi in ordine di arrivo per lavorare in modo equo e rapido.

Criteri di accettazione:
- La coda mostra NUOVA e IN_LAVORAZIONE non assegnate; le fast-track in cima con badge testuale.
- Filtri per progetto e zona; vista ottimizzata per desktop.

#### US-402
**Presa in carico** · Must · 2 punti · R1 · 📋 Backlog

Come **operatore Call Center** voglio prendere in carico un ticket per evitare che due operatori lavorino lo stesso caso.

Criteri di accettazione:
- Il ticket passa in IN_LAVORAZIONE assegnato a me; se un collega lo ha preso un istante prima, ricevo un avviso (409).

Dipendenze: EN-1

#### US-403
**Approvazione fast-track** · Must · 5 punti · R2 · 📋 Backlog

Come **operatore Call Center** voglio approvare una segnalazione speciale per trasformarla subito in proposta all'azienda.

Criteri di accettazione:
- All'approvazione: ticket in PROPOSTA_AZIENDA, mansione del post BLOCCATA, post ARCHIVED, in un'unica operazione.

Dipendenze: US-306

#### US-404
**Rifiuto fast-track** · Must · 3 punti · R2 · 📋 Backlog

Come **operatore Call Center** voglio rifiutare una segnalazione speciale per declassarla a lavorazione normale.

Criteri di accettazione:
- Al rifiuto: fast-track disattivato, ticket in IN_LAVORAZIONE normale, post ancora PUBLISHED.

Domande aperte: Q1

### E5 — Aziende e mansioni

_Il Call Center mantiene il database delle aziende ospitanti e delle mansioni offerte._

#### US-501
**Anagrafica aziende** · Must · 3 punti · R1 · 📋 Backlog

Come **operatore Call Center** voglio creare, modificare e disattivare aziende (ragione sociale, P.IVA, sede, referente, contatti) per avere un database affidabile per il matching.

Criteri di accettazione:
- La P.IVA è validata (11 cifre) e univoca; un'azienda con storico si disattiva, non si cancella.

#### US-502
**Mansioni dell'azienda** · Must · 3 punti · R1 · 📋 Backlog

Come **operatore Call Center** voglio gestire le mansioni di un'azienda (titolo, descrizione, tipologia, zona) per sapere quali posizioni sono disponibili.

Criteri di accettazione:
- Lo stato LIBERA/BLOCCATA è visibile e non si modifica a mano: lo governano abbinamenti e chiusure.

#### US-503
**Audit trail dell'azienda** · Should · 2 punti · R3 · 📋 Backlog

Come **operatore Call Center / ADMIN** voglio consultare lo storico immodificabile degli eventi di un'azienda per avere trasparenza verso aziende ed enti.

Criteri di accettazione:
- Lo storico è in sola lettura, in ordine cronologico, con autore e ticket collegato.

### E6 — Abbinamento (fase 4)

_Il Call Center abbina il candidato a una mansione libera e la blocca._

#### US-601
**Ricerca mansioni compatibili** · Must · 5 punti · R1 · 📋 Backlog

Come **operatore Call Center** voglio trovare le mansioni libere compatibili con il candidato (zona, tipologia) escludendo le aziende in blacklist per abbinare velocemente.

Criteri di accettazione:
- Sono proposte solo mansioni LIBERE di aziende attive e non escluse per quel ticket.

Dipendenze: US-502

#### US-602
**Abbinamento e proposta** · Must · 5 punti · R1 · 📋 Backlog

Come **operatore Call Center** voglio assegnare una mansione al ticket per proporre il candidato all'azienda.

Criteri di accettazione:
- La mansione diventa BLOCCATA dal ticket e il ticket passa in PROPOSTA_AZIENDA.
- Se la mansione è stata appena bloccata da un altro ticket, l'abbinamento fallisce con un messaggio chiaro.

Dipendenze: EN-1, US-601

#### US-603
**Esclusione di un'azienda** · Should · 2 punti · R2 · 📋 Backlog

Come **operatore Call Center** voglio escludere un'azienda per un ticket, con motivo per non riproporla allo stesso candidato.

Criteri di accettazione:
- Un'azienda esclusa non compare più nella ricerca per quel ticket.

### E7 — Proposta e timer (fase 5)

_Il Tutor valuta la proposta; i tempi di risposta sono presidiati da un timer di 7 giorni lavorativi._

#### US-701
**Accettazione della proposta** · Must · 3 punti · R2 · 📋 Backlog

Come **Tutor** voglio accettare la proposta per il mio candidato per far partire il contatto con l'azienda.

Criteri di accettazione:
- Il ticket passa in PROPOSTA_ACCOLTA e parte il timer di 7 giorni lavorativi, con scadenza visibile.

Dipendenze: US-703

#### US-702
**Rifiuto della proposta** · Should · 3 punti · R2 · 📋 Backlog

Come **Tutor** voglio rifiutare la proposta con motivo per tornare alla ricerca di un'altra mansione.

Criteri di accettazione:
- La mansione torna LIBERA e il ticket torna in lavorazione.

Domande aperte: Q3

#### US-703
**Calendario dei giorni lavorativi** · Must · 3 punti · R2 · 📋 Backlog

Come **team di sviluppo** voglio calcolare le scadenze escludendo sabati, domeniche e festività per avere timer corretti.

Criteri di accettazione:
- La scadenza di 7 giorni lavorativi salta weekend e festività nazionali (es. 25 aprile, Pasquetta).

Domande aperte: Q4

#### US-704
**Promemoria al 4° giorno** · Must · 3 punti · R2 · 📋 Backlog

Come **Tutor** voglio ricevere un solo promemoria al 4° giorno lavorativo senza azioni per non far scadere la proposta.

Criteri di accettazione:
- Il promemoria parte una sola volta anche se il job gira più volte (idempotenza).

Dipendenze: US-1201

#### US-705
**Scadenza del timer** · Must · 3 punti · R2 · 📋 Backlog

Come **Call Center** voglio che allo scadere del 7° giorno lavorativo la mansione torni libera per non tenere bloccate posizioni inattive.

Criteri di accettazione:
- Allo scadere: mansione LIBERA, ticket rimesso in lavorazione, evento nello storico.

Domande aperte: Q2

### E8 — Appuntamento e rilancio (fase 6)

_Il Tutor registra l'esito dell'appuntamento; se fallisce, la mansione può essere rilanciata in bacheca._

#### US-801
**Appuntamento preso** · Must · 2 punti · R2 · 📋 Backlog

Come **Tutor** voglio registrare che l'appuntamento con l'azienda è fissato per avanzare il percorso.

Criteri di accettazione:
- Il ticket passa in APPUNTAMENTO e il timer si ferma.

#### US-802
**Appuntamento fallito** · Must · 2 punti · R2 · 📋 Backlog

Come **Tutor** voglio registrare che l'appuntamento non è andato a buon fine per riaprire la ricerca.

Criteri di accettazione:
- Il ticket passa in RIAPERTO e la mansione torna LIBERA; l'evento entra nell'audit trail dell'azienda.

#### US-803
**Rilancio in bacheca** · Must · 5 punti · R2 · 📋 Backlog

Come **Tutor** voglio chiedere il rilancio della mansione in bacheca dopo un appuntamento fallito per offrirla ad altri candidati.

Criteri di accettazione:
- Si crea una bozza di post precompilata dalla mansione, riservata al progetto del ticket, collegata al ticket.
- Il Call Center riceve una notifica.

Dipendenze: US-802, US-1201

### E9 — Bacheca opportunità

_Le mansioni disponibili sono visibili ai Tutor in forma anonima e gestite dal Call Center._

#### US-901
**Bacheca per il Tutor** · Must · 3 punti · R2 · 📋 Backlog

Come **Tutor** voglio consultare gli annunci pubblici e quelli dei miei progetti, senza il nome dell'azienda per proporre candidati adatti.

Criteri di accettazione:
- Il nome dell'azienda non arriva mai al browser del tutor (filtrato dall'API).
- Annunci filtrabili per zona e tipologia; consultabili da smartphone.

#### US-902
**Gestione della bacheca** · Must · 5 punti · R2 · 📋 Backlog

Come **operatore Call Center** voglio vedere le bozze, modificarle, pubblicarle e archiviarle per controllare cosa è visibile ai tutor.

Criteri di accettazione:
- Solo Call Center e ADMIN modificano i post; la pubblicazione registra la data.

#### US-903
**Sblocco a tutti dopo 7 giorni** · Must · 2 punti · R2 · 📋 Backlog

Come **operatore Call Center** voglio rendere pubblico un post riservato a un progetto per allargare la platea se nessuno risponde.

Criteri di accettazione:
- Il comando è disponibile solo dopo 7 giorni solari dalla pubblicazione; registra la data dello sblocco.

#### US-904
**Annuncio da una mansione libera** · Should · 3 punti · R2 · 📋 Backlog

Come **operatore Call Center** voglio creare un annuncio partendo da una mansione libera per promuovere le posizioni aperte.

Criteri di accettazione:
- Una mansione può avere un solo annuncio attivo (bozza o pubblicato).

### E10 — Tirocinio e documenti (fase 7)

_I dati amministrativi del tirocinio producono Convenzione e Progetto Formativo in Word._

#### US-1001
**Scheda amministrativa del tirocinio** · Must · 5 punti · R3 · 📋 Backlog

Come **Tutor** voglio compilare i dati del tirocinio (date, monte ore, dati anagrafici necessari come il codice fiscale) per preparare i documenti.

Criteri di accettazione:
- I campi e le validazioni seguono i moduli ufficiali della cooperativa.

Domande aperte: Q5

#### US-1002
**Griglia oraria e sospensioni** · Must · 5 punti · R3 · 📋 Backlog

Come **Tutor** voglio definire l'orario settimanale e i periodi di sospensione (dal–al) per calcolare correttamente il monte ore.

Criteri di accettazione:
- Periodi di sospensione sovrapposti vengono rifiutati; il monte ore si ricalcola.

Domande aperte: Q5

#### US-1003
**Avvio del tirocinio** · Must · 1 punti · R3 · 📋 Backlog

Come **Tutor** voglio confermare l'avvio del tirocinio per tracciarne l'inizio.

Criteri di accettazione:
- Il ticket passa in IN_TIROCINIO solo con scheda completa.

#### US-1004
**Convenzione in Word** · Must · 5 punti · R3 · 📋 Backlog

Come **Tutor / Call Center** voglio generare e scaricare la Convenzione compilata per evitare la compilazione manuale.

Criteri di accettazione:
- I segnaposto del template (es. {NOME_TIROCINANTE}, {MONTE_ORE_TOTALE}, {GRIGLIA_ORARIA}) sono sostituiti; il file si apre in Word senza errori.

Domande aperte: Q5

#### US-1005
**Progetto Formativo in Word** · Must · 3 punti · R3 · 📋 Backlog

Come **Tutor / Call Center** voglio generare e scaricare il Progetto Formativo per completare la documentazione.

Criteri di accettazione:
- Stesse regole della Convenzione, con il proprio template.

Dipendenze: US-1004 · Domande aperte: Q5

### E11 — Chiusura e restituzione

_Il percorso si chiude con un report e la traccia immodificabile nell'audit trail dell'azienda._

#### US-1101
**Report di restituzione** · Must · 5 punti · R3 · 📋 Backlog

Come **Tutor** voglio compilare il report finale (gradimenti 1–5, assunzione sì/no, mansione ancora disponibile sì/no) per chiudere il percorso.

Criteri di accettazione:
- Il ticket passa in FORM_RESTITUZIONE; il report non è più modificabile dopo l'invio.

Domande aperte: Q6

#### US-1102
**Esito sulla mansione e audit trail** · Must · 2 punti · R3 · 📋 Backlog

Come **Call Center** voglio che la chiusura aggiorni mansione e storico dell'azienda per avere dati affidabili per i prossimi abbinamenti.

Criteri di accettazione:
- Se la mansione è ancora disponibile torna LIBERA; l'esito entra nell'audit trail immodificabile dell'azienda.

Dipendenze: US-1101

### E12 — Notifiche

_Le persone giuste sono avvisate al momento giusto, in app e via email._

#### US-1201
**Notifiche in app** · Must · 5 punti · R2 · 📋 Backlog

Come **Tutor / Call Center** voglio vedere le notifiche che mi riguardano (promemoria, rilanci, esiti) per non perdere scadenze e richieste.

Criteri di accettazione:
- Contatore di non lette nell'intestazione, annunciato agli screen reader; segna come letta.

Domande aperte: Q9

#### US-1202
**Email transazionali** · Should · 5 punti · R3 · 📋 Backlog

Come **Tutor / Call Center** voglio ricevere per email le notifiche importanti per essere avvisato anche fuori dall'app.

Criteri di accettazione:
- Invio tramite SMTP IONOS da un indirizzo del dominio; nessun dato personale sensibile nel testo dell'email.

Domande aperte: Q9

### E13 — Trasversali e produzione

_Qualità, sicurezza, GDPR e passaggio dalla demo alla produzione._

#### US-1301
**Verifica della PWA online** · Must · 1 punti · R1 · 📋 Backlog

Come **PO** voglio verificare installazione e service worker su smartphone per confermare il requisito PWA.

Criteri di accettazione:
- L'app si installa su Android e iOS; offline si apre la shell con l'avviso.

#### US-1302
**Backup del database** · Must · 5 punti · R4 · 📋 Backlog

Come **ADMIN** voglio backup automatici giornalieri fuori dal server, con prova di ripristino per non perdere dati.

Criteri di accettazione:
- Il ripristino di prova su un ambiente pulito riesce ed è documentato.

#### US-1303
**Anonimizzazione a scadenza** · Must · 5 punti · R4 · 📋 Backlog

Come **responsabile della privacy** voglio che i dati identificativi dei candidati siano anonimizzati alla scadenza della conservazione per rispettare il GDPR.

Criteri di accettazione:
- Restano solo i dati statistici; l'operazione è registrata.

Domande aperte: Q7

#### US-1304
**Tema di Keycloak** · Should · 3 punti · R4 · 📋 Backlog

Come **utente** voglio una pagina di login con l'identità visiva di NEXUS per riconoscere l'app e fidarmi.

Criteri di accettazione:
- Login con la palette NEXUS, 0 violazioni WCAG, in italiano.

#### US-1305
**Revisione UX/UI** · Should · 8 punti · R4 · 📋 Backlog

Come **utente** voglio un'interfaccia studiata sui flussi reali di Tutor e Call Center per lavorare più velocemente.

Criteri di accettazione:
- Da raffinare con il PO in una sessione dedicata (probabile suddivisione in più storie).

#### US-1306
**Passaggio in produzione** · Must · 5 punti · R4 · 📋 Backlog

Come **PO** voglio passare dalla demo alla produzione per usare NEXUS con dati reali.

Criteri di accettazione:
- Niente dati dimostrativi, utenti reali creati dall'ADMIN, backup attivi, checklist di sicurezza superata.

Dipendenze: US-102..105, US-1302, US-1307

#### US-1307
**Monitoraggio e revisione di sicurezza** · Must · 5 punti · R4 · 📋 Backlog

Come **ADMIN** voglio essere avvisato se l'app non risponde e avere una revisione di sicurezza prima della produzione per reagire ai problemi prima degli utenti.

Criteri di accettazione:
- Controllo di disponibilità esterno con avviso; checklist OWASP ASVS di livello 1 verificata.

#### US-1308
**Recupero password** · Should · 2 punti · R4 · 📋 Backlog

Come **utente** voglio reimpostare la password dimenticata per non dipendere dall'amministratore.

Criteri di accettazione:
- Il link di recupero arriva per email ed è valido per un tempo limitato.

Dipendenze: US-1202
