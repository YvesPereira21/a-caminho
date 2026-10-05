package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.polloption.PollOptionDTO;
import io.a_caminho.backend.dto.polloption.PollOptionResponseDTO;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.mapper.PollOptionMapper;
import io.a_caminho.backend.model.Municipality;
import io.a_caminho.backend.model.PollOption;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.repository.MunicipalityRepository;
import io.a_caminho.backend.repository.PollOptionRepository;
import io.a_caminho.backend.repository.PollVoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - PollOptionService")
class PollOptionServiceTest {

    @Mock
    private PollOptionRepository pollOptionRepository;

    @Mock
    private PollVoteRepository pollVoteRepository;

    @Mock
    private PollOptionMapper pollOptionMapper;

    @Mock
    private MunicipalityRepository municipalityRepository;

    @InjectMocks
    private PollOptionService pollOptionService;

    private UUID municipalityUserId;
    private UUID otherUserId;
    private UUID municipalityId;
    private UUID optionId;

    private User municipalityUser;
    private Municipality municipality;
    private PollOption pollOption;
    private PollOptionDTO pollOptionDTO;
    private PollOptionResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        municipalityUserId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        municipalityId = UUID.randomUUID();
        optionId = UUID.randomUUID();

        municipalityUser = User.builder()
                .userId(municipalityUserId)
                .email("prefeitura@municipio.gov.br")
                .role(UserRole.MUNICIPALITY)
                .build();

        municipality = Municipality.builder()
                .municipalityId(municipalityId)
                .municipalityName("Prefeitura de João Pessoa")
                .user(municipalityUser)
                .build();

        pollOption = PollOption.builder()
                .optionId(optionId)
                .stopName("Praça Central")
                .municipality(municipality)
                .build();

