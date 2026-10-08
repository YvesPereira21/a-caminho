package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.state.StateDTO;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.mapper.StateMapper;
import io.a_caminho.backend.model.State;
import io.a_caminho.backend.repository.StateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - StateService")
class StateServiceTest {

    @Mock
    private StateRepository stateRepository;

    @Mock
    private StateMapper stateMapper;

    @InjectMocks
    private StateService stateService;

    @Nested
    @DisplayName("Cenários de createState (Cadastro de Estado)")
    class CreateStateTests {

        @Test
        @DisplayName("Deve cadastrar um estado com sucesso quando o nome não estiver em uso")
        void shouldCreateStateSuccessfully() {
            StateDTO requestDTO = new StateDTO("Paraíba");
            State stateEntity = State.builder()
                    .stateId(UUID.randomUUID())
                    .stateName("Paraíba")
                    .build();

            when(stateRepository.existsByStateName("Paraíba")).thenReturn(false);
            when(stateMapper.toEntity(requestDTO)).thenReturn(stateEntity);
            when(stateRepository.save(stateEntity)).thenReturn(stateEntity);
            when(stateMapper.toResponse(stateEntity)).thenReturn(requestDTO);

            StateDTO result = stateService.createState(requestDTO);

            assertNotNull(result);
            assertEquals("Paraíba", result.stateName());
            verify(stateRepository).existsByStateName("Paraíba");
            verify(stateRepository).save(stateEntity);
        }

        @Test
        @DisplayName("Deve lançar ObjectAlreadyExistsException quando estado com mesmo nome já existir")
        void shouldThrowObjectAlreadyExistsExceptionWhenStateAlreadyExists() {
            StateDTO requestDTO = new StateDTO("Paraíba");

            when(stateRepository.existsByStateName("Paraíba")).thenReturn(true);

            assertThrows(ObjectAlreadyExistsException.class, () -> stateService.createState(requestDTO));
            verify(stateRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cenários de getAllState (Listagem de Estados)")
    class GetAllStateTests {

        @Test
        @DisplayName("Deve retornar todos os estados cadastrados com sucesso")
        void shouldGetAllStatesSuccessfully() {
            State state1 = State.builder().stateId(UUID.randomUUID()).stateName("Paraíba").build();
            State state2 = State.builder().stateId(UUID.randomUUID()).stateName("Pernambuco").build();
            StateDTO dto1 = new StateDTO("Paraíba");
            StateDTO dto2 = new StateDTO("Pernambuco");

            when(stateRepository.findAll()).thenReturn(List.of(state1, state2));
            when(stateMapper.toResponse(state1)).thenReturn(dto1);
            when(stateMapper.toResponse(state2)).thenReturn(dto2);

            List<StateDTO> result = stateService.getAllState();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Paraíba", result.get(0).stateName());
            assertEquals("Pernambuco", result.get(1).stateName());
            verify(stateRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Cenários de deleteState (Exclusão de Estado)")
    class DeleteStateTests {

        @Test
        @DisplayName("Deve excluir um estado com sucesso quando ele for encontrado")
        void shouldDeleteStateSuccessfully() {
            String stateName = "Paraíba";
            State state = State.builder().stateId(UUID.randomUUID()).stateName(stateName).build();

            when(stateRepository.findByStateName(stateName)).thenReturn(Optional.of(state));

            stateService.deleteState(stateName);

            verify(stateRepository).findByStateName(stateName);
            verify(stateRepository).delete(state);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando estado a ser excluído não existir")
        void shouldThrowObjectNotFoundExceptionWhenStateNotFoundOnDelete() {
            String stateName = "EstadoInexistente";

            when(stateRepository.findByStateName(stateName)).thenReturn(Optional.empty());

            assertThrows(ObjectNotFoundException.class, () -> stateService.deleteState(stateName));
            verify(stateRepository, never()).delete(any());
        }
    }
}
