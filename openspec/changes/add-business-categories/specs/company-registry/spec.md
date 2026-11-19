## ADDED Requirements

### Requirement: Categoria merceologica e attività dell'azienda
Il Call Center e l'ADMIN SHALL poter indicare nella scheda dell'azienda:
- la categoria merceologica, facoltativa, scelta tra le voci attive del catalogo;
- la descrizione dell'attività, facoltativa, al massimo 200 caratteri.

Un'azienda con una categoria poi disattivata SHALL mantenerla, anche quando si salva la scheda; una categoria
disattivata o inesistente MUST NOT essere scelta altrimenti (`400` sul campo). L'audit SHALL registrare categoria e
attività cambiate, con i valori prima e dopo. L'elenco delle aziende SHALL mostrare la categoria e SHALL potersi
filtrare per categoria, insieme alla ricerca e agli altri filtri, con il filtro nell'indirizzo della pagina. Il
dettaglio dell'abbinamento SHALL mostrare categoria e attività.

#### Scenario: Categoria e attività
- **WHEN** l'operatore sceglie «Ristorante / Trattoria / Bistrot», scrive «Pizzeria» come attività e salva
- **THEN** la scheda e l'elenco mostrano la categoria, e l'audit registra categoria e attività con i valori prima e
  dopo

#### Scenario: Filtro per categoria
- **WHEN** l'operatore sceglie nell'elenco la categoria «Bar / Pub»
- **THEN** l'elenco mostra solo le aziende di quella categoria e l'indirizzo della pagina contiene il filtro

#### Scenario: Categoria disattivata sull'azienda
- **WHEN** l'operatore salva la scheda di un'azienda la cui categoria è stata disattivata
- **THEN** la categoria resta e il salvataggio riesce

#### Scenario: Categoria non valida
- **WHEN** il modulo invia una categoria disattivata che l'azienda non ha, o una categoria inesistente
- **THEN** la risposta è `400` con l'errore sul campo `businessCategoryId`
