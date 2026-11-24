package it.nexus.services.identity;

/** Dati dell'account corretti dall'ADMIN (US-116). {@code toString()} senza dati personali. */
public record IdentityUserDetails(String username, String firstName, String lastName, String email) {

    @Override
    public String toString() {
        return "IdentityUserDetails[]";
    }
}
