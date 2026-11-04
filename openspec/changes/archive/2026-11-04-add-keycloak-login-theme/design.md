## Decisions

### D1 — Un tema FreeMarker sopra `keycloak.v2`, non Keycloakify
Keycloakify permetterebbe di scrivere la pagina in React, ma aggiunge una catena di build e dipendenze nuove: le
versioni sono congelate. Il tema `nexus` estende invece il tema predefinito della stessa versione di Keycloak e
cambia due cose:
- `template.ftl`, copiato da `keycloak.v2` 26.7.4 e cambiato solo nelle parti segnate «NEXUS»: favicon,
  intestazione a tutta larghezza, piè di pagina;
- `css/nexus.css` sopra il CSS di PatternFly.

Le altre pagine di Keycloak (errore, uscita, cambio password, …) ereditano tutto dallo stesso template. Quando
Keycloak cambierà versione si riparte dal nuovo `template.ftl` e si riapplicano le parti segnate.

### D2 — I token del sito, copiati
I colori sono quelli di `frontend/src/index.css`, già verificati per WCAG 2.2 AA in chiaro e in scuro. Sono copiati,
non importati, perché il tema non passa dalla build del frontend: un commento in testa al CSS ricorda di tenerli
allineati. Il tema scuro segue la classe `pf-v5-theme-dark`, che `keycloak.v2` mette quando il sistema preferisce lo
scuro, come fa il sito.

Il CSS sostituisce i bordi di PatternFly, solo inferiori e chiari, con un bordo intero a contrasto 3:1, e porta il
contorno del focus sul riquadro del campo: il controllo interno è tagliato da `overflow: hidden`.

### D3 — Solo italiano, ma con la localizzazione attiva
`internationalizationEnabled` resta `true` con `supportedLocales: ["it"]`. Così:
- il selettore non compare (Keycloak lo mostra solo con più lingue);
- un browser in inglese riceve comunque l'italiano;
- i testi passano dai file dei messaggi, sopra quelli italiani di Keycloak.

`locales=it` nel tema limita anche le lingue proposte nella console.

### D4 — Un'immagine `nexus-keycloak`, non file scaricati dal server
Lo script del server (`nexus-deploy`) scarica un elenco fisso di file ed è installato a mano: aggiungere i file del
tema vorrebbe dire cambiarlo sul server. Un'immagine costruita dal workflow di deploy, versionata con lo SHA come
backend e web, porta il tema dentro: per i deploy successivi non serve nulla sul server.

### D5 — Il realm della demo si aggiorna una volta
Keycloak importa il realm all'avvio solo se non esiste (`IGNORE_EXISTING`). Sulla demo il realm c'è già, quindi tema
e lingue vanno impostati una volta con `kcadm` dentro il container, come scritto nel README. Lo stesso si può fare
dalla console via tunnel SSH. Il comando non tocca utenti né client.

### D6 — Accessibilità verificata sulla pagina vera
axe-core (la stessa versione dei test del frontend), sulla pagina servita da Keycloak:
- chiaro, con l'errore di accesso: nessuna violazione WCAG 2.2 AA;
- scuro: nessuna violazione.

L'unico controllo da verificare a mano è `aria-invalid=""` sulla password, che viene dal markup di Keycloak: per
ARIA un valore vuoto vale "false". In più:
- a 390 px non c'è scorrimento orizzontale e resta il margine di 16 px;
- campi e pulsanti sono alti 44 px;
- il testo segue la dimensione scelta dall'utente.
