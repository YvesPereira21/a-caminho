package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.polloption.PollOptionResponseDTO;
import io.a_caminho.backend.dto.polltemplate.PollTemplateCreateDTO;
import io.a_caminho.backend.dto.polltemplate.PollTemplateDTO;
import io.a_caminho.backend.dto.university.UniversityResponseDTO;
import io.a_caminho.backend.exception.InvalidTimeException;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.mapper.PollTemplateMapper;
import io.a_caminho.backend.model.City;
import io.a_caminho.backend.model.Municipality;
import io.a_caminho.backend.model.PollOption;
import io.a_caminho.backend.model.PollTemplate;
import io.a_caminho.backend.model.State;
import io.a_caminho.backend.model.University;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.Shift;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.repository.MunicipalityRepository;
import io.a_caminho.backend.repository.PollOptionRepository;
import io.a_caminho.backend.repository.PollTemplateRepository;
import io.a_caminho.backend.repository.UniversityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - PollTemplateService")
class PollTemplateServiceTest {

    @Mock
    private PollTemplateRepository pollTemplateRepository;

    @Mock
    private PollTemplateMapper pollTemplateMapper;

    @Mock
    private MunicipalityRepository municipalityRepository;

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private PollOptionRepository pollOptionRepository;

    @InjectMocks
    private PollTemplateService pollTemplateService;

    private UUID municipalityUserId;
    private UUID municipalityId;
    private UUID otherMunicipalityId;
    private UUID templateId;
    private UUID universityId;
    private UUID optionId;

    private Municipality municipality;
    private Municipality otherMunicipality;
    private University university;
    private PollOption pollOption;
    private PollOption otherMunicipalityOption;
    private PollTemplate pollTemplate;
    private PollTemplateCreateDTO createDTO;
    private PollTemplateDTO responseDTO;

    @BeforeEach
    void setUp() {
        municipalityUserId = UUID.randomUUID();
        municipalityId = UUID.randomUUID();
        otherMunicipalityId = UUID.randomUUID();
        templateId = UUID.randomUUID();
        universityId = UUID.randomUUID();
        optionId = UUID.randomUUID();

        User municipalityUser = User.builder()
                .userId(municipalityUserId)
                .email("prefeitura@municipio.gov.br")
                .role(UserRole.MUNICIPALITY)
                .build();

        User otherMunicipalityUser = User.builder()
                .userId(UUID.randomUUID())
                .email("outra@municipio.gov.br")
                .role(UserRole.MUNICIPALITY)
                .build();

        municipality = Municipality.builder()
                .municipalityId(municipalityId)
                .municipalityName("Prefeitura Municipal")
                .user(municipalityUser)
                .build();

        otherMunicipality = Municipality.builder()
                .municipalityId(otherMunicipalityId)
                .municipalityName("Outra Prefeitura")
                .user(otherMunicipalityUser)
                .build();

        State state = State.builder()
                .stateId(UUID.randomUUID())
                .stateName("Paraíba")
                .build();

        City city = City.builder()
                .cityId(UUID.randomUUID())
                .cityName("João Pessoa")
                .state(state)
                .build();

        university = University.builder()
                .universityId(universityId)
                .name("Universidade Federal da Paraíba")
                .campus("Campus I")
                .city(city)
                .build();

        pollOption = PollOption.builder()
                .optionId(optionId)
                .stopName("Praça Central")
                .municipality(municipality)
                .build();

        otherMunicipalityOption = PollOption.builder()
                .optionId(UUID.randomUUID())
                .stopName("Ponto de Outra Cidade")
                .municipality(otherMunicipality)
                .build();

        pollTemplate = PollTemplate.builder()
                .templateId(templateId)
                .routeName("Rota Universitária Manhã")
                .shift(Shift.MORNING)
                .defaultStartTime(LocalTime.of(6, 0))
                .defaultEndTime(LocalTime.of(12, 0))
                .active(true)
                .municipality(municipality)
                .targetUniversities(Set.of(university))
                .defaultOptions(Set.of(pollOption))
                .build();

        createDTO = new PollTemplateCreateDTO(
                "Rota Universitária Manhã",
                Shift.MORNING,
                LocalTime.of(6, 0),
                LocalTime.of(12, 0),
                Set.of(universityId),
                Set.of(optionId)
        );

        responseDTO = new PollTemplateDTO(
                templateId,
                "Rota Universitária Manhã",
                Shift.MORNING,
                LocalTime.of(6, 0),
                LocalTime.of(12, 0),
                true,
                Set.of(new UniversityResponseDTO(universityId, "Universidade Federal da Paraíba", "Campus I", "João Pessoa", "Paraíba")),
                Set.of(new PollOptionResponseDTO(optionId, "Praça Central", 0L))
        );
    }

