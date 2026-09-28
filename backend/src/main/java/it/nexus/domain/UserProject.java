package it.nexus.domain;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Assegnazione di un utente a un progetto (determina la visibilità in lettura dei ticket). */
@Getter
@Entity
@Table(name = "user_project")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProject extends AbstractCreationAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private Id id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @MapsId("projectId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    public UserProject(AppUser user, Project project) {
        this.id = new Id(user.getId(), project.getId());
        this.user = user;
        this.project = project;
    }

    @Getter
    @Embeddable
    @EqualsAndHashCode
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Id implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Column(name = "user_id")
        private Long userId;

        @Column(name = "project_id")
        private Long projectId;

        public Id(Long userId, Long projectId) {
            this.userId = userId;
            this.projectId = projectId;
        }
    }
}
