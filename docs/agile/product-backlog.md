# NEXUS — Product Backlog

Ordinato per priorità dal Product Owner. Stime in story point (Fibonacci); stato: 📋 Backlog · ✅ Ready · 🔨 In corso · 👀 In review · 🎉 Done.
Metodo di lavoro, Definition of Ready e Definition of Done: [README](README.md). Release: [Roadmap](roadmap.md).

**Totale: 68 elementi, 240 punti.** La fondazione tecnica (Sprint 0) è già completata: vedi [sprint-00](sprints/sprint-00.md). Sprint in corso: [sprint-07](sprints/sprint-07.md).

## Epiche

| Epica | Titolo | Obiettivo | Storie | Punti |
|---|---|---|---|---|
| E1 | Utenti e progetti | L'ADMIN gestisce utenti, ruoli, progetti, il calendario e i cataloghi senza passare dalla console di Keycloak. | 9 | 27 |
| E2 | Anagrafica beneficiari | Il Tutor registra e mantiene i dati dei tirocinanti, nel rispetto della minimizzazione GDPR. | 5 | 18 |
| E3 | Segnalazioni (fase 1) | Il Tutor segnala un beneficiario per un percorso di tirocinio e ne segue lo stato. | 7 | 28 |
| E4 | Coda Call Center e fast-track (fase 3) | Il Call Center lavora le segnalazioni in ordine FIFO, con priorità alle segnalazioni speciali. | 5 | 17 |
| E5 | Aziende e mansioni | Il Call Center mantiene il database delle aziende ospitanti e delle mansioni offerte. | 6 | 15 |
| E6 | Abbinamento (fase 4) | Il Call Center abbina il beneficiario a una mansione libera e la blocca. | 4 | 17 |
| E7 | Proposta e timer (fase 5) | Il Tutor valuta la proposta; i tempi di risposta sono presidiati da un timer di 7 giorni lavorativi. | 5 | 15 |
| E8 | Appuntamento e rilancio (fase 6) | Il Tutor registra l'esito dell'appuntamento; se fallisce, la mansione può essere rilanciata in bacheca. | 3 | 9 |
| E9 | Bacheca opportunità | Le mansioni disponibili sono visibili ai Tutor in forma anonima e gestite dal Call Center. | 4 | 13 |
| E10 | Tirocinio e documenti (fase 7) | I dati amministrativi del tirocinio producono Convenzione e Progetto Formativo in Word. | 5 | 22 |
| E11 | Chiusura e restituzione | Il percorso si chiude con un report e la traccia immodificabile nell'audit trail dell'azienda. | 2 | 7 |
| E12 | Notifiche | Le persone giuste sono avvisate al momento giusto, in app e via email. | 2 | 10 |
| E13 | Trasversali e produzione | Qualità, sicurezza, audit, GDPR e passaggio dalla demo alla produzione. | 11 | 41 |

## Release

| Release | Obiettivo | Storie | Punti |
|---|---|---|---|
| R1 | Flusso base: dalla segnalazione alla proposta | 25 | 78 |
| R2 | Proposta, timer, appuntamenti, bacheca e fast-track | 21 | 69 |
| R3 | Tirocinio, documenti Word, chiusura, email | 11 | 43 |
| R4 | Produzione: utenti, backup, GDPR, UX, sicurezza | 11 | 50 |

## Domande aperte per il Product Owner

Bloccano la *Definition of Ready* delle storie indicate.

