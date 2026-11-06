## 1. Backend

- [x] 1.1 `IdentityProviderUsers`: creazione con ruolo, cancellazione, email per la password; test con Keycloak vero
- [x] 1.2 `/api/v1/users`: elenco, creazione con annullamento in Keycloak se NEXUS non salva, nuovo invito; audit; test di integrazione

## 2. Frontend

- [x] 2.1 Pagina «Utenti» con nuovo invito, pagina «Nuovo utente» con username proposto, voce di menu; test con axe

## 3. Keycloak e deploy

- [x] 3.1 SMTP nel realm demo (password da `NEXUS_SMTP_PASSWORD`), compose; script una tantum `keycloak-email.sh`
- [x] 3.2 README e collaudo

## 4. Consegna

- [ ] 4.1 PR con CI verde
- [ ] 4.2 Sul server: `keycloak-email.sh` dopo il deploy, poi prova reale con un nuovo utente
