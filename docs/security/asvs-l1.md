# Revisione di sicurezza — OWASP ASVS 4.0.3, livello 1

**Storia:** US-1307 (Sprint 10) · **Data:** 07/11/2026 · **Versione esaminata:** demo `0c42db6` su
https://portalenexus.it · **Chi:** team di sviluppo (Claude Code), da rivedere con il PO prima del passaggio in
produzione (US-1306).

Metodo: lettura del codice e della configurazione (backend, PWA, Keycloak, Caddy, deploy), test automatici esistenti,
richieste dall'esterno alla demo (intestazioni HTTP, percorsi pubblici, certificati). Sono riportati i requisiti di
livello 1 che riguardano NEXUS; quelli non applicabili sono indicati come tali.

Legenda: ✅ soddisfatto · ⚠️ lacuna, con azione proposta · — non applicabile.

## Esito in breve

| | |
|---|---|
| **Soddisfatti** | autenticazione delegata a Keycloak con protezione dai tentativi ripetuti, password lunghe, recupero sicuro e avviso a ogni cambio; sessioni brevi; ogni endpoint autorizzato per ruolo e per oggetto; intestazioni di sicurezza complete (CSP rigida, HSTS, `nosniff`, `frame-ancestors 'none'`); API mai in cache; errori senza dettagli interni; log e audit senza dati personali; conservazione limitata (registro accessi 12 mesi, anonimizzazione dei beneficiari) |
| **Lacune** | Nessuna aperta: **L1** password comuni (2.1.7) chiusa con EN-7; **L2** avvisi sulle dipendenze (14.2.1) chiusa l'08/11 |
| **Giudizio** | nessuna lacuna bloccante per la demo; L1 e L2 vanno chiuse prima della produzione (US-1306) |

## V1 — Architettura

| Req. | Esito | Evidenza |
|---|---|---|
| 1.4.1 Controllo degli accessi lato server | ✅ | Ogni endpoint ha `@PreAuthorize` con i ruoli del token (verificato su tutti i `@…Mapping` di `web/rest`); la PWA nasconde soltanto, non decide |
| 1.14.6 Nessuna tecnologia client insicura | ✅ | PWA React senza plugin; `object-src 'none'` |

## V2 — Autenticazione (Keycloak)

| Req. | Esito | Evidenza |
|---|---|---|
| 2.1.1 Password di almeno 12 caratteri | ✅ | `passwordPolicy: length(12) and notUsername and notEmail` |
| 2.1.2 Password lunghe ammesse (≥ 64) | ✅ | Nessun limite massimo nella policy |
| 2.1.7 Password comuni o compromesse rifiutate | ✅ (L1 chiusa con EN-7) | Policy `passwordBlacklist` con le 46 146 password da 12 caratteri in su tra il milione più usate (SecLists); messaggio «Password troppo comune» |
| 2.1.9 Nessuna regola di composizione | ✅ | Solo la lunghezza, come chiede ASVS |
| 2.1.12 Mostrare la password digitata | ✅ | Pulsante «mostra» nel tema |
| 2.2.1 Protezione dai tentativi ripetuti | ✅ | `bruteForceProtected`: dopo 5 errori attesa crescente fino a 15 minuti; i tentativi si vedono in «Accessi non riusciti» (US-113) |
| 2.2.3 Avviso dopo il cambio delle credenziali | ✅ | Email «PORTALE NEXUS - Le tue credenziali sono state cambiate» con data, ora e IP (US-1308) |
| 2.5.1 Recupero senza rivelare la password | ✅ | Collegamento monouso per email, valido 30 minuti (US-1308) |
| 2.5.2 Niente domande segrete | ✅ | Non esistono |
| 2.5.4 Nessun account condiviso o predefinito | ✅ | Gli utenti li crea l'ADMIN (US-103); gli utenti demo esistono solo sulla demo e si eliminano con US-1306 |
| 2.7/2.8 Codici monouso | ✅ | Codice del cambio email: 6 cifre, 30 minuti, 5 tentativi (US-110) |

## V3 — Sessioni