| # | Domanda | Storie | Risposta |
|---|---|---|---|
| Q1 | Rifiuto della segnalazione speciale: il tipo diventa NORMAL o resta SPECIAL senza fast-track? | US-404 | Resta **SPECIAL senza fast-track** (traccia dell'origine dalla bacheca) (PO, 01/10). |
| Q2 | Scadenza del timer: il ticket torna in IN_LAVORAZIONE o in RIAPERTO? | US-705 | **RIAPERTO** (PO, 01/10). |
| Q3 | Il Tutor può rifiutare una proposta? Con quali effetti (es. azienda esclusa automaticamente)? | US-702 | **Sì, con motivo obbligatorio**: ticket al Call Center (RIAPERTO), mansione LIBERA, azienda esclusa per quel ticket (PO, 01/10). |
| Q4 | Festività da escludere: solo nazionali o anche il patrono locale? Chiusure della cooperativa? | US-703, US-107 | Festività nazionali + **patrono configurabile per progetto** (24 giugno per l'attuale) + chiusure; vale il calendario del progetto del ticket (PO, 01/10). **Aggiornata il 24/10 (review dello Sprint 3):** il calendario è **unico per tutti** i progetti: festività nazionali (senza il 4 ottobre), patrono del 24 giugno e chiusure, impostati dall'ADMIN. |
| Q5 | Campi della scheda amministrativa e template Word reali di Convenzione e Progetto Formativo. | US-1001..1005 | Per ora **segnaposto**; i documenti reali arriveranno più avanti. I modelli dovranno essere modificabili (modalità da definire: il PO ci sta pensando) (PO, 01/10). |
| Q6 | Voci esatte dei gradimenti 1–5 del report di restituzione. | US-1101 | Soddisfazione dell'azienda (1–5), soddisfazione del beneficiario (1–5), obiettivi raggiunti (testo), difficoltà riscontrate (testo); assunto sì → formula (menu), no → motivo (menu). Voci dei menu provvisorie, definitive più avanti (PO, 01/10). |
| Q7 | Durata di conservazione dei dati dei beneficiari (decisione della cooperativa/DPO). | US-1303 | **Parametrica**, per ora 2 anni (PO, 01/10). |
| Q8 | Chi approva la mansione a testo libero (ADMIN?) e se, approvata, entra nel catalogo. | US-302 | **ADMIN**; non entra da sola nel catalogo, l'ADMIN può aggiungerla con un clic; se respinta il Tutor vede il motivo (PO, 01/10). **Aggiornata il 24/10:** approva un **operatore** del Call Center (o l'ADMIN) e la tipologia approvata **entra nel catalogo**; se respinta, l'operatore sceglie la tipologia corretta e il Tutor riceve una notifica. |
| Q9 | Canali delle notifiche per evento (solo app, app + email) e destinatari. | US-1201, US-1202 | **In app e via email**, alla controparte del passaggio (Tutor ↔ operatore, già definiti per ogni fase) (PO, 01/10). |
| Q10 | Elenco reale di zone e tipologie di mansione. | US-106 | Maschere di inserimento per l'ADMIN (US-106). Intanto: zone = quartieri di Firenze e comuni dell'hinterland (EN-3); tipologie ancora segnaposto (PO, 01/10). |
| Q11 | Cosa vedono gli altri tutor dello stesso progetto del beneficiario (oltre al divieto su nome e cognome). | US-204 | Ogni tutor gestisce le proprie segnalazioni e vede **complete** (nome e cognome compresi) quelle del proprio progetto: dentro il progetto le informazioni sono condivise (PO, 01/10). |
| Q12 | Un beneficiario può avere più segnalazioni aperte insieme (es. su progetti o mansioni diverse)? | US-301 | **No**: una sola segnalazione aperta alla volta (PO, 01/10). Aperta = qualunque stato tranne FORM_RESTITUZIONE. |
| Q13 | Che cos'è un "servizio", a cui legare il calendario (patrono, chiusure)? | US-107, US-703 | Il "servizio" non esiste: era il **progetto** del Tutor. Il calendario (patrono, chiusure) è del progetto (PO, 01/10). **Aggiornata il 24/10 (review dello Sprint 3):** il calendario è **unico per tutti** i progetti: festività nazionali (senza il 4 ottobre), patrono del 24 giugno e chiusure, impostati dall'ADMIN. |

## Elenco delle storie

| ID | Epica | Storia | Priorità | Punti | Release | Stato |
|---|---|---|---|---|---|---|
| [EN-1](#en-1) | E3 | Motore delle transizioni del ticket | Must | 5 | R1 | 🎉 Done (Sprint 1) |
| [EN-2](#en-2) | E13 | Audit di ogni cambio di stato e scelta | Must | 5 | R1 | 🎉 Done (Sprint 2) |
| [US-101](#us-101) | E1 | Messaggio "utente non abilitato" | Should | 1 | R1 | 🎉 Done (Sprint 1) |
| [US-102](#us-102) | E1 | Gestione progetti | Must | 3 | R4 | 📋 Backlog |
| [US-103](#us-103) | E1 | Creazione utente | Must | 8 | R4 | 📋 Backlog |
| [US-104](#us-104) | E1 | Assegnazione utenti ai progetti | Must | 3 | R4 | 📋 Backlog |
| [US-105](#us-105) | E1 | Disattivazione e cambio ruolo | Must | 3 | R4 | 📋 Backlog |
| [US-106](#us-106) | E1 | Cataloghi di zone e tipologie di mansione | Must | 3 | R2 | 🎉 Done (Sprint 7) |
| [US-108](#us-108) | E1 | Tipi di tirocinio | Must | 1 | R3 | 🎉 Done (Sprint 7) |
| [US-107](#us-107) | E1 | Calendario unico (patrono e chiusure) | Must | 3 | R2 | 🎉 Done (Sprint 4) |
| [EN-3](#en-3) | E1 | Zone iniziali: Firenze e hinterland | Must | 2 | R1 | 🎉 Done (Sprint 2) |
| [EN-4](#en-4) | E13 | Contatti dei referenti aziendali nell'audit senza valori | Must | 1 | R1 | 🎉 Done (Sprint 3) |
| [EN-5](#en-5) | E13 | Controllo della numerazione delle migrazioni nella CI | Must | 1 | R1 | 🎉 Done (Sprint 3) |
| [US-201](#us-201) | E2 | Inserimento beneficiario | Must | 5 | R1 | 🎉 Done (Sprint 1) |
| [US-202](#us-202) | E2 | I miei beneficiari | Must | 3 | R1 | 🎉 Done (Sprint 3) |
| [US-203](#us-203) | E2 | Modifica beneficiario | Must | 2 | R1 | 🎉 Done (Sprint 3) |
| [US-204](#us-204) | E2 | Visibilità dei beneficiari per progetto | Must | 3 | R1 | 🎉 Done (Sprint 3) |
| [US-205](#us-205) | E2 | Curriculum del beneficiario | Could | 5 | R3 | 📋 Backlog |
| [US-301](#us-301) | E3 | Nuova segnalazione | Must | 5 | R1 | 🎉 Done (Sprint 1) |
| [US-302](#us-302) | E3 | Nuova tipologia di mansione dal Tutor, approvata dal Call Center | Should | 5 | R2 | 🎉 Done (Sprint 4) |
| [US-303](#us-303) | E3 | Elenco delle segnalazioni (Home) | Must | 5 | R1 | 🎉 Done (Sprint 3) |
| [US-304](#us-304) | E3 | Ticket dei miei progetti | Should | 0 | R1 | ↪️ Assorbita in US-303 |
| [US-305](#us-305) | E3 | Dettaglio e cronologia del ticket | Must | 3 | R1 | 🎉 Done (Sprint 3) |
| [US-306](#us-306) | E3 | Segnalazione speciale dalla bacheca | Must | 3 | R2 | 🎉 Done (Sprint 7) |
| [US-307](#us-307) | E3 | Scadenza nella Home del Tutor | Must | 2 | R2 | 🎉 Done (Sprint 6) |
| [US-401](#us-401) | E4 | Coda FIFO | Must | 5 | R1 | 🎉 Done (Sprint 1) |
| [US-402](#us-402) | E4 | Presa in carico | Must | 2 | R1 | 🎉 Done (Sprint 2) |
| [US-403](#us-403) | E4 | Approvazione fast-track | Must | 5 | R2 | 🎉 Done (Sprint 7) |
| [US-404](#us-404) | E4 | Rifiuto fast-track | Must | 3 | R2 | 🎉 Done (Sprint 7) |
| [US-405](#us-405) | E4 | Lavorazione condivisa tra operatori | Must | 2 | R1 | 🎉 Done (Sprint 3) |
| [US-501](#us-501) | E5 | Anagrafica aziende | Must | 3 | R1 | 🎉 Done (Sprint 2) |
| [US-502](#us-502) | E5 | Mansioni dell'azienda | Must | 3 | R1 | 🎉 Done (Sprint 2) |
| [US-503](#us-503) | E5 | Storico e pregressi dell'azienda | Must | 3 | R1 | 🎉 Done (Sprint 4) |
| [US-504](#us-504) | E5 | Disattivazione impedita con mansioni bloccate | Must | 1 | R1 | 🎉 Done (Sprint 3) |
| [US-505](#us-505) | E5 | Diario delle note dell'azienda | Must | 2 | R1 | 🎉 Done (Sprint 4) |
| [US-506](#us-506) | E5 | Legale rappresentante e codici ATECO dell'azienda | Must | 3 | R3 | 🎉 Done (Sprint 7) |
| [US-601](#us-601) | E6 | Ricerca mansioni compatibili | Must | 5 | R1 | 🎉 Done (Sprint 2) |
| [US-602](#us-602) | E6 | Abbinamento e proposta | Must | 5 | R1 | 🎉 Done (Sprint 2) |
| [US-603](#us-603) | E6 | Esclusione di un'azienda | Should | 2 | R2 | 🎉 Done (Sprint 5) |
| [US-604](#us-604) | E6 | Dettagli di azienda e mansione durante l'abbinamento | Must | 5 | R1 | 🎉 Done (Sprint 4) |
| [US-701](#us-701) | E7 | Accettazione della proposta | Must | 3 | R2 | 🎉 Done (Sprint 4) |
| [US-702](#us-702) | E7 | Rifiuto della proposta | Should | 3 | R2 | 🎉 Done (Sprint 5) |
| [US-703](#us-703) | E7 | Calendario dei giorni lavorativi | Must | 3 | R2 | 🎉 Done (Sprint 4) |
| [US-704](#us-704) | E7 | Promemoria al 4° giorno | Must | 3 | R2 | 🎉 Done (Sprint 5) |
| [US-705](#us-705) | E7 | Scadenza del timer | Must | 3 | R2 | 🎉 Done (Sprint 5) |
| [US-801](#us-801) | E8 | Appuntamento preso | Must | 2 | R2 | 🎉 Done (Sprint 5) |
| [US-802](#us-802) | E8 | Appuntamento fallito | Must | 3 | R2 | 🎉 Done (Sprint 5) |
| [US-803](#us-803) | E8 | Rilancio in bacheca | Must | 5 | R2 | 🎉 Done (Sprint 6) |
| [US-901](#us-901) | E9 | Bacheca per il Tutor | Must | 3 | R2 | 🎉 Done (Sprint 6) |
| [US-902](#us-902) | E9 | Gestione della bacheca | Must | 5 | R2 | 🎉 Done (Sprint 6) |
| [US-903](#us-903) | E9 | Sblocco a tutti dopo 7 giorni | Must | 2 | R2 | 🎉 Done (Sprint 6) |
| [US-904](#us-904) | E9 | Annuncio da una mansione libera | Should | 3 | R2 | 🎉 Done (Sprint 6) |
| [US-1001](#us-1001) | E10 | Scheda amministrativa del tirocinio | Must | 8 | R3 | 🎉 Done (Sprint 7) |
| [US-1002](#us-1002) | E10 | Griglia oraria e monte ore | Must | 5 | R3 | 🎉 Done (Sprint 7) |
| [US-1003](#us-1003) | E10 | Avvio del tirocinio | Must | 1 | R3 | 📋 Backlog |
| [US-1004](#us-1004) | E10 | Convenzione in Word | Must | 5 | R3 | 📋 Backlog |
| [US-1005](#us-1005) | E10 | Progetto Formativo in Word | Must | 3 | R3 | 📋 Backlog |
| [US-1101](#us-1101) | E11 | Report di restituzione | Must | 5 | R3 | 📋 Backlog |
| [US-1102](#us-1102) | E11 | Esito sulla mansione e audit trail | Must | 2 | R3 | 📋 Backlog |
| [US-1201](#us-1201) | E12 | Notifiche in app | Must | 5 | R2 | 🎉 Done (Sprint 5) |
| [US-1202](#us-1202) | E12 | Email transazionali | Must | 5 | R3 | 📋 Backlog |
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
**Messaggio "utente non abilitato"** · Should · 1 punti · R1 · 🎉 Done (Sprint 1)

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
**Cataloghi di zone e tipologie di mansione** · Must · 3 punti · R2 · 🎉 Done (Sprint 7)

Come **ADMIN** voglio inserire, modificare e disattivare zone e tipologie di mansione da maschere dedicate per usare i valori reali della cooperativa senza rilasci.

Criteri di accettazione:
- Quando disattivo una voce, allora non è più selezionabile ma resta sui dati esistenti.
- Ogni modifica è registrata nell'audit (EN-2).

#### US-108
**Tipi di tirocinio** · Must · 1 punti · R3 · 🎉 Done (Sprint 7)

Come **ADMIN** voglio gestire il catalogo dei tipi di tirocinio per classificare ogni tirocinio e scegliere i
documenti giusti. Richiesta del PO, 29/10.

Criteri di accettazione:
- Terzo catalogo della pagina "Cataloghi", con le stesse regole di US-106 (aggiunta, nuovo nome, disattivazione, audit).
- Voci iniziali: Curriculare, Extracurriculare, Di inclusione, Di inserimento socio-terapeutico.
- Il Tutor sceglie il tipo nella scheda del tirocinio (US-1001). Il tipo determina il tipo di documento (US-1004,
  US-1005); altri parametri legati al tipo arriveranno più avanti.

#### EN-3
**Zone iniziali: Firenze e hinterland** · Must · 2 punti · R1 · 🎉 Done (Sprint 2)

Come **PO** voglio che le zone disponibili siano i quartieri di Firenze e i comuni dell'hinterland per lavorare subito con dati realistici, in attesa delle maschere dell'ADMIN (US-106).

Criteri di accettazione:
- Zone come dati iniziali (anche in produzione): Novoli, Isolotto, Legnaia, Rifredi, Campo di Marte, Santa Croce, Oltrarno, Galluzzo, Le Piagge, Rovezzano, Le Cure, Trespiano, Sorgane, Casellina, Soffiano, Serpiolle, Castello, San Niccolò, Varlungo, Santo Spirito, Due Strade, Porta al Prato, San Giovanni, Statuto, Europa, Gavinana, San Lorenzo, San Frediano, San Miniato, San Marco; Scandicci, Sesto Fiorentino, Campi Bisenzio, Borgo San Lorenzo, Scarperia e San Piero, Vicchio, Dicomano, Bagno a Ripoli, Impruneta, Fiesole.
- Le zone segnaposto della demo vengono disattivate e i dati demo spostati sulle nuove zone.

#### US-107
**Calendario unico (patrono e chiusure)** · Must · 3 punti · R2 · 🎉 Done (Sprint 4)

Come **ADMIN** voglio impostare il patrono e le chiusure della cooperativa per calcolare le scadenze sui giorni lavorativi
effettivi. Aggiornata nella review dello Sprint 3 (24/10): il calendario è **unico per tutti** i progetti.

Criteri di accettazione:
- Pagina "Calendario" per l'ADMIN: patrono (24 giugno) e chiusure, come date singole o periodi, con descrizione;
  modifiche registrate nell'audit.
- Un solo calendario, valido per tutti i progetti e per tutte le segnalazioni.
### E2 — Anagrafica beneficiari

_Il Tutor registra e mantiene i dati dei tirocinanti, nel rispetto della minimizzazione GDPR._

#### US-201
**Inserimento beneficiario** · Must · 5 punti · R1 · 🎉 Done (Sprint 1)

Come **Tutor** voglio registrare un beneficiario con i dati previsti (anagrafica minima, patente, mezzi, L. 68/99, titolo di studio, vincoli, lingue) per poterlo segnalare per un tirocinio.

Criteri di accettazione:
- Quando compilo il modulo con dati validi, allora il beneficiario è salvato e ne sono il tutor proprietario.
- Quando un campo non è valido (es. anno di nascita futuro, livello linguistico non ammesso), allora vedo l'errore accanto al campo, annunciato agli screen reader.
- Il modulo è utilizzabile solo da tastiera e su smartphone a 320 px.

#### US-202
**I miei beneficiari** · Must · 3 punti · R1 · 🎉 Done (Sprint 3)

Come **Tutor** voglio vedere l'elenco e il dettaglio dei beneficiari che ho inserito per ritrovarli rapidamente.

Criteri di accettazione:
- Vedo solo i beneficiari di cui sono proprietario, con ricerca per nome.

#### US-203
**Modifica beneficiario** · Must · 2 punti · R1 · 🎉 Done (Sprint 3)

Come **Tutor** voglio correggere i dati di un mio beneficiario per tenerli aggiornati.

Criteri di accettazione:
- Posso modificare solo i miei beneficiari; le modifiche sono tracciate (autore e data).

#### US-204
**Visibilità dei beneficiari per progetto** · Must · 3 punti · R1 · 🎉 Done (Sprint 3)

Come **responsabile della privacy** voglio che i dati di un beneficiario siano visibili solo a chi ne ha diritto: il tutor proprietario, i tutor dei progetti in cui il beneficiario ha segnalazioni, il Call Center e l'ADMIN.

Criteri di accettazione:
- Un tutor dello stesso progetto vede i dati completi (nome e cognome compresi): nel progetto le informazioni sono condivise (Q11).
- Un tutor di altri progetti non vede né il beneficiario né le sue segnalazioni; il controllo è nelle API, non solo nell'interfaccia.

#### US-205
**Curriculum del beneficiario** · Could · 5 punti · R3 · 📋 Backlog

Come **Tutor** voglio allegare il CV (PDF) al beneficiario per metterlo a disposizione del Call Center per l'abbinamento.

Criteri di accettazione:
- Accetta solo PDF fino a 5 MB; il file non è accessibile senza autorizzazione.
- Il CV si scarica solo da utenti autorizzati a vedere il beneficiario.

### E3 — Segnalazioni (fase 1)

_Il Tutor segnala un beneficiario per un percorso di tirocinio e ne segue lo stato._

#### EN-1
**Motore delle transizioni del ticket** · Must · 5 punti · R1 · 🎉 Done (Sprint 1)

Come **team di sviluppo** voglio un servizio unico che applica le transizioni di stato ammesse per ruolo, registra lo storico e rifiuta le transizioni non valide per ogni storia delle fasi 1–7 aggiunga solo le proprie transizioni senza duplicare regole.

Criteri di accettazione:
- Dato un ticket in uno stato, quando si richiede una transizione non ammessa, allora il sistema risponde 409 e lo stato non cambia.
- Quando una transizione riesce, allora viene aggiunta una riga a `ticket_status_history` con autore e nota.
- Quando due richieste concorrenti modificano lo stesso ticket, allora la seconda riceve 409 (blocco ottimistico).

#### US-301
**Nuova segnalazione** · Must · 5 punti · R1 · 🎉 Done (Sprint 1)

Come **Tutor** voglio segnalare un mio beneficiario per un progetto indicando la mansione desiderata dal catalogo per avviare il percorso di tirocinio.

Criteri di accettazione:
- Quando invio la segnalazione, allora il ticket nasce in stato NUOVA con un numero progressivo ed entra nella coda del Call Center.
- Posso scegliere solo tra i progetti a cui sono assegnato.
- Un beneficiario ha al più una segnalazione aperta: se ne ha già una, non posso inviarne un'altra (Q12).

Dipendenze: EN-1, US-201

#### US-302
**Nuova tipologia di mansione dal Tutor, approvata dal Call Center** · Should · 5 punti · R2 · 🎉 Done (Sprint 4)

Come **Tutor** voglio indicare nella segnalazione una tipologia di mansione che non trovo nel catalogo, che un operatore
del Call Center approva, per non bloccare segnalazioni atipiche. Aggiornata con la nota operativa del PO (24/10): approva
l'**operatore**, non l'ADMIN.

Criteri di accettazione:
- Se la mansione non è nell'elenco, il Tutor la scrive; la segnalazione nasce in attesa di approvazione e compare al
  Call Center come "da approvare".
- Un operatore (o l'ADMIN) approva, potendo correggerne il nome: la tipologia **entra nel catalogo** e la segnalazione
  passa in NUOVA (entra in coda); evento di audit.
- Se l'operatore respinge la proposta, **sceglie lui una tipologia corretta del catalogo** e indica il motivo; la
  segnalazione entra in coda con quella tipologia e il Tutor riceve una **notifica** con il motivo (PO, 24/10).
  Finché non ci sono le notifiche (US-1201): evento "Tipologia cambiata dal Call Center" nella cronologia (proposta →
  scelta, motivo) ed evidenza testuale "Tipologia cambiata" nella Home del Tutor finché non apre la segnalazione
  (review dello Sprint 3, §8.2).
- Lo stato `IN_ATTESA_APPROVAZIONE_ADMIN` diventa `IN_ATTESA_APPROVAZIONE` (non è più solo dell'ADMIN).

#### US-303
**Elenco delle segnalazioni (Home)** · Must · 5 punti · R1 · 🎉 Done (Sprint 3)

Come **Tutor, operatore Call Center o ADMIN** voglio che la pagina iniziale sia l'elenco delle segnalazioni che mi
riguardano per partire subito dal lavoro. Nota operativa del PO, decisa il 24/10: sostituisce "I miei ticket" e assorbe
US-304 e gli elenchi di US-401/US-402.

Criteri di accettazione:
- La Home di tutti i ruoli è la pagina "Segnalazioni"; numero, beneficiario, progetto, mansione, stato in testo e data;
  ogni numero apre il dettaglio con la cronologia (US-305).
- Tutor: vista predefinita "Le mie segnalazioni" e vista "Del mio progetto" (segnalazioni dei colleghi complete, in sola
  lettura; nessuna di altri progetti); pulsante "Nuova segnalazione".
- Call Center e ADMIN: viste "Da prendere in carico" (predefinita: la coda FIFO con le fast-track in cima, con il
  pulsante "Prendi in carico"), "In carico a me" e "Tutte"; le pagine "Coda" e "Le mie lavorazioni" spariscono dal menu.
- Filtri per stato e progetto in ogni vista, **nessun filtro per zona** (nota 4 del PO); vista e filtri nell'indirizzo;
  numero di risultati annunciato.

#### US-304
**Ticket dei miei progetti** · Should · 0 punti · R1 · ↪️ Assorbita in US-303 (24/10)

Confluita nella vista "Del mio progetto" della Home (US-303).

#### US-305
**Dettaglio e cronologia del ticket** · Must · 3 punti · R1 · 🎉 Done (Sprint 3)

Come **Tutor / Call Center** voglio vedere il dettaglio del ticket e la cronologia degli stati per sapere cosa è successo e quando.

Criteri di accettazione:
- La cronologia mostra per ogni cambio di stato data, autore e nota.

#### US-306
**Segnalazione speciale dalla bacheca** · Must · 3 punti · R2 · 🎉 Done (Sprint 7)

Come **Tutor** voglio candidare un mio beneficiario direttamente a un post #N della bacheca per ottenere una corsia prioritaria (fast-track).

Criteri di accettazione:
- Il ticket nasce SPECIAL, fast-track, in IN_LAVORAZIONE, collegato al post, con badge prioritario per il Call Center.
- Dalla bacheca il Tutor candida un suo beneficiario senza segnalazioni aperte; la segnalazione ha la tipologia della mansione dell'annuncio (review dello Sprint 6, §8.2).
- Un annuncio accetta una sola candidatura: la prima lo blocca finché il Call Center non decide; in bacheca l'annuncio bloccato mostra «Candidatura in valutazione», senza il pulsante (review dello Sprint 6, §8.1).

Dipendenze: US-901

#### US-307
**Scadenza nella Home del Tutor** · Must · 2 punti · R2 · 🎉 Done (Sprint 6)

Come **Tutor** voglio vedere nella Home la scadenza delle mie segnalazioni per non perdere il termine del passo in corso.

Criteri di accettazione:
- Nell'elenco del Tutor ("Le mie" e "Del progetto") la colonna "Scadenza" mostra l'ultimo giorno utile del timer attivo (es. "entro mer 04/11"), vuota se non c'è scadenza.
- Dal promemoria (4° giorno lavorativo) la scadenza è in grassetto con la scritta "in scadenza": l'informazione non è affidata al solo colore.
- Le segnalazioni con la scadenza più vicina sono in cima all'elenco.
- Scadenze future (es. sblocco della bacheca) useranno la stessa colonna.

Origine: note operative del PO, review dello Sprint 5 §8.3 (28/10).

Dipendenze: US-704

### E4 — Coda Call Center e fast-track (fase 3)

_Il Call Center lavora le segnalazioni in ordine FIFO, con priorità alle segnalazioni speciali._

#### US-401
**Coda FIFO** · Must · 5 punti · R1 · 🎉 Done (Sprint 1)

Come **operatore Call Center** voglio vedere la coda delle segnalazioni da lavorare, prima le speciali poi in ordine di arrivo per lavorare in modo equo e rapido.

Criteri di accettazione:
- La coda mostra NUOVA e IN_LAVORAZIONE non assegnate; le fast-track in cima con badge testuale.
- Filtri per progetto e zona; vista ottimizzata per desktop.

#### US-402
**Presa in carico** · Must · 2 punti · R1 · 🎉 Done (Sprint 2)

Come **operatore Call Center** voglio prendere in carico un ticket per evitare che due operatori lavorino lo stesso caso.

Criteri di accettazione:
- Il ticket passa in IN_LAVORAZIONE assegnato a me; se un collega lo ha preso un istante prima, ricevo un avviso (409).

Dipendenze: EN-1

#### US-403
**Approvazione fast-track** · Must · 5 punti · R2 · 🎉 Done (Sprint 7)

Come **operatore Call Center** voglio approvare una segnalazione speciale per trasformarla subito in proposta all'azienda.

Criteri di accettazione:
- All'approvazione: ticket in PROPOSTA_AZIENDA, mansione del post BLOCCATA, post ARCHIVED, in un'unica operazione.

Dipendenze: US-306

#### US-404
**Rifiuto fast-track** · Must · 3 punti · R2 · 🎉 Done (Sprint 7)

Come **operatore Call Center** voglio rifiutare una segnalazione speciale per declassarla a lavorazione normale.

Criteri di accettazione:
- Al rifiuto: fast-track disattivato, il tipo resta SPECIAL (traccia dell'origine), ticket in IN_LAVORAZIONE, post ancora PUBLISHED e di nuovo candidabile (review dello Sprint 6, §8.1).
- Il rifiuto richiede un motivo ed è registrato nell'audit (EN-2).

#### US-405
**Lavorazione condivisa tra operatori** · Must · 2 punti · R1 · 🎉 Done (Sprint 3)

Come **operatore Call Center** voglio poter lavorare anche le segnalazioni prese in carico da un collega per non
bloccare il lavoro quando un collega è assente. Decisione del PO nella review dello Sprint 2 (05/10).

Criteri di accettazione:
- Ogni operatore del Call Center (e l'ADMIN) può lavorare e abbinare una segnalazione in lavorazione anche se è in
  carico a un collega; l'assegnazione non cambia e audit e cronologia registrano chi ha agito.
- Nella lavorazione di una segnalazione di un collega compare "In carico a …" e il pulsante "Prendila in carico tu",
  che sposta l'assegnazione su di sé con evento di audit (operatore prima/dopo).
- "Le mie lavorazioni" offre anche la vista "Tutte le lavorazioni" del Call Center, con la colonna dell'operatore.

### E5 — Aziende e mansioni

_Il Call Center mantiene il database delle aziende ospitanti e delle mansioni offerte._

#### US-501
**Anagrafica aziende** · Must · 3 punti · R1 · 🎉 Done (Sprint 2)

Come **operatore Call Center** voglio creare, modificare e disattivare aziende (ragione sociale, P.IVA, sede, referente, contatti) per avere un database affidabile per il matching.

Criteri di accettazione:
- La P.IVA è validata (11 cifre) e univoca; un'azienda con storico si disattiva, non si cancella.

#### US-502
**Mansioni dell'azienda** · Must · 3 punti · R1 · 🎉 Done (Sprint 2)

Come **operatore Call Center** voglio gestire le mansioni di un'azienda (titolo, descrizione, tipologia, zona) per sapere quali posizioni sono disponibili.

Criteri di accettazione:
- Lo stato LIBERA/BLOCCATA è visibile e non si modifica a mano: lo governano abbinamenti e chiusure.

#### US-503
**Storico e pregressi dell'azienda** · Must · 3 punti · R1 · 🎉 Done (Sprint 4)

Come **operatore Call Center / ADMIN** voglio vedere nella scheda dell'azienda lo storico immodificabile e i pregressi
(segnalazioni passate ed esiti) per valutare l'azienda prima di proporle un beneficiario. Aggiornata con la nota operativa
del PO (24/10): anticipata a R1 e mostrata anche durante l'abbinamento (US-604).

Criteri di accettazione:
- Riepilogo in testo: proposte, accettate, appuntamenti falliti, tirocini, assunzioni, esiti negativi.
- Elenco cronologico in sola lettura: segnalazione collegata, evento ed esito, autore, data; esclusioni con motivo.
- Visibile solo a Call Center e ADMIN.
- Le voci del riepilogo compaiono sempre, a zero finché i relativi eventi non esistono (arriveranno con R2 e R3), così la
  scheda non cambia forma (review dello Sprint 3, 24/10).

#### US-504
**Disattivazione impedita con mansioni bloccate** · Must · 1 punti · R1 · 🎉 Done (Sprint 3)

Come **operatore Call Center** voglio che non si possa disattivare un'azienda con mansioni bloccate da proposte in corso
per non far sparire dalle ricerche un'azienda che sta ancora valutando un beneficiario. Decisione del PO nella review
dello Sprint 2 (05/10).

Criteri di accettazione:
- La disattivazione è rifiutata (409) finché l'azienda ha mansioni bloccate; il messaggio indica mansione e numero della
  segnalazione (es. "la mansione «Magazziniere» è bloccata dalla segnalazione n. 30").

#### US-505
**Diario delle note dell'azienda** · Must · 2 punti · R1 · 🎉 Done (Sprint 4)

Come **operatore Call Center** voglio annotare informazioni utili sull'azienda in un diario di note datate per
condividerle con i colleghi e ritrovarle negli abbinamenti futuri. Nota operativa del PO, decisa il 24/10.

Criteri di accettazione:
- Nella scheda azienda, sezione "Note": elenco dal più recente con data, ora e autore; "Aggiungi nota" (testo
  obbligatorio, fino a 2000 caratteri). Le note si aggiungono, non si sovrascrivono né si cancellano.
- Visibili solo a Call Center e ADMIN.
- Audit: evento "nota aggiunta" sull'azienda, **senza il testo** (può contenere dati personali, come EN-4).

#### US-506
**Legale rappresentante e codici ATECO dell'azienda** · Must · 3 punti · R3 · 🎉 Done (Sprint 7)

Come **operatore Call Center** voglio registrare nella scheda dell'azienda il legale rappresentante e i codici ATECO
per avere i dati pronti per i documenti dei tirocini. Richiesta del PO, 29/10.

Criteri di accettazione:
- Legale rappresentante: nome, cognome, codice fiscale (con controllo formale), data e luogo di nascita, residenza.
  Può non coincidere con il referente.
- Codici ATECO: uno o più per azienda, con descrizione facoltativa; si aggiungono e si tolgono.
- Sono dati personali: visibili a Call Center e ADMIN e, per i propri tirocini, al Tutor; mai nei log; nell'audit solo
  che cosa è cambiato, senza i valori del legale rappresentante.

Dipendenze: US-501

### E6 — Abbinamento (fase 4)

_Il Call Center abbina il beneficiario a una mansione libera e la blocca._

#### US-601
**Ricerca mansioni compatibili** · Must · 5 punti · R1 · 🎉 Done (Sprint 2)

Come **operatore Call Center** voglio trovare le mansioni libere compatibili con il beneficiario (zona, tipologia) escludendo le aziende in blacklist per abbinare velocemente.

Criteri di accettazione:
- Sono proposte solo mansioni LIBERE di aziende attive e non escluse per quel ticket.

Dipendenze: US-502

#### US-602
**Abbinamento e proposta** · Must · 5 punti · R1 · 🎉 Done (Sprint 2)

Come **operatore Call Center** voglio assegnare una mansione al ticket per proporre il beneficiario all'azienda.

Criteri di accettazione:
- La mansione diventa BLOCCATA dal ticket e il ticket passa in PROPOSTA_AZIENDA.
- Se la mansione è stata appena bloccata da un altro ticket, l'abbinamento fallisce con un messaggio chiaro.

Dipendenze: EN-1, US-601

#### US-603
**Esclusione di un'azienda** · Should · 2 punti · R2 · 🎉 Done (Sprint 5)

Come **operatore Call Center** voglio escludere un'azienda per un ticket, con motivo per non riproporla allo stesso beneficiario.

Criteri di accettazione:
- Un'azienda esclusa non compare più nella ricerca per quel ticket.

#### US-604
**Dettagli di azienda e mansione durante l'abbinamento** · Must · 5 punti · R1 · 🎉 Done (Sprint 4)

Come **operatore Call Center** voglio aprire, dalla ricerca delle mansioni compatibili, una finestra con la scheda
dell'azienda, la scheda della mansione, le note e i pregressi dell'azienda per scegliere la proposta con tutte le
informazioni. Nota operativa del PO, decisa il 24/10.

Criteri di accettazione:
- Pulsante "Dettagli" accanto a "Proponi" per ogni mansione compatibile; la finestra mostra: scheda azienda (ragione
  sociale, sede, referente e contatti, stato), scheda mansione (titolo, tipologia, zona, descrizione), note più recenti
  (US-505) e pregressi con riepilogo (US-503); da qui si può proporre con la stessa conferma.
- Dialogo modale accessibile: focus nella finestra e intrappolato finché è aperta, chiusura con Esc o "Chiudi", ritorno
  del focus sul pulsante di partenza; a schermo intero sui dispositivi piccoli.

Dipendenze: US-503, US-505

### E7 — Proposta e timer (fase 5)

_Il Tutor valuta la proposta; i tempi di risposta sono presidiati da un timer di 7 giorni lavorativi._

#### US-701
**Accettazione della proposta** · Must · 3 punti · R2 · 🎉 Done (Sprint 4)

Come **Tutor** voglio accettare la proposta per il mio beneficiario per far partire il contatto con l'azienda.

Criteri di accettazione:
- Il ticket passa in PROPOSTA_ACCOLTA e parte il timer di 7 giorni lavorativi, con scadenza visibile.
- Accetta solo il Tutor della segnalazione: i tutor, di norma, non sono intercambiabili come gli operatori del Call
  Center (PO, 27/10).

Dipendenze: US-703

#### US-702
**Rifiuto della proposta** · Should · 3 punti · R2 · 🎉 Done (Sprint 5)

Come **Tutor** voglio rifiutare la proposta con motivo per tornare alla ricerca di un'altra mansione.

Criteri di accettazione:
- Il motivo è obbligatorio; la mansione torna LIBERA, il ticket torna al Call Center in RIAPERTO e l'azienda è esclusa per quel ticket.
- L'operatore riceve la notifica (in app e via email).

#### US-703
**Calendario dei giorni lavorativi** · Must · 3 punti · R2 · 🎉 Done (Sprint 4)

Come **team di sviluppo** voglio calcolare le scadenze escludendo sabati, domeniche e festività per avere timer corretti.

Criteri di accettazione:
- La scadenza di 7 giorni lavorativi salta sabati, domeniche, festività nazionali, il patrono e le chiusure del calendario
  unico (US-107).
- Festività nazionali calcolate nel codice: 1/1, 6/1, 25/4, 1/5, 2/6, 15/8, 1/11, 8/12, 25/12, 26/12, Pasqua e
  Pasquetta. Il 4 ottobre **non** è considerato festivo (PO, 24/10).

Dipendenze: US-107

#### US-704
**Promemoria al 4° giorno** · Must · 3 punti · R2 · 🎉 Done (Sprint 5)

Come **Tutor** voglio ricevere un solo promemoria al 4° giorno lavorativo senza azioni per non far scadere la proposta.

Criteri di accettazione:
- Il promemoria parte una sola volta anche se il job gira più volte (idempotenza).
- Il controllo gira ogni ora: il promemoria arriva entro un'ora dall'inizio del 4° giorno lavorativo (planning dello
  Sprint 5).

Dipendenze: US-1201

#### US-705
**Scadenza del timer** · Must · 3 punti · R2 · 🎉 Done (Sprint 5)

Come **Call Center** voglio che allo scadere del 7° giorno lavorativo la mansione torni libera per non tenere bloccate posizioni inattive.

Criteri di accettazione:
- Allo scadere: mansione LIBERA, ticket in RIAPERTO, evento nello storico e nell'audit; notifica al Tutor e all'operatore.
- Il timer scade alla fine del 7° giorno lavorativo (mezzanotte di Roma); il controllo gira ogni ora (planning dello
  Sprint 5).

### E8 — Appuntamento e rilancio (fase 6)

_Il Tutor registra l'esito dell'appuntamento; se fallisce, la mansione può essere rilanciata in bacheca._

#### US-801
**Appuntamento preso** · Must · 2 punti · R2 · 🎉 Done (Sprint 5)

Come **Tutor** voglio registrare che l'appuntamento con l'azienda è fissato per avanzare il percorso.

Criteri di accettazione:
- Il ticket passa in APPUNTAMENTO e il timer si ferma.

#### US-802
**Appuntamento fallito** · Must · 3 punti · R2 · 🎉 Done (Sprint 5)

Come **Tutor** voglio registrare che l'appuntamento non è andato a buon fine per riaprire la ricerca.

Criteri di accettazione:
- Il ticket passa in RIAPERTO e la mansione torna LIBERA; l'evento entra nell'audit trail dell'azienda.
- Il Tutor indica il motivo: causa da un menu ("L'azienda non ha confermato il tirocinio", "Il beneficiario ha
  rinunciato", "Il beneficiario non si è presentato", "Altro"; voci provvisorie) e nota, obbligatoria con "Altro"; il
  motivo va in cronologia e nello storico dell'azienda. Nessuna esclusione automatica dell'azienda: decide l'operatore
  (review dello Sprint 4, §8.2).
- La segnalazione riaperta resta in carico allo stesso operatore, in cima a "In carico a me" con l'evidenza "Riaperta",
  e si può abbinare di nuovo (review dello Sprint 4, §8.2). Stima portata da 2 a 3 punti nel planning dello Sprint 5.

#### US-803
**Rilancio in bacheca** · Must · 5 punti · R2 · 🎉 Done (Sprint 6)

Come **Tutor** voglio chiedere il rilancio della mansione in bacheca dopo un appuntamento fallito per offrirla ad altri beneficiari.

Criteri di accettazione:
- Nel modulo dell'appuntamento fallito (US-802) il Tutor trova la casella "Chiedi il rilancio in bacheca" (review Sprint 5, §8.1).
- Si crea una bozza di post precompilata dalla mansione, riservata al progetto del ticket, collegata al ticket.
- La bozza non è visibile ai Tutor finché il Call Center non la pubblica (US-902).
- Il Call Center riceve una notifica.

Dipendenze: US-802, US-1201

### E9 — Bacheca opportunità

_Le mansioni disponibili sono visibili ai Tutor in forma anonima e gestite dal Call Center._

#### US-901
**Bacheca per il Tutor** · Must · 3 punti · R2 · 🎉 Done (Sprint 6)

Come **Tutor** voglio consultare gli annunci pubblici e quelli dei miei progetti, senza il nome dell'azienda per proporre beneficiari adatti.

Criteri di accettazione:
- Il nome dell'azienda non arriva mai al browser del tutor (filtrato dall'API).
- Il Tutor non vede né il titolo né le note dell'annuncio (possono contenere il nome dell'azienda): solo numero (#N), tipologia, zona, fascia d'età, orario, durata e l'indicazione "solo per il tuo progetto" per i post riservati (review Sprint 5, §8.2).
- Un annuncio vive solo finché la sua mansione è disponibile: abbinamento, ritiro della mansione o disattivazione dell'azienda lo archiviano, con il motivo nell'audit (decisione del PO, 28/10).
- Annunci filtrabili per zona e tipologia; consultabili da smartphone.

#### US-902
**Gestione della bacheca** · Must · 5 punti · R2 · 🎉 Done (Sprint 6)

Come **operatore Call Center** voglio vedere le bozze, modificarle, pubblicarle e archiviarle per controllare cosa è visibile ai tutor.

Criteri di accettazione:
- Solo Call Center e ADMIN modificano i post; la pubblicazione registra la data.

#### US-903
**Sblocco a tutti dopo 7 giorni** · Must · 2 punti · R2 · 🎉 Done (Sprint 6)

Come **operatore Call Center** voglio rendere pubblico un post riservato a un progetto per allargare la platea se nessuno risponde.

Criteri di accettazione:
- Il comando è disponibile solo dopo 7 giorni solari dalla pubblicazione; registra la data dello sblocco.

#### US-904
**Annuncio da una mansione libera** · Should · 3 punti · R2 · 🎉 Done (Sprint 6)

Come **operatore Call Center** voglio creare un annuncio partendo da una mansione libera per promuovere le posizioni aperte.

Criteri di accettazione:
- Una mansione può avere un solo annuncio attivo (bozza o pubblicato).

### E10 — Tirocinio e documenti (fase 7)

_I dati amministrativi del tirocinio producono Convenzione e Progetto Formativo in Word._

#### US-1001
**Scheda amministrativa del tirocinio** · Must · 8 punti · R3 · 🎉 Done (Sprint 7)

Come **Tutor** voglio compilare i dati del tirocinio (date, monte ore, dati anagrafici necessari come il codice fiscale) per preparare i documenti.

Criteri di accettazione:
- I campi e le validazioni seguono i moduli ufficiali della cooperativa.
- Fino ai moduli reali (Q5) si usano campi segnaposto, sostituibili senza cambiare il flusso (review dello Sprint 6, §8.3).
- La compila il Tutor della segnalazione dopo l'appuntamento fissato e la corregge in qualunque momento, anche a
  tirocinio avviato (PO, 29/10). Scheda e documenti sono competenza del Tutor: il Call Center non la vede (PO, 29/10).
- Campi (PO, 28–29/10):
  - tipo di tirocinio (US-108), data di inizio, data di fine prevista, sede del tirocinio; l'orario è la griglia di
    US-1002, che ha sostituito le ore settimanali;
  - uno o più tutor aziendali (nome e recapito);
  - il codice ATECO giusto, scelto tra quelli dell'azienda (US-506). Se manca, il Tutor lo aggiunge da qui e va nella
    scheda dell'azienda;
  - il legale rappresentante dell'azienda (US-506). Se manca, il Tutor lo inserisce da qui; correggerlo resta al Call
    Center;
  - dati del beneficiario per i documenti: codice fiscale, data e luogo di nascita, residenza. Sono solo nella scheda
    del tirocinio, non nell'anagrafica (minimizzazione), e rientrano nell'anonimizzazione (US-1303);
  - periodi di sospensione previsti (dal–al, più righe, senza sovrapposizioni), anticipati da US-1002;
  - note.

Dipendenze: US-108, US-506 · Domande aperte: Q5

#### US-1002
**Griglia oraria e monte ore** · Must · 5 punti · R3 · 🎉 Done (Sprint 7)

Come **Tutor** voglio definire l'orario settimanale per calcolare correttamente il monte ore.

Criteri di accettazione:
- Il monte ore si calcola da griglia oraria, date e periodi di sospensione, e si ricalcola quando cambiano.
- I periodi di sospensione si inseriscono nella scheda del tirocinio (US-1001, dal 29/10); qui si usano per il calcolo.
- Griglia (PO, 29/10): due fasce per tirocinio, precompilate 9–13 e 14–18 e modificabili; ogni giorno usa una o
  entrambe le fasce. Le ore si indicano **per giorno**: se mancano valgono le fasce intere (es. 9–13 e 14–18, ma 3 ore).
- Le festività nazionali si escludono dal monte ore con una scelta nella scheda, attiva di default; il patrono no
  (dipende dalla sede: se serve si inserisce come sospensione).
- Totale settimanale al massimo di 40 ore (PO, 29/10).
- Stima portata da 3 a 5 punti (PO, 29/10).

Domande aperte: Q5

#### US-1003
**Avvio del tirocinio** · Must · 1 punti · R3 · 📋 Backlog

Come **Tutor** voglio confermare l'avvio del tirocinio per tracciarne l'inizio.

Criteri di accettazione:
- Il ticket passa in IN_TIROCINIO solo con scheda completa.
- L'avvio non blocca la scheda: il Tutor la corregge anche a tirocinio avviato (PO, 29/10).

#### US-1004
**Convenzione in Word** · Must · 5 punti · R3 · 📋 Backlog

Come **Tutor** voglio generare e scaricare la Convenzione compilata per evitare la compilazione manuale. I documenti
sono competenza del Tutor (PO, 29/10).

Criteri di accettazione:
- I segnaposto del template (es. {NOME_TIROCINANTE}, {MONTE_ORE_TOTALE}, {GRIGLIA_ORARIA}) sono sostituiti; il file si apre in Word senza errori.
- Per ora modelli segnaposto; i modelli dovranno essere modificabili (modalità da definire con il PO).
- Il modello dipende dal tipo di tirocinio (US-108, PO 29/10); altri parametri da definire.

Domande aperte: Q5

#### US-1005
**Progetto Formativo in Word** · Must · 3 punti · R3 · 📋 Backlog

Come **Tutor** voglio generare e scaricare il Progetto Formativo per completare la documentazione.

Criteri di accettazione:
- Stesse regole della Convenzione, con il proprio template.

Dipendenze: US-1004 · Domande aperte: Q5

### E11 — Chiusura e restituzione

_Il percorso si chiude con un report e la traccia immodificabile nell'audit trail dell'azienda._

#### US-1101
**Report di restituzione** · Must · 5 punti · R3 · 📋 Backlog

Come **Tutor** voglio compilare il report finale (gradimenti 1–5, assunzione sì/no, mansione ancora disponibile sì/no) per chiudere il percorso.

Criteri di accettazione:
- Campi: soddisfazione dell'azienda (1–5), soddisfazione del beneficiario (1–5), obiettivi raggiunti (testo), difficoltà riscontrate (testo), mansione ancora disponibile sì/no.
- Assunto sì → formula da menu (tempo determinato 3/6/12 mesi, tempo indeterminato, contratto a chiamata, prestazione occasionale); no → motivo da menu (l'azienda non ha risorse; giudica il tirocinante non idoneo; non aveva mai espresso l'intenzione di assumere). Le voci dei menu sono provvisorie: devono poter cambiare senza rilascio.
- Il ticket passa in FORM_RESTITUZIONE; il report non è più modificabile dopo l'invio.

#### US-1102
**Esito sulla mansione e audit trail** · Must · 2 punti · R3 · 📋 Backlog

Come **Call Center** voglio che la chiusura aggiorni mansione e storico dell'azienda per avere dati affidabili per i prossimi abbinamenti.

Criteri di accettazione:
- Se la mansione è ancora disponibile torna LIBERA; l'esito entra nell'audit trail immodificabile dell'azienda.

Dipendenze: US-1101

### E12 — Notifiche

_Le persone giuste sono avvisate al momento giusto, in app e via email._

#### US-1201
**Notifiche in app** · Must · 5 punti · R2 · 🎉 Done (Sprint 5)

Come **Tutor / Call Center** voglio vedere le notifiche che mi riguardano (promemoria, rilanci, esiti) per non perdere scadenze e richieste.

Criteri di accettazione:
- Contatore di non lette nell'intestazione, annunciato agli screen reader; segna come letta.
- Ogni evento è notificato alla controparte del passaggio (Tutor ↔ operatore).
- Elenco dalla più recente con link alla segnalazione; segna come letta una o tutte; nessun dato personale oltre al
  numero della segnalazione. Sostituisce l'avviso provvisorio di US-302. Solo in app: l'email è US-1202 (review dello
  Sprint 4, §8.3).

#### US-1202
**Email transazionali** · Must · 5 punti · R3 · 📋 Backlog

Come **Tutor / Call Center** voglio ricevere per email le notifiche importanti per essere avvisato anche fuori dall'app.

Criteri di accettazione:
- Ogni notifica in app arriva anche via email alla stessa controparte.
- Invio tramite SMTP IONOS da un indirizzo del dominio; nessun dato personale sensibile nel testo dell'email.

### E13 — Trasversali e produzione

_Qualità, sicurezza, GDPR e passaggio dalla demo alla produzione._

#### EN-2
**Audit di ogni cambio di stato e scelta** · Must · 5 punti · R1 · 🎉 Done (Sprint 2)

Come **PO / ADMIN** voglio che ogni cambio di stato e ogni scelta (presa in carico, abbinamento, approvazioni, rifiuti e
motivi, configurazioni) sia registrato in modo immodificabile per poter ricostruire chi ha fatto cosa e quando.

Criteri di accettazione:
- Per ogni evento: autore, istante, entità, azione, valori prima/dopo e motivo; il registro accetta solo aggiunte.
- Le storie che cambiano stati o registrano scelte scrivono nell'audit (regola della Definition of Done).
- L'ADMIN consulta l'audit filtrando per entità, autore e periodo.

#### EN-4
**Contatti dei referenti aziendali nell'audit senza valori** · Must · 1 punti · R1 · 🎉 Done (Sprint 3)

Come **responsabile della privacy** voglio che nome, telefono ed email del referente di un'azienda non finiscano nell'audit
(che non si può cancellare) per rispettare la minimizzazione GDPR. Decisione del PO nella review dello Sprint 2 (04/10).

Criteri di accettazione:
- Creando o modificando un'azienda, l'audit registra per referente, telefono ed email solo che il campo è stato
  impostato o modificato, mai il valore; gli altri campi restano con prima/dopo.

#### EN-5
**Controllo della numerazione delle migrazioni nella CI** · Must · 1 punti · R1 · 🎉 Done (Sprint 3)

Come **PO** voglio che una PR con una migrazione numerata male venga segnalata dalla CI per non scoprirlo durante un
deploy (caso V11/V12 dello Sprint 2). Decisione del PO nella review dello Sprint 2 (24/10).

Criteri di accettazione:
- Il controllo fallisce se la PR aggiunge una migrazione `V<n>__…` con `n` non superiore alla più alta già su `main`, o
  con un numero duplicato, e il messaggio indica il numero da usare.
- Le PR senza migrazioni non sono toccate dal controllo.

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

Come **responsabile della privacy** voglio che i dati identificativi dei beneficiari siano anonimizzati alla scadenza della conservazione per rispettare il GDPR.

Criteri di accettazione:
- La durata di conservazione è un parametro (per ora 2 anni dalla conclusione del percorso).
- Restano solo i dati statistici; l'operazione è registrata.

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
