## Context

Vedi proposal.md, «Why». Oggi l'azienda ha in `company` un referente (`contact_person`), un telefono e un'email.
Telefono ed email finiscono nei documenti (`TELEFONO_AZIENDA`, `EMAIL_AZIENDA`); nell'audit tutti e tre sono solo
«cambiati» (EN-4). I cataloghi dell'ADMIN passano da `CatalogServiceImpl`. Le righe figlie della scheda del tirocinio
(tutor aziendali, progetti formativi) si sostituiscono in blocco quando cambiano. Nel frontend le proroghe sono un
elenco ripetuto con aggiunta e rimozione (`ExtensionsFields`).

Nel database attuale i referenti sono due colonne fisse dell'azienda («Nominativo_1» e «Nominativo_2», ognuna con il
suo ruolo). Telefono, cellulare, fax e le due email sono dell'azienda, non dei referenti. La sede operativa è un comune
(«Sede Locale») e un indirizzo («Indirizzo_2»).

## Goals / Non-Goals

**Goals:** più referenti con il ruolo e i recapiti propri, recapiti e sede operativa dell'azienda, pronti per
l'importazione di US-1309.

**Non-Goals:** importazione, legale rappresentante, segnaposto dei documenti, ricerca per referente (vedi
proposal.md).

## Decisions

### D1. Catalogo dei ruoli

Tabella `contact_role`, con le colonne e i vincoli di `business_category` (V46):
- `uq_contact_role_code`;
- `ck_contact_role_code`;
- `ck_contact_role_name`;
- l'indice `uq_contact_role_active_name` sui nomi attivi.

`CatalogKind.CONTACT_ROLES` (`/catalogs/contact-roles`, prefisso dei codici `RUOLO`) e `AuditEntityType.CONTACT_ROLE`.

Voci iniziali con **id = 893700000000000500 + numero in Access** (`ContactRole.LEGACY_ID_BASE`):

| Access | Nome in NEXUS | Uso nel database attuale |
|---|---|---|
| 1 | Referente | 2.240 |
| 2 | Addetto di segreteria | 171 |
| 3 | Responsabile del personale | 1.899 |
| 13 | Titolare | 1.558 |
| 16 | Presidente | 34 |
| 19 | Amministrazione | 19 |
| 23 | Direttore | 1 |
| 24 | Socio | 2 |
| 25 | Capo area | 0 |
| 26 | Centralino | 0 |
| 27 | Legale rappresentante | 0 |
| 29 | Amministratore delegato | 0 |

Restano fuori:
- le righe vuote 15, 17 e 20;
- 18, 21 e 22, che non sono ruoli («pelle», «DI», «ele»);
- 28, doppione di 27.

Per US-1309:
- i 16 referenti con il ruolo 4 (inesistente), 15 o 17 entrano senza ruolo;
- 28 diventa 27, anche se oggi nessuno lo usa.

### D2. Referenti

- **Tabella `company_contact`**:
  - colonne:
    - `id`;
    - `company_id`;
    - `position` (0, 1, …);
    - `name` `varchar(200)`;
    - `role_id`;
    - `phone` e `mobile` `varchar(30)`;
    - `email` `varchar(254)`;
    - `created_at` e `created_by`;
  - vincoli:
    - `pk_company_contact`;
    - `fk_company_contact_company`;
    - `fk_company_contact_role`;
    - `uq_company_contact_position` su (`company_id`, `position`);
    - `ck_company_contact_position` (`position >= 0`);
    - `ck_company_contact_name`: se c'è, il nome non è vuoto;
    - `ck_company_contact_name_or_role`: almeno il nome o il ruolo;
  - l'indice `ix_company_contact_role`, per la chiave esterna verso il catalogo.
- **Nome o ruolo**: nel database attuale 1.129 referenti hanno il solo ruolo, per esempio «Responsabile del personale»
  senza nome. Il Call Center li usa per chiedere della persona giusta, quindi restano validi. Un referente con i soli
  recapiti invece non dice chi è.
- **Entità `CompanyContact`**:
  - estende `AbstractCreationAuditingEntity`, come i tutor aziendali;
  - è immutabile: quando i referenti cambiano, il servizio li sostituisce in blocco, con un flush tra la cancellazione
    e l'inserimento per `uq_company_contact_position`;
  - la raccolta dell'azienda è `@OneToMany(mappedBy)` con `@OrderBy("position")`;
  - la raccolta è dal lato inverso: cambiarla non incrementa da sola la versione dell'azienda. Quando i referenti
    cambiano, il servizio la incrementa con `PESSIMISTIC_FORCE_INCREMENT`, così due operatori che modificano i
    referenti ricevono il `409` come per gli altri campi. Lo verifica un test;
  - `toString()` senza valori.
- **Referente di oggi**: V47 lo copia come primo referente (`position` 0, senza ruolo), con l'id dell'azienda come id
  della riga, come V18 per le notifiche. La colonna `company.contact_person` resta e non si usa più: se si torna alla
  release precedente (A16), quella legge il referente di prima.
- **Al massimo 10 referenti**: il database attuale ne ha due; 10 bastano e tengono il modulo leggibile.

### D3. Recapiti e sede operativa

Colonne nuove di `company`, facoltative:
- `mobile` e `fax` `varchar(30)`, con il controllo di formato del telefono;
- `secondary_email` `varchar(254)`, con il controllo dell'email;
- `operating_address` `varchar(300)` e `operating_municipality_id`, con `fk_company_operating_municipality` e
  l'indice `ix_company_operating_municipality`.

Il comune della sede operativa si sceglie come quello della sede legale (`MunicipalityService.forAddress`). Resta il
comune già salvato anche se nel frattempo è cessato, come per la sede legale.

