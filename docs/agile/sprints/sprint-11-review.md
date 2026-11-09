# Sprint 11 — Review, retrospettiva e planning

**Sprint:** 9 – 11 novembre 2026 (iniziato l'08/11 sera dopo il planning, chiuso il 09/11, in un giorno) · **Review:**
09/11/2026 · **Partecipanti:** Mauro Bernardi (Product Owner), team di sviluppo (Claude Code) · **Demo:**
https://portalenexus.it, versione `34d1301` · **Collaudo:** [docs/test/collaudo.md](../../test/collaudo.md), T-21.7 e
T-21.8 superate; T-21.6 (prime PR di Dependabot) in attesa del lunedì.

> Documento unico e asincrono: review (§1–§6), retrospettiva (§7), decisioni da prendere (§8) e proposta di planning
> dello Sprint 12 (§9). La PR resta in **bozza** finché non abbiamo finito di discuterne (A12): le decisioni le aggiungo
> qui, poi la segno come pronta per il merge.

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | NEXUS è aggiornato e senza vulnerabilità note, resta aggiornato con le PR di Dependabot, le password comuni sono rifiutate e il Tutor allega il curriculum del beneficiario. |
| **Esito** | ✅ **Raggiunto.** Tutto pubblicato e collaudato; Dependabot: 0 avvisi aperti. |
| **Impegno / completato** | 15 punti / **15 punti** (3 storie su 3) |
| **Extra** | Nessuno pronto |
| **Velocity** | Sprint 1: 21 · 2: 25 · 3: 21 · 4: 24 · 5: 21 · 6: 20 · 7: 31 · 8: 23 · 9: 25 · 10: 22 · 11: **15** · media 22,5 |
| **PR unite** | 12 (#125–#136): 9 di EN-6, 1 di EN-7, 1 di US-205, 1 di chiusura |
| **Migrazioni del database** | 1: V32 (contenuto dei file) |
| **Deploy della demo** | 9, tutti riusciti, ognuno controllato; più 1 inutile (stessa versione rilanciata per errore, §7) |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| EN-6 | Dipendenze aggiornate e senza vulnerabilità note | 8 | [#125](https://github.com/maubernardi/nexus/pull/125)–[#133](https://github.com/maubernardi/nexus/pull/133) | Tutto all'ultima versione stabile (Java 25 e Node 26 LTS, Keycloak 26.8.2, TypeScript 7, i18next 27…), Dependabot configurato, 0 avvisi. |
| EN-7 | Password comuni rifiutate | 2 | [#134](https://github.com/maubernardi/nexus/pull/134) | Keycloak rifiuta 46 146 password comuni da 12 caratteri in su, con «Password troppo comune». |
| US-205 | Curriculum del beneficiario | 5 | [#135](https://github.com/maubernardi/nexus/pull/135) | Il Tutor allega il CV in PDF (fino a 5 MB); lo scaricano solo quelli che vedono il beneficiario; l'anonimizzazione lo cancella. |

## 3. Demo guidata

- **T-21.6** dipendenze aggiornate: Dependabot senza avvisi ✅; le prime PR automatiche arrivano il lunedì.
- **T-21.7** password comuni ✅.
- **T-21.8** curriculum del beneficiario ✅.

## 4. Qualità

### Test automatici

| | Fine Sprint 10 | Fine Sprint 11 |
|---|---|---|
| Backend – unit | 108 | 108 |
| Backend – integrazione (PostgreSQL e Keycloak veri) | 378 | 384 |
| Frontend – componenti (tutti con controllo axe) | 213 | 215 |

Da segnalare:
- **La CI ha fermato un guasto prima del deploy**: con Keycloak 26.8 il backend non poteva più usare l'API di
  amministrazione (scope `kc-admin-api`). L'hanno scoperto i test con la stessa versione della produzione; ora un
  controllo impedisce che test e produzione divergano.
- **Prova con Keycloak vero** anche delle password comuni: `password1234` rifiutata, una password valida accettata.

### Sicurezza

Revisione ASVS livello 1 ([`docs/security/asvs-l1.md`](../../security/asvs-l1.md)): **nessuna lacuna aperta** (L1
chiusa con EN-7, L2 l'08/11); capitolo V12 (caricamento di file) rivisto con US-205. Dependabot: 0 avvisi.

### Difetti

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| Keycloak 26.8: API di amministrazione in 401 per il client tecnico | EN-6 | CI | Prima del merge ([#127](https://github.com/maubernardi/nexus/pull/127)) e script sul server |
| TypeScript 7 senza API: il lint non partiva | EN-6 | Verifica locale | Prima della PR ([#131](https://github.com/maubernardi/nexus/pull/131)): due versioni affiancate |
| Override di `minimatch` ovunque: un plugin di ESLint si rompeva | EN-6 | Verifica locale | Prima della PR ([#128](https://github.com/maubernardi/nexus/pull/128)): override solo dove serve |
| Sostituire o togliere il CV non cancellava bene il file | US-205 | CI | Prima del merge ([#135](https://github.com/maubernardi/nexus/pull/135)) |

## 5. Decisioni prese durante lo sprint

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | Aggiornamenti di EN-6 una PR alla volta, ognuna con il suo deploy e controllo | PO e team | EN-6 |
| D2 | `deepmerge` (nessuna correzione esistente, solo build) archiviato | PO (08/11) | EN-6 |
| D3 | TypeScript 7 affiancato all'API di TypeScript 6 finché typescript-eslint non lo supporta | Team | EN-6 |
| D4 | Elenco delle password: le 46 146 da 12 caratteri in su tra il milione più usate, invece delle 100 000 | Team, confermato dal PO (§8.2) | EN-7 |
| D5 | Contenuto dei file nel database, non su disco | Team, confermato dal PO (§8.3) | US-205 |
| D6 | Windows Terminal: `experimental.detectURLs: false` (un solo link per Ctrl+clic) | PO | Ambiente del PO |

## 6. Stato del prodotto rispetto alla roadmap

**R1** — sviluppo completo; resta **US-1301** (PWA su smartphone, a cura del PO: prova T-10.1).

**R2, R3** — ✅ complete.

**R4** — *produzione*: **68 punti su 86** fatti. Restano:

| Storia | Punti | Note |
|---|---|---|
| US-1302 Backup del database | 5 | Nel backlog per decisione del PO (review dello Sprint 9); serve per la produzione |
| US-1305 Revisione UX/UI | 8 (da rivedere) | Sprint dedicato; tra gli spunti la tabella Utenti stretta a 1280 px |
| US-1006 Integrazione dei modelli Word reali | da stimare | Sprint dedicato, quando arrivano i modelli della cooperativa |
| US-1306 Passaggio in produzione | 5 | Dipende dal backup |

**Product backlog:** 76 elementi attivi, 278 punti; completati 71 (259 punti) negli Sprint 1–11.

## 7. Retrospettiva

**Cosa ha funzionato**
- **Sprint chiuso in un giorno**, con 12 PR piccole, ognuna verificata (CI, deploy, controllo della demo).
- **Aggiornare una cosa alla volta** ha reso visibili i problemi (Keycloak 26.8, TypeScript 7) dove nascevano.
- **Chrome collegato** (A20): menu, testi e pagine controllati dopo ogni deploy che toccava l'interfaccia.
- **Le regole della review dello Sprint 10 hanno funzionato**: «versioni sempre aggiornate» applicata dall'inizio alla
  fine, Dependabot pronto per le prossime.

**Cosa non ha funzionato**
- **PR date per unite ma ancora aperte** (#128, #130, come #120 e #127 nello Sprint 10): ogni volta il controllo del
  merge l'ha fermato, ma è un giro in più per entrambi.
- **Ho rilanciato un deploy della versione già in esecuzione**: il mio controllo non si fermava davanti a una PR aperta.
  Corretto subito: deploy e cancellazioni solo dopo `state = MERGED` verificato.
- **Le sessioni della demo scadono dopo 30 minuti**: per i controlli con Chrome ti ho dovuto chiedere più volte di
  rientrare.
- **Sprint sotto la media (15 contro 22,5)**: non per lentezza, ma perché non c'erano altre storie pronte (backup
  rinviato, UX e modelli in sprint dedicati).

**Azioni**

| # | Azione | Chi |
|---|---|---|
| A23 | Merge su GitHub: controllare che la PR diventi viola con «Merged» (dopo «Confirm merge») | PO |
| A24 | Deploy e cancellazione dei branch solo dopo aver verificato `state = MERGED` (già nel processo) | Team |
| A25 | Tenere nel backlog almeno uno sprint di storie pronte, per non chiudere sprint sotto la media | Team e PO |
| A20 | (confermata) Vedere le modifiche visibili prima di consegnarle | Team |

## 8. Da decidere

### 8.1 Chiusura dello Sprint 11
*Proposta:* Sprint 11 chiuso con velocity **15**.

> ✅ **Deciso (PO, 09/11): proposta accettata.**

### 8.2 Elenco delle password comuni (EN-7)
Invece delle 100 000 password più usate (solo 488 da 12 caratteri in su, le altre già bloccate dalla lunghezza) ho preso
le 46 146 da 12 caratteri in su tra il milione più usate. *Proposta:* confermare.

> ✅ **Deciso (PO, 09/11): confermato.**

### 8.3 Dove stanno i file (US-205)
Il contenuto dei CV è nel database, accanto ai metadati: il backup lo copre e il server non ha nulla da configurare.
*Proposta:* confermare; da rivedere solo se i file diventassero molti o grandi.

> ✅ **Deciso (PO, 09/11): i CV restano nel database.** Prima il PO aveva chiesto se salvarli sul disco, perché non ha chiari spazi e diritti su IONOS.
> Precisazione del team: il database è **già sul disco del server** (volume di PostgreSQL), quindi CV nel database e CV
> in una cartella usano lo **stesso disco** e lo stesso spazio; IONOS non c'entra, finché non si sceglie un backup
> esterno (§8.5). La differenza è solo che nel database il backup e la cancellazione vanno insieme ai dati.

### 8.4 Cosa fare nello Sprint 12
Restano quattro blocchi per chiudere R4. *Proposta del team:* **Sprint 12 = backup e preparazione della produzione**
(US-1302 + la parte di US-1306 che non dipende dai dati reali), perché sblocca il go-live; poi gli sprint dedicati a
UX/UI e ai modelli Word. *Alternative:* sprint UX/UI subito; oppure modelli Word, se i modelli della cooperativa sono
pronti.

> ⏸️ **Deciso (PO, 09/11): per ora ci fermiamo.** Il contenuto dello Sprint 12 lo valuta il PO più avanti.
>
> ✅ **Deciso (PO, 09/11, sera):** arrivati i modelli Word reali, lo Sprint 12 prepara i documenti reali (cataloghi,
> comuni, beneficiario, sede di competenza); backup e produzione vanno in fondo. Vedi [sprint-12](sprint-12.md).

### 8.5 Backup (US-1302), se si fa nello Sprint 12
Serve una destinazione fuori dal server. *Proposta* (dalla review dello Sprint 9): spazio di archiviazione **IONOS**
(S3), 30 backup giornalieri cifrati; le credenziali le metti tu nel `.env` del server. *Alternative:* un altro server o
un NAS via SSH; un servizio esterno (es. Backblaze B2).

> ❓ **Domanda del PO (09/11): ha un costo aggiuntivo?** Sì, ma piccolo: lo spazio di archiviazione S3 di IONOS si
> paga a consumo, per GB e per ora di conservazione, e i caricamenti sono gratuiti (si paga il traffico in uscita, cioè
> solo i ripristini). Con il database di oggi (decine di MB) anche 30 copie restano sotto qualche GB: **pochi centesimi
> al mese**. Il prezzo esatto va letto sul listino IONOS del contratto. Resta da capire se il contratto IONOS attuale
> comprende lo spazio di archiviazione S3: da verificare quando si riprende US-1302.

### 8.6 Data del passaggio in produzione
*Domanda:* c'è una data indicativa con la cooperativa per usare NEXUS con dati reali? Serve per ordinare gli sprint.

> ✅ **Risposta (PO, 09/11): nessuna data**; servono ancora giri di discussione con la cooperativa.

## 9. Proposta per lo Sprint 12 (sospesa, §8.4)

**Obiettivo proposto:** *i dati di NEXUS hanno un backup giornaliero fuori dal server, provato con un ripristino, e la
demo è pronta a diventare produzione.*

| Ordine | ID | Storia | Punti |
|---|---|---|---|
| 1 | US-1302 | Backup del database: giornaliero, cifrato, fuori dal server, con prova di ripristino | 5 |
| 2 | US-1306 (parte 1) | Passaggio in produzione, preparazione: checklist, configurazione senza dati demo, procedura di avvio | 3 |
| 3 | — | Tabella Utenti a 1280 px (piccola correzione emersa con Chrome) | 1 |
| | | **Totale** | **9** |

Volutamente piccolo: il resto di US-1306 (dati reali, utenti veri) dipende dalla data e dalla cooperativa. Se §8.4
sceglie un'alternativa, la proposta cambia di conseguenza.

## 10. Azioni a carico del PO

- [x] Decidere i punti §8.1, §8.2, §8.4, §8.6 (09/11).
- [x] §8.3: i CV restano nel database (09/11).
- [ ] §8.5 (backup) quando si riprende US-1302; planning dello Sprint 12 più avanti.
- [ ] T-21.6: lunedì prossimo, le prime PR di Dependabot.
- [ ] US-1301: PWA su Android e iOS (collaudo T-10.1).
- [x] Collaudo dello Sprint 11 (T-21.7, T-21.8).