    @Nested
    @DisplayName("Cenários de createPollTemplate")
    class CreatePollTemplateTests {

        @Test
        @DisplayName("Deve criar modelo de enquete com sucesso")
        void shouldCreatePollTemplateSuccessfully() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollTemplateRepository.existsByMunicipality_MunicipalityIdAndRouteNameIgnoreCaseAndShift(
                    municipalityId, createDTO.routeName(), createDTO.shift()))
                    .thenReturn(false);
            when(universityRepository.findAllById(createDTO.targetUniversityIds()))
                    .thenReturn(List.of(university));
            when(pollOptionRepository.findAllById(createDTO.defaultOptionIds()))
                    .thenReturn(List.of(pollOption));
            when(pollTemplateRepository.save(any(PollTemplate.class)))
                    .thenReturn(pollTemplate);
            when(pollTemplateMapper.toDTO(pollTemplate))
                    .thenReturn(responseDTO);

            PollTemplateDTO result = pollTemplateService.createPollTemplate(municipalityUserId, createDTO);

            assertNotNull(result);
            assertEquals(templateId, result.templateId());
            assertEquals("Rota Universitária Manhã", result.routeName());
            assertTrue(result.active());
            verify(pollTemplateRepository).save(any(PollTemplate.class));
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando prefeitura não for encontrada")
        void shouldThrowObjectNotFoundExceptionWhenMunicipalityNotFound() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollTemplateService.createPollTemplate(municipalityUserId, createDTO)
            );

            assertEquals("Prefeitura não existe.", exception.getMessage());
            verify(pollTemplateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar InvalidTimeException quando horários de início e término forem iguais")
        void shouldThrowInvalidTimeExceptionWhenStartAndEndTimeAreEqual() {
            PollTemplateCreateDTO invalidTimeDTO = new PollTemplateCreateDTO(
                    "Rota",
                    Shift.MORNING,
                    LocalTime.of(8, 0),
                    LocalTime.of(8, 0),
                    Set.of(universityId),
                    Set.of(optionId)
            );

            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));

            InvalidTimeException exception = assertThrows(
                    InvalidTimeException.class,
                    () -> pollTemplateService.createPollTemplate(municipalityUserId, invalidTimeDTO)
            );

            assertEquals("Horário de início e término não podem ser iguais.", exception.getMessage());
            verify(pollTemplateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectAlreadyExistsException quando rota com mesmo turno já existir no município")
        void shouldThrowObjectAlreadyExistsExceptionWhenTemplateAlreadyExists() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollTemplateRepository.existsByMunicipality_MunicipalityIdAndRouteNameIgnoreCaseAndShift(
                    municipalityId, createDTO.routeName(), createDTO.shift()))
                    .thenReturn(true);

            ObjectAlreadyExistsException exception = assertThrows(
                    ObjectAlreadyExistsException.class,
                    () -> pollTemplateService.createPollTemplate(municipalityUserId, createDTO)
            );

            assertEquals("Esse modelo de enquete já existe.", exception.getMessage());
            verify(pollTemplateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando uma ou mais universidades não forem encontradas")
        void shouldThrowObjectNotFoundExceptionWhenUniversityNotFound() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollTemplateRepository.existsByMunicipality_MunicipalityIdAndRouteNameIgnoreCaseAndShift(
                    municipalityId, createDTO.routeName(), createDTO.shift()))
                    .thenReturn(false);
            when(universityRepository.findAllById(createDTO.targetUniversityIds()))
                    .thenReturn(Collections.emptyList());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollTemplateService.createPollTemplate(municipalityUserId, createDTO)
            );

            assertEquals("Uma ou mais universidades informadas não foram encontradas.", exception.getMessage());
            verify(pollTemplateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando um ou mais pontos não forem encontrados")
        void shouldThrowObjectNotFoundExceptionWhenOptionNotFound() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollTemplateRepository.existsByMunicipality_MunicipalityIdAndRouteNameIgnoreCaseAndShift(
                    municipalityId, createDTO.routeName(), createDTO.shift()))
                    .thenReturn(false);
            when(universityRepository.findAllById(createDTO.targetUniversityIds()))
                    .thenReturn(List.of(university));
            when(pollOptionRepository.findAllById(createDTO.defaultOptionIds()))
                    .thenReturn(Collections.emptyList());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollTemplateService.createPollTemplate(municipalityUserId, createDTO)
            );

            assertEquals("Um ou mais pontos informados não foram encontrados.", exception.getMessage());
            verify(pollTemplateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException quando um ou mais pontos pertencerem a outro município")
        void shouldThrowUserIsNotOwnerExceptionWhenOptionBelongsToAnotherMunicipality() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollTemplateRepository.existsByMunicipality_MunicipalityIdAndRouteNameIgnoreCaseAndShift(
                    municipalityId, createDTO.routeName(), createDTO.shift()))
                    .thenReturn(false);
            when(universityRepository.findAllById(createDTO.targetUniversityIds()))
                    .thenReturn(List.of(university));
            when(pollOptionRepository.findAllById(createDTO.defaultOptionIds()))
                    .thenReturn(List.of(otherMunicipalityOption));

            UserIsNotOwnerException exception = assertThrows(
                    UserIsNotOwnerException.class,
                    () -> pollTemplateService.createPollTemplate(municipalityUserId, createDTO)
            );

            assertEquals("Um ou mais pontos não pertencem à sua prefeitura.", exception.getMessage());
            verify(pollTemplateRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cenários de getPollTemplate")
    class GetPollTemplateTests {

        @Test
        @DisplayName("Deve retornar modelo de enquete quando prefeitura for a proprietária")
        void shouldReturnPollTemplateWhenOwner() {
            when(pollTemplateRepository.findById(templateId))
                    .thenReturn(Optional.of(pollTemplate));
            when(pollTemplateMapper.toDTO(pollTemplate))
                    .thenReturn(responseDTO);

            PollTemplateDTO result = pollTemplateService.getPollTemplate(municipalityUserId, templateId);

            assertNotNull(result);
            assertEquals(templateId, result.templateId());
            verify(pollTemplateRepository).findById(templateId);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando modelo não for encontrado")
        void shouldThrowObjectNotFoundExceptionWhenTemplateNotFound() {
            when(pollTemplateRepository.findById(templateId))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollTemplateService.getPollTemplate(municipalityUserId, templateId)
            );

            assertEquals("Modelo de enquete não cadastrado.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException quando usuário não for a prefeitura proprietária")
        void shouldThrowUserIsNotOwnerExceptionWhenUserIsNotOwner() {
            UUID otherUserId = UUID.randomUUID();
            when(pollTemplateRepository.findById(templateId))
                    .thenReturn(Optional.of(pollTemplate));

            UserIsNotOwnerException exception = assertThrows(
                    UserIsNotOwnerException.class,
                    () -> pollTemplateService.getPollTemplate(otherUserId, templateId)
            );

            assertEquals("Você não tem permissão para isso.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cenários de getAllPollTemplateFromMunicipality")
    class GetAllPollTemplatesTests {

        @Test
        @DisplayName("Deve retornar lista de modelos de enquete do município")
        void shouldReturnListOfPollTemplates() {
            when(pollTemplateRepository.findAllByMunicipality_User_UserId(municipalityUserId))
                    .thenReturn(List.of(pollTemplate));
            when(pollTemplateMapper.toDTO(pollTemplate))
                    .thenReturn(responseDTO);

            List<PollTemplateDTO> result = pollTemplateService.getAllPollTemplateFromMunicipality(municipalityUserId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Rota Universitária Manhã", result.get(0).routeName());
            verify(pollTemplateRepository).findAllByMunicipality_User_UserId(municipalityUserId);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando município não possuir modelos cadastrados")
        void shouldReturnEmptyListWhenNoTemplatesFound() {
            when(pollTemplateRepository.findAllByMunicipality_User_UserId(municipalityUserId))
                    .thenReturn(Collections.emptyList());

            List<PollTemplateDTO> result = pollTemplateService.getAllPollTemplateFromMunicipality(municipalityUserId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Cenários de deactivatePollTemplate")
    class DeactivatePollTemplateTests {

        @Test
        @DisplayName("Deve desativar modelo de enquete com sucesso")
        void shouldDeactivatePollTemplateSuccessfully() {
            when(pollTemplateRepository.findById(templateId))
                    .thenReturn(Optional.of(pollTemplate));

            pollTemplateService.deactivatePollTemplate(municipalityUserId, templateId);

            assertFalse(pollTemplate.getActive());
            verify(pollTemplateRepository).save(pollTemplate);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException ao tentar desativar modelo inexistente")
        void shouldThrowObjectNotFoundExceptionWhenDeactivatingNonExistentTemplate() {
            when(pollTemplateRepository.findById(templateId))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollTemplateService.deactivatePollTemplate(municipalityUserId, templateId)
            );

            assertEquals("Modelo de enquete não cadastrado.", exception.getMessage());
            verify(pollTemplateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException ao tentar desativar modelo de outra prefeitura")
        void shouldThrowUserIsNotOwnerExceptionWhenDeactivatingOtherMunicipalityTemplate() {
            UUID otherUserId = UUID.randomUUID();
            when(pollTemplateRepository.findById(templateId))
                    .thenReturn(Optional.of(pollTemplate));

            UserIsNotOwnerException exception = assertThrows(
                    UserIsNotOwnerException.class,
                    () -> pollTemplateService.deactivatePollTemplate(otherUserId, templateId)
            );

            assertEquals("Você não tem permissão para isso.", exception.getMessage());
            verify(pollTemplateRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cenários de deletePollTemplate")
    class DeletePollTemplateTests {

        @Test
        @DisplayName("Deve excluir modelo de enquete com sucesso")
        void shouldDeletePollTemplateSuccessfully() {
            when(pollTemplateRepository.findById(templateId))
                    .thenReturn(Optional.of(pollTemplate));

            pollTemplateService.deletePollTemplate(municipalityUserId, templateId);

            verify(pollTemplateRepository).delete(pollTemplate);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException ao tentar excluir modelo inexistente")
        void shouldThrowObjectNotFoundExceptionWhenDeletingNonExistentTemplate() {
            when(pollTemplateRepository.findById(templateId))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollTemplateService.deletePollTemplate(municipalityUserId, templateId)
            );

            assertEquals("Modelo de enquete não cadastrado.", exception.getMessage());
            verify(pollTemplateRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException ao tentar excluir modelo de outra prefeitura")
        void shouldThrowUserIsNotOwnerExceptionWhenDeletingOtherMunicipalityTemplate() {
            UUID otherUserId = UUID.randomUUID();
            when(pollTemplateRepository.findById(templateId))
                    .thenReturn(Optional.of(pollTemplate));

            UserIsNotOwnerException exception = assertThrows(
                    UserIsNotOwnerException.class,
                    () -> pollTemplateService.deletePollTemplate(otherUserId, templateId)
            );

            assertEquals("Você não tem permissão para isso.", exception.getMessage());
            verify(pollTemplateRepository, never()).delete(any());
        }
    }
}