### D4. API e validazione

- **`CompanyDTO`**:
  - `contacts`: elenco di `CompanyContactDTO`, con `name`, `role` (`ReferenceItemDTO`), `phone`, `mobile` ed `email`;
  - `mobile`, `fax` e `secondaryEmail`;
  - `operatingAddress` e `operatingMunicipality`;
  - non c'è più `contactPerson`.
- **`CompanyFormDTO`**:
  - `contacts`: elenco di `CompanyContactFormDTO`, con `name`, `roleId`, `phone`, `mobile` ed `email`;
    - `@Size(max = 10)`;
    - ogni elemento ha `@Valid`, con gli stessi controlli di telefono ed email dell'azienda;
    - un elenco assente vale come vuoto;
  - `mobile`, `fax`, `secondaryEmail`, `operatingAddress` e `operatingMunicipalityId`;
  - non c'è più `contactPerson`.
- **Controlli nel servizio**, con l'errore sul campo del referente (`contacts[i].…`):
  - un referente senza nome e senza ruolo dà `contacts[i].name`, «Inserisci il nome o scegli il ruolo»;
  - il ruolo deve essere una voce attiva oppure un ruolo che un referente dell'azienda ha già, altrimenti
    `contacts[i].roleId`, «Ruolo non valido». I referenti non hanno un'identità tra un salvataggio e l'altro, quindi
    il ruolo disattivato resta valido per tutta l'azienda che lo usa.
- **`GET /reference/contact-roles`**: solo Call Center e ADMIN, come l'anagrafica.
- **Ricerca nell'elenco**: i referenti arrivano a lotti (`@BatchSize` sulla raccolta e sui ruoli), non con un
  `fetch join`. La ricerca non ha paginazione: un `fetch join` moltiplicherebbe le righe per il numero dei referenti.

### D5. Audit

- `contacts`: un'unica voce, `{"redacted": true}`, se cambia qualcosa nei referenti, compreso l'ordine. Si confronta
  l'elenco normalizzato prima e dopo.
- `mobile`, `fax` e `secondaryEmail`: solo «cambiati», come telefono ed email (EN-4).
- `operatingAddress` e `operatingMunicipality`: valori prima e dopo, come la sede legale.

### D6. Frontend

- **Modulo dell'azienda** diviso in gruppi (`fieldset` con `legend`):
  - **Dati dell'azienda**: ragione sociale, partita IVA, categoria, attività;
  - **Sede legale** e **Sede operativa**: indirizzo e comune, con il campo dei comuni di US-1007;
  - **Recapiti dell'azienda**: telefono, cellulare, fax, email, seconda email;
  - **Referenti**: un gruppo per referente (`role="group"` con il titolo «Referente 1»), con nome, ruolo (menu con
    «Nessuno», le voci attive e quelle già usate), telefono, cellulare ed email.
- **Etichette dei referenti** con il numero, come le proroghe («Referente 1: nome»): sono uniche anche nel riepilogo
  degli errori e per i comandi vocali.
- **Aggiunta e rimozione**:
  - «Aggiungi referente» aggiunge un gruppo vuoto e porta il focus al suo nome. Il pulsante si disattiva a 10
    referenti;
  - «Togli il referente N» lo toglie e porta il focus su «Aggiungi referente».
- **Errori**:
  - nel modulo il nome è richiesto se manca il ruolo;
  - gli errori del server su `contacts[i].…` vanno al campo con `toFormPath`;
  - il riepilogo elenca anche gli errori dei referenti.
- **Elenco**: la colonna «Referente» mostra il nome del primo referente, oppure il suo ruolo se manca il nome, con
  «e altri N» se ce ne sono altri.
- **Dettaglio dell'abbinamento**:
  - la sede operativa dopo la sede legale;
  - cellulare, fax e seconda email dopo telefono ed email;
  - i referenti in un elenco, ognuno con nome, ruolo e recapiti.

### D7. Dati dimostrativi

`R__seed_03` inserisce i referenti, con id fissi, nelle aziende dimostrative che non ne hanno. Una ne ha due e una ha
il solo ruolo. Le aziende dimostrative non scrivono più `contact_person`.

Sulla demo V47 ha già copiato i referenti di prima, quindi le aziende che li avevano non cambiano. Ricevono referenti
nuovi solo le due aziende da completare, che non ne avevano.

`demo-addresses.sql` aggiunge la sede operativa a un'azienda dimostrativa che non ce l'ha, dopo il caricamento dei
comuni.

## Risks / Trade-offs

- **[Ripristino della release precedente (A16)]**: tabelle e colonne nuove vengono ignorate, e la release precedente
  legge `contact_person`, che V47 non tocca. I referenti aggiunti nel frattempo non si vedono finché non si torna alla
  release nuova. Le modifiche al referente fatte durante il ripristino non tornano nei referenti.
- **[Colonna `contact_person` inutilizzata]**: resta finché serve poter tornare alla release precedente. Si toglie con
  una migrazione successiva, dopo il passaggio in produzione.
- **[Ruolo disattivato valido per tutta l'azienda]**: un operatore può assegnare a un altro referente della stessa
  azienda un ruolo disattivato che un collega ha già. È un caso raro e innocuo, e evita di dare un'identità ai
  referenti.
- **[Referenti sostituiti in blocco]**: a ogni salvataggio con modifiche ai referenti, `created_at` e `created_by` dei
  referenti diventano quelli dell'ultima modifica. Chi li ha cambiati resta nell'audit dell'azienda.

## Migration Plan

Deploy normale: V47 aggiunge tabelle, colonne vuote e i referenti copiati, con vincoli che i dati attuali rispettano.
In caso di ripristino la release precedente ignora quello che è nuovo (vedi Risks).