        pollOptionDTO = new PollOptionDTO("Praça Central");
        responseDTO = new PollOptionResponseDTO(optionId, "Praça Central", 0L);
    }

    @Nested
    @DisplayName("Cenários de createPollOption")
    class CreatePollOptionTests {

        @Test
        @DisplayName("Deve cadastrar ponto de ônibus com sucesso quando dados válidos")
        void shouldCreatePollOptionSuccessfully() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollOptionRepository.existsByMunicipality_MunicipalityIdAndStopNameIgnoreCase(
                    municipalityId, pollOptionDTO.stopName()))
                    .thenReturn(false);
            when(pollOptionMapper.toEntity(pollOptionDTO)).thenReturn(pollOption);
            when(pollOptionRepository.save(pollOption)).thenReturn(pollOption);
            when(pollOptionMapper.toDTO(pollOption)).thenReturn(responseDTO);

            PollOptionResponseDTO result = pollOptionService.createPollOption(municipalityUserId, pollOptionDTO);

            assertNotNull(result);
            assertEquals(optionId, result.optionId());
            assertEquals("Praça Central", result.stopName());
            verify(pollOptionRepository).save(pollOption);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando prefeitura não for encontrada")
        void shouldThrowObjectNotFoundExceptionWhenMunicipalityNotFound() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollOptionService.createPollOption(municipalityUserId, pollOptionDTO)
            );

            assertEquals("Prefeitura não existe.", exception.getMessage());
            verify(pollOptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectAlreadyExistsException quando ponto com mesmo nome já existir no município")
        void shouldThrowObjectAlreadyExistsExceptionWhenStopNameAlreadyExists() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollOptionRepository.existsByMunicipality_MunicipalityIdAndStopNameIgnoreCase(
                    municipalityId, pollOptionDTO.stopName()))
                    .thenReturn(true);

            ObjectAlreadyExistsException exception = assertThrows(
                    ObjectAlreadyExistsException.class,
                    () -> pollOptionService.createPollOption(municipalityUserId, pollOptionDTO)
            );

            assertEquals("Já existe um ponto de ônibus cadastrado com este nome.", exception.getMessage());
            verify(pollOptionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cenários de getPollOption")
    class GetPollOptionTests {

        @Test
        @DisplayName("Deve retornar ponto de ônibus com sucesso quando prefeitura for a dona")
        void shouldReturnPollOptionWhenMunicipalityOwner() {
            when(pollOptionRepository.findById(optionId)).thenReturn(Optional.of(pollOption));
            when(pollOptionMapper.toDTO(pollOption)).thenReturn(responseDTO);

            PollOptionResponseDTO result = pollOptionService.getPollOption(municipalityUserId, optionId);

            assertNotNull(result);
            assertEquals(optionId, result.optionId());
            assertEquals("Praça Central", result.stopName());
            verify(pollOptionRepository).findById(optionId);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando ponto de ônibus não for encontrado")
        void shouldThrowObjectNotFoundExceptionWhenPollOptionNotFound() {
            when(pollOptionRepository.findById(optionId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollOptionService.getPollOption(municipalityUserId, optionId)
            );

            assertEquals("Ponto não cadastrado.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException quando usuário não for o dono do ponto")
        void shouldThrowUserIsNotOwnerExceptionWhenUserIsNotOwner() {
            when(pollOptionRepository.findById(optionId)).thenReturn(Optional.of(pollOption));

            UserIsNotOwnerException exception = assertThrows(
                    UserIsNotOwnerException.class,
                    () -> pollOptionService.getPollOption(otherUserId, optionId)
            );

            assertEquals("Você não tem permissão para isso.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cenários de getAllPollOptions")
    class GetAllPollOptionsTests {

        @Test
        @DisplayName("Deve retornar lista de pontos de ônibus do município com sucesso")
        void shouldReturnListOfPollOptionsWhenMunicipalityExists() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollOptionRepository.findAllByMunicipality_User_UserId(municipalityUserId))
                    .thenReturn(List.of(pollOption));
            when(pollOptionMapper.toDTO(pollOption)).thenReturn(responseDTO);

            List<PollOptionResponseDTO> result = pollOptionService.getAllPollOptions(municipalityUserId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Praça Central", result.get(0).stopName());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando município não possuir pontos cadastrados")
        void shouldReturnEmptyListWhenNoOptionsExist() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollOptionRepository.findAllByMunicipality_User_UserId(municipalityUserId))
                    .thenReturn(Collections.emptyList());

            List<PollOptionResponseDTO> result = pollOptionService.getAllPollOptions(municipalityUserId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando prefeitura não for encontrada")
        void shouldThrowObjectNotFoundExceptionWhenMunicipalityNotFound() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollOptionService.getAllPollOptions(municipalityUserId)
            );

            assertEquals("Prefeitura não existe.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cenários de updatePollOption")
    class UpdatePollOptionTests {

        @Test
        @DisplayName("Deve atualizar ponto com sucesso quando nome permanecer o mesmo")
        void shouldUpdatePollOptionSuccessfullyWhenNameUnchanged() {
            PollOptionDTO sameNameDTO = new PollOptionDTO("praça central");

            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollOptionRepository.findById(optionId))
                    .thenReturn(Optional.of(pollOption));
            when(pollOptionMapper.updateEntityFromDTO(sameNameDTO, pollOption))
                    .thenReturn(pollOption);

            pollOptionService.updatePollOption(municipalityUserId, optionId, sameNameDTO);

            verify(pollOptionRepository).save(pollOption);
            verify(pollOptionRepository, never()).existsByMunicipality_MunicipalityIdAndStopNameIgnoreCase(any(), any());
        }

        @Test
        @DisplayName("Deve atualizar ponto com sucesso quando novo nome não for duplicado")
        void shouldUpdatePollOptionSuccessfullyWhenNameChangedAndNotDuplicate() {
            PollOptionDTO newNameDTO = new PollOptionDTO("Novo Ponto de Ônibus");

            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollOptionRepository.findById(optionId))
                    .thenReturn(Optional.of(pollOption));
            when(pollOptionRepository.existsByMunicipality_MunicipalityIdAndStopNameIgnoreCase(
                    municipalityId, newNameDTO.stopName()))
                    .thenReturn(false);
            when(pollOptionMapper.updateEntityFromDTO(newNameDTO, pollOption))
                    .thenReturn(pollOption);

            pollOptionService.updatePollOption(municipalityUserId, optionId, newNameDTO);

            verify(pollOptionRepository).save(pollOption);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando prefeitura não for encontrada ao atualizar")
        void shouldThrowObjectNotFoundExceptionWhenMunicipalityNotFoundOnUpdate() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollOptionService.updatePollOption(municipalityUserId, optionId, pollOptionDTO)
            );

            assertEquals("Prefeitura não existe.", exception.getMessage());
            verify(pollOptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando ponto não for encontrado ao atualizar")
        void shouldThrowObjectNotFoundExceptionWhenOptionNotFoundOnUpdate() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollOptionRepository.findById(optionId))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollOptionService.updatePollOption(municipalityUserId, optionId, pollOptionDTO)
            );

            assertEquals("Ponto não cadastrado.", exception.getMessage());
            verify(pollOptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException quando usuário não for o dono do ponto ao atualizar")
        void shouldThrowUserIsNotOwnerExceptionWhenUserIsNotOwnerOnUpdate() {
            when(municipalityRepository.findByUser_UserId(otherUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollOptionRepository.findById(optionId))
                    .thenReturn(Optional.of(pollOption));

            UserIsNotOwnerException exception = assertThrows(
                    UserIsNotOwnerException.class,
                    () -> pollOptionService.updatePollOption(otherUserId, optionId, pollOptionDTO)
            );

            assertEquals("Você não tem permissão para isso.", exception.getMessage());
            verify(pollOptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectAlreadyExistsException quando novo nome já estiver em uso no município")
        void shouldThrowObjectAlreadyExistsExceptionWhenNewNameAlreadyExists() {
            PollOptionDTO duplicateDTO = new PollOptionDTO("Ponto Existente");

            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollOptionRepository.findById(optionId))
                    .thenReturn(Optional.of(pollOption));
            when(pollOptionRepository.existsByMunicipality_MunicipalityIdAndStopNameIgnoreCase(
                    municipalityId, duplicateDTO.stopName()))
                    .thenReturn(true);

            ObjectAlreadyExistsException exception = assertThrows(
                    ObjectAlreadyExistsException.class,
                    () -> pollOptionService.updatePollOption(municipalityUserId, optionId, duplicateDTO)
            );

            assertEquals("Já existe um ponto de ônibus cadastrado com este nome.", exception.getMessage());
            verify(pollOptionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cenários de deleteOption")
    class DeleteOptionTests {

        @Test
        @DisplayName("Deve excluir ponto de ônibus com sucesso quando não houver votos vinculados")
        void shouldDeleteOptionSuccessfullyWhenNoVotesLinked() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollOptionRepository.findById(optionId))
                    .thenReturn(Optional.of(pollOption));
            when(pollVoteRepository.existsByOption_OptionId(optionId))
                    .thenReturn(false);

            pollOptionService.deleteOption(municipalityUserId, optionId);

            verify(pollOptionRepository).delete(pollOption);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando prefeitura não for encontrada ao excluir")
        void shouldThrowObjectNotFoundExceptionWhenMunicipalityNotFoundOnDelete() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollOptionService.deleteOption(municipalityUserId, optionId)
            );

            assertEquals("Prefeitura não existe.", exception.getMessage());
            verify(pollOptionRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando ponto não for encontrado ao excluir")
        void shouldThrowObjectNotFoundExceptionWhenOptionNotFoundOnDelete() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollOptionRepository.findById(optionId))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollOptionService.deleteOption(municipalityUserId, optionId)
            );

            assertEquals("Ponto não cadastrado.", exception.getMessage());
            verify(pollOptionRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException quando usuário não for dono do ponto ao excluir")
        void shouldThrowUserIsNotOwnerExceptionWhenUserIsNotOwnerOnDelete() {
            when(municipalityRepository.findByUser_UserId(otherUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollOptionRepository.findById(optionId))
                    .thenReturn(Optional.of(pollOption));

            UserIsNotOwnerException exception = assertThrows(
                    UserIsNotOwnerException.class,
                    () -> pollOptionService.deleteOption(otherUserId, optionId)
            );

            assertEquals("Você não tem permissão para isso.", exception.getMessage());
            verify(pollOptionRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando existirem votos vinculados ao ponto")
        void shouldThrowIllegalArgumentExceptionWhenOptionHasVotesLinked() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(pollOptionRepository.findById(optionId))
                    .thenReturn(Optional.of(pollOption));
            when(pollVoteRepository.existsByOption_OptionId(optionId))
                    .thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> pollOptionService.deleteOption(municipalityUserId, optionId)
            );

            assertEquals("Não é possível excluir este ponto pois já existem votos vinculados a ele.", exception.getMessage());
            verify(pollOptionRepository, never()).delete(any());
        }
    }
}