| Req. | Esito | Evidenza |
|---|---|---|
| 3.2.1 Token nuovo a ogni accesso | ✅ | OIDC con PKCE (Keycloak) |
| 3.2.3 Token non in archivi del browser | ✅ | Token solo in memoria (keycloak-js); in `localStorage` c'è solo l'utente mock dello sviluppo locale |
| 3.3.1 Uscita che invalida la sessione | ✅ | «Esci» chiama il logout di Keycloak |
| 3.3.2 Scadenza per inattività | ✅ | Token 5 minuti, sessione 30 minuti di inattività, 10 ore al massimo |
| 3.4.x Cookie `Secure`/`HttpOnly`/`SameSite` | ✅ | Cookie di sessione solo di Keycloak, con questi attributi; l'API è senza stato (`STATELESS`) |
| 3.5.x Revoca | ✅ | Disattivazione e cambio di ruolo chiudono le sessioni (US-105) |

## V4 — Controllo degli accessi

| Req. | Esito | Evidenza |
|---|---|---|
| 4.1.1 Regole applicate sul server | ✅ | `@PreAuthorize` + controlli nei servizi |
| 4.1.3 Minimo privilegio | ✅ | Tutor: solo i propri beneficiari e i progetti assegnati (US-104, US-204); Coordinatore solo nei progetti che coordina (US-112); client tecnico Keycloak con `view-users`, `manage-users`, `view-events` |
| 4.2.1 Accesso diretto agli oggetti (IDOR) | ✅ | Visibilità per proprietario/progetto nei servizi; un oggetto non visibile risponde `404`; test di integrazione `403`/`404` per ogni storia |
| 4.3.1 Interfacce di amministrazione protette | ✅ | Console di Keycloak raggiungibile solo da localhost (dall'esterno `403`, verificato a ogni deploy) |

## V5 — Validazione, sanificazione, codifica

| Req. | Esito | Evidenza |
|---|---|---|
| 5.1.3 Validazione degli input | ✅ | Vincoli Jakarta su ogni DTO con `@Valid`; vincoli `CHECK`/`UNIQUE` anche nel database, testati per nome |
| 5.2.x / 5.3.3 Codifica dell'output (XSS) | ✅ | React codifica di default; nessun `dangerouslySetInnerHTML`; CSP `script-src 'self'` senza `unsafe-inline` |
| 5.3.4 SQL injection | ✅ | JPA con parametri; le query native hanno solo parametri con nome |
| 5.3.x Documenti Word | ✅ | Apache POI codifica i valori nei segnaposto (`DocxTemplate`) |

## V7 — Errori e log

| Req. | Esito | Evidenza |
|---|---|---|
| 7.1.1 Niente credenziali né dati personali nei log | ✅ | `toString()` senza dati personali (regola del progetto); nei log del deploy pubblici solo backend e postgres |
| 7.1.2 Eventi di sicurezza registrati | ✅ | Accessi riusciti e falliti (US-113), audit immodificabile delle operazioni (EN-2) |
| 7.4.1 Errori generici senza dettagli interni | ✅ | Corpo d'errore uniforme; test `unexpectedError_returns500WithoutInternals` |

## V8 — Protezione dei dati

| Req. | Esito | Evidenza |
|---|---|---|
| 8.2.1 Niente dati sensibili in cache | ✅ | API con `Cache-Control: no-store`; il service worker della PWA non mette mai in cache `/api` |
| 8.3.1 Dati sensibili non nell'URL | ✅ | Nell'indirizzo solo identificativi e filtri |
| 8.3.4 Conservazione limitata | ✅ | Beneficiari anonimizzati dopo 2 anni senza attività con conferma (US-1303); registro degli accessi 12 mesi |

## V9 — Comunicazioni

| Req. | Esito | Evidenza |
|---|---|---|
| 9.1.1 Solo TLS | ✅ | Caddy con certificati Let's Encrypt rinnovati da soli; HTTP reindirizzato |
| 9.1.2 HSTS | ✅ | `max-age=31536000; includeSubDomains` su sito e Keycloak |
| 9.1.3 TLS recente | ✅ | Caddy accetta solo TLS 1.2 e 1.3 |

## V10 / V14 — Codice, dipendenze e configurazione

| Req. | Esito | Evidenza |
|---|---|---|
| 10.3.2 Dipendenze da fonti affidabili, versioni fissate | ✅ | Versioni esatte (pnpm `saveExact`, Maven), action di GitHub fissate per SHA |
| 14.2.1 Componenti senza vulnerabilità note | ⚠️ **L2** | Nessun avviso automatico: le versioni sono congelate (decisione del 27/09) e nessuno segnala se una diventa vulnerabile |
| 14.3.2 Niente modalità di debug in produzione | ✅ | Actuator e Swagger non raggiungibili dall'esterno (`404`) |
| 14.4.x Intestazioni di sicurezza | ✅ | CSP rigida, `X-Content-Type-Options: nosniff`, `frame-ancestors 'none'`/`X-Frame-Options: DENY`, `Referrer-Policy`, `Permissions-Policy` |
| 14.5.3 CORS ristretto | ✅ | Nessuna origine esterna ammessa (stessa origine) |
| Segreti | ✅ | Solo nel `.env` del server; mai nel repository né nei log delle Actions |

## V12 / V13 — File e API

| Req. | Esito | Evidenza |
|---|---|---|
| 12.1.1 Limite di dimensione | ✅ (US-205) | CV fino a 5 MB: controllo nel servizio, vincolo `ck_stored_file_content_size` nel database, limite del server a 6 MB |
| 12.2.1 Tipo verificato dal contenuto | ✅ (US-205) | Si accetta solo un file che inizia con `%PDF-`, non conta il nome né il tipo dichiarato dal browser |
| 12.3.1 Nome del file non usato come percorso | ✅ (US-205) | Il contenuto è nel database (`stored_file_content`), mai su disco; il nome caricato è solo un'etichetta, ripulita da percorsi e caratteri di controllo |
| 12.4.1 File fuori dalla radice web | ✅ (US-205) | Il CV si scarica solo dall'API, con i permessi del beneficiario (proprietario, Tutor dei progetti, Call Center, ADMIN) |
| 12.5.1 / 12.5.2 Download sicuro | ✅ (US-205) | Sempre come allegato (`Content-Disposition: attachment`), `application/pdf`, `nosniff`, `Cache-Control: no-store`; il service worker non lo mette in cache (API) |
| 12.4.2 Antivirus | — | Non richiesto al livello 1; i PDF li caricano solo i Tutor autenticati |
| 13.1.x API autenticate | ✅ | Bearer token su ogni chiamata; CSRF non necessario con token nell'intestazione |

## Azioni proposte

| # | Lacuna | Proposta | Chi | Quando |
|---|---|---|---|---|
| L1 | Password comuni accettate (2.1.7) | Policy `passwordBlacklist` di Keycloak con un elenco pubblico di password comuni (es. le 100 000 più usate), nel tema/immagine e attivata da uno script | Team | ✅ fatto con EN-7 (Sprint 11) |
| L2 | Nessun avviso per dipendenze vulnerabili (14.2.1) | Attivare gli **avvisi di sicurezza di Dependabot** del repository (solo avvisi, senza PR automatiche: le versioni restano congelate e si aggiorna solo su decisione) | PO (impostazione di GitHub) | ✅ fatto l'08/11 |

Da rivedere quando cambiano: caricamento dei file (US-205), passaggio in produzione (US-1306: utenti demo eliminati,
backup, questa revisione aggiornata).

## Monitoraggio (US-1307)

Il workflow **«Controllo della demo»** (`.github/workflows/uptime.yml`) gira ogni 10 minuti dalle 8 alle 20 (ora
italiana) e controlla da fuori la PWA, Keycloak e il backend (`/api/v1/me` deve rispondere `401`), con 3 tentativi a 30
secondi; controlla anche che i certificati non scadano entro 14 giorni. Se fallisce, GitHub manda un'email al PO.
Limiti: GitHub può ritardare i run pianificati di qualche minuto e li sospende dopo 60 giorni senza attività nel
repository.
