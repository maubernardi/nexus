package it.nexus.domain.enumeration;

/** Stato del post di bacheca. */
public enum BoardPostStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED;

    /** Stati di un annuncio attivo: al più uno per mansione ({@code uq_board_post_active_job_slot}). */
    public static final java.util.Set<BoardPostStatus> ACTIVE = java.util.EnumSet.of(DRAFT, PUBLISHED);
}
