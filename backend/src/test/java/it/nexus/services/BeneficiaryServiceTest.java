package it.nexus.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.nexus.domain.AppUser;
import it.nexus.domain.Beneficiary;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Zone;
import it.nexus.domain.dto.BeneficiaryCreateDTO;
import it.nexus.domain.dto.BeneficiaryLanguageDTO;
import it.nexus.domain.enumeration.Gender;
import it.nexus.domain.enumeration.LanguageLevel;
import it.nexus.domain.enumeration.LicenseType;
import it.nexus.domain.enumeration.Role;
import it.nexus.mapper.BeneficiaryMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.ZoneRepository;
import it.nexus.services.impl.BeneficiaryServiceImpl;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;

@ExtendWith(MockitoExtension.class)
class BeneficiaryServiceTest {

    @Mock BeneficiaryRepository beneficiaries;
    @Mock ZoneRepository zones;
    @Mock TicketRepository tickets;
    @Mock CurrentAppUserService currentUser;
    @Mock BeneficiaryMapper mapper;
    @InjectMocks BeneficiaryServiceImpl service;

    private static final String ZONE_ID = TsidMapper.toExternal(123L);

    private static BeneficiaryCreateDTO dto(List<BeneficiaryLanguageDTO> languages) {
        return new BeneficiaryCreateDTO(" Mario ", "Rossi", 1998, Gender.M, "IT", "IT", ZONE_ID,
                Set.of(LicenseType.CQC, LicenseType.B), false, null, false, null, "", languages);
    }

    @Test
    void register_impostaProprietarioPatenteDerivataENomiPuliti() {
        AppUser tutor = TestEntities.user("tutor1", Role.TUTOR);
        when(zones.findById(123L)).thenReturn(Optional.of(TestEntities.zone("ZNORD")));
        when(currentUser.getCurrentAppUser()).thenReturn(tutor);
        when(beneficiaries.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.register(dto(List.of(new BeneficiaryLanguageDTO("it", LanguageLevel.MADRELINGUA))));

        ArgumentCaptor<Beneficiary> saved = ArgumentCaptor.forClass(Beneficiary.class);
        verify(beneficiaries).save(saved.capture());
        assertThat(saved.getValue().getOwnerTutor()).isSameAs(tutor);
        assertThat(saved.getValue().getFirstName()).isEqualTo("Mario");
        assertThat(saved.getValue().isHasDrivingLicense()).isTrue();
        assertThat(saved.getValue().getLicenseTypes()).containsExactly(LicenseType.B, LicenseType.CQC);
        assertThat(saved.getValue().getConstraints()).isNull();
    }

    @Test
    void register_rifiutaLinguaRipetuta() {
        when(zones.findById(123L)).thenReturn(Optional.of(TestEntities.zone("ZNORD")));

        assertThatThrownBy(() -> service.register(dto(List.of(
                new BeneficiaryLanguageDTO("it", LanguageLevel.C1), new BeneficiaryLanguageDTO("it", LanguageLevel.B1)))))
                .isInstanceOf(FieldValidationException.class)
                .extracting("field").isEqualTo("languages");
        verify(beneficiaries, never()).save(any());
    }

    @Test
    void register_rifiutaZonaDisattivata() {
        Zone closed = TestEntities.zone("ZCHIUSA");
        closed.setActive(false);
        when(zones.findById(123L)).thenReturn(Optional.of(closed));

        assertThatThrownBy(() -> service.register(dto(List.of())))
                .isInstanceOf(FieldValidationException.class)
                .extracting("field").isEqualTo("residenceZoneId");
    }

    @Test
    void getById_idNonValido_notFound() {
        assertThatThrownBy(() -> service.getById("xyz")).isInstanceOf(NotFoundException.class);
    }
}
