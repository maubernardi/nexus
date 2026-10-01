package it.nexus.domain;

import it.nexus.domain.enumeration.Gender;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.enumeration.TicketType;

/** Costruttori di entità valide per i test (stesso package: accede ai costruttori protected). */
public final class TestEntities {

    private TestEntities() {
    }

    public static Project project(String code) {
        Project project = new Project();
        project.setCode(code);
        project.setName("Progetto " + code);
        return project;
    }

    public static Zone zone(String code) {
        Zone zone = new Zone();
        zone.setCode(code);
        zone.setName("Zona " + code);
        return zone;
    }

    public static JobCategory jobCategory(String code) {
        JobCategory category = new JobCategory();
        category.setCode(code);
        category.setName("Categoria " + code);
        return category;
    }

    public static AppUser user(String username, Role role) {
        return user(username, role, "ext-" + username);
    }

    public static AppUser user(String username, Role role, String externalId) {
        AppUser user = new AppUser();
        user.setExternalId(externalId);
        user.setUsername(username);
        user.setFirstName("Nome");
        user.setLastName("Cognome");
        user.setEmail(username + "@nexus.test");
        user.setRole(role);
        return user;
    }

    public static Company company(String vatCode) {
        Company company = new Company();
        company.setName("Azienda " + vatCode);
        company.setVatCode(vatCode);
        return company;
    }

    public static JobSlot jobSlot(Company company, JobCategory category, Zone zone) {
        JobSlot slot = new JobSlot();
        slot.setCompany(company);
        slot.setJobCategory(category);
        slot.setZone(zone);
        slot.setTitle("Mansione di prova");
        return slot;
    }

    public static Beneficiary beneficiary(AppUser tutor, Zone zone) {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setOwnerTutor(tutor);
        beneficiary.setFirstName("Mario");
        beneficiary.setLastName("Rossi");
        beneficiary.setBirthYear(1995);
        beneficiary.setGender(Gender.M);
        beneficiary.setResidenceZone(zone);
        return beneficiary;
    }

    public static Ticket ticket(AppUser tutor, Project project, Beneficiary beneficiary, JobCategory requested) {
        Ticket ticket = new Ticket();
        ticket.setTutor(tutor);
        ticket.setProject(project);
        ticket.setBeneficiary(beneficiary);
        ticket.setType(TicketType.NORMAL);
        ticket.setStatus(TicketStatus.NUOVA);
        ticket.setRequestedJobCategory(requested);
        return ticket;
    }

    public static BoardPost boardPost(JobSlot slot) {
        BoardPost post = new BoardPost();
        post.setJobSlot(slot);
        post.setTitle("Annuncio di prova");
        return post;
    }
}
