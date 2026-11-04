## Decisions

### D1 — Due tabelle come gli altri cataloghi
Stessa struttura di `internship_type` (V21) e stesse regole di `CatalogServiceImpl`: due nuovi valori di `CatalogKind`,
senza logica nuova. I codici delle voci iniziali sono espliciti; quelli delle voci aggiunte dall'ADMIN si ricavano dal
nome (`ReferenceCodes`), come negli altri cataloghi.

### D2 — I testi dei motivi come frasi intere
Q6 elenca i motivi in forma breve ("giudica il tirocinante non idoneo"). Nel catalogo, e quindi nel report, sono frasi
complete con il soggetto ("L'azienda giudica il tirocinante non idoneo"), così si leggono da sole. L'ADMIN può
cambiarle dalla pagina "Cataloghi".
