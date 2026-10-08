package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.poll.PollPassengerDTO;
import io.a_caminho.backend.dto.travel.TravelCreateDTO;
import io.a_caminho.backend.dto.travel.TravelDTO;
import io.a_caminho.backend.exception.CrossMunicipalityAccessException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.mapper.TravelMapper;
import io.a_caminho.backend.model.Bus;
import io.a_caminho.backend.model.BusDriver;
import io.a_caminho.backend.model.Municipality;
import io.a_caminho.backend.model.Poll;
import io.a_caminho.backend.model.PollVote;
import io.a_caminho.backend.model.Travel;
import io.a_caminho.backend.model.University;
import io.a_caminho.backend.model.enums.PollDirection;
import io.a_caminho.backend.model.enums.TravelStatus;
import io.a_caminho.backend.repository.BusDriverRepository;
import io.a_caminho.backend.repository.BusRepository;
import io.a_caminho.backend.repository.MunicipalityRepository;
import io.a_caminho.backend.repository.PollRepository;
import io.a_caminho.backend.repository.PollVoteRepository;
import io.a_caminho.backend.repository.TravelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TravelService {

    private final TravelRepository travelRepository;
    private final TravelMapper travelMapper;
    private final BusDriverRepository busDriverRepository;
    private final MunicipalityRepository municipalityRepository;
    private final BusRepository busRepository;
    private final PollRepository pollRepository;
    private final PollVoteRepository pollVoteRepository;

    public TravelService(
            TravelRepository travelRepository,
            TravelMapper travelMapper,
            BusDriverRepository busDriverRepository,
            MunicipalityRepository municipalityRepository,
            BusRepository busRepository,
            PollRepository pollRepository,
            PollVoteRepository pollVoteRepository
    ) {
        this.travelRepository = travelRepository;
        this.travelMapper = travelMapper;
        this.busDriverRepository = busDriverRepository;
        this.municipalityRepository = municipalityRepository;
        this.busRepository = busRepository;
        this.pollRepository = pollRepository;
        this.pollVoteRepository = pollVoteRepository;
    }

    @Transactional
    public TravelDTO createTravel(UUID busDriverUserId, TravelCreateDTO travelDTO) {
        BusDriver busDriver = busDriverRepository.findByUser_UserId(busDriverUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Motorista não encontrado."));
        Municipality municipality = busDriver.getMunicipality();

        if (municipality == null) {
            throw new ObjectNotFoundException("Município vinculado ao motorista não encontrado.");
        }

        Poll poll = pollRepository.findById(travelDTO.pollId())
                .orElseThrow(() -> new ObjectNotFoundException("Enquete não encontrada."));
        Bus bus = busRepository.findById(travelDTO.busId())
                .orElseThrow(() -> new ObjectNotFoundException("Ônibus não encontrado."));

        if (!poll.getMunicipality().getMunicipalityId().equals(municipality.getMunicipalityId())) {
            throw new CrossMunicipalityAccessException("A enquete selecionada não pertence ao seu município.");
        }

        if (!bus.getMunicipality().getMunicipalityId().equals(municipality.getMunicipalityId())) {
            throw new CrossMunicipalityAccessException("O ônibus selecionado não pertence ao seu município.");
        }

        if (travelRepository.existsByBusDriver_BusDriverIdAndStatus(busDriver.getBusDriverId(), TravelStatus.IN_PROGRESS)) {
            throw new IllegalArgumentException("O motorista já possui uma viagem em andamento.");
        }

        if (travelRepository.existsByBus_BusIdAndStatus(bus.getBusId(), TravelStatus.IN_PROGRESS)) {
            throw new IllegalArgumentException("O ônibus selecionado já está em uma viagem em andamento.");
        }

        Set<University> universities = poll.getTargetUniversities();

        Travel newTravel = travelMapper.toEntity(travelDTO);
        newTravel.setTravelDate(LocalDate.now());
        newTravel.setStatus(TravelStatus.IN_PROGRESS);
        newTravel.setDirection(PollDirection.OUTBOUND);
        newTravel.setDepartureTime(LocalTime.now());
        newTravel.setReturnTime(travelDTO.returnTime());
        newTravel.setShift(poll.getShift());
        newTravel.setBus(bus);
        newTravel.setBusDriver(busDriver);
        newTravel.setPoll(poll);
        newTravel.setMunicipality(municipality);
        newTravel.setTargetUniversities(universities);

        Travel savedTravel = travelRepository.save(newTravel);
        return travelMapper.toDTO(savedTravel);
    }

    @Transactional(readOnly = true)
    public TravelDTO getTravelById(UUID travelId, UUID userId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new ObjectNotFoundException("Viagem não encontrada."));

        validateMunicipalityOrDriverAccess(travel, userId);

        return travelMapper.toDTO(travel);
    }

    @Transactional(readOnly = true)
    public TravelDTO getActiveTravelByDriver(UUID busDriverUserId) {
        Travel travel = travelRepository.findByBusDriver_User_UserIdAndStatus(busDriverUserId, TravelStatus.IN_PROGRESS)
                .orElseThrow(() -> new ObjectNotFoundException("Nenhuma viagem em andamento encontrada para este motorista."));

        return travelMapper.toDTO(travel);
    }

    @Transactional(readOnly = true)
    public List<TravelDTO> getAllTravelsByDriver(UUID busDriverUserId) {
        busDriverRepository.findByUser_UserId(busDriverUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Motorista não encontrado."));

        return travelRepository.findAllByBusDriver_User_UserIdOrderByTravelDateDescDepartureTimeDesc(busDriverUserId)
                .stream()
                .map(travelMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TravelDTO> getAllTravelsByMunicipality(UUID municipalityUserId) {
        municipalityRepository.findByUser_UserId(municipalityUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Prefeitura não encontrada."));

        return travelRepository.findAllByMunicipality_User_UserIdOrderByTravelDateDescDepartureTimeDesc(municipalityUserId)
                .stream()
                .map(travelMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PollPassengerDTO> getTravelPassengers(UUID travelId, UUID userId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new ObjectNotFoundException("Viagem não encontrada."));

        validateMunicipalityOrDriverAccess(travel, userId);

        if (travel.getPoll() == null) {
            throw new ObjectNotFoundException("Esta viagem não possui uma enquete vinculada.");
        }

        List<PollVote> votes = pollVoteRepository.findAllWithStudentAndOptionByPollId(travel.getPoll().getPollId());

        if (travel.getDirection() == PollDirection.INBOUND) {
            votes = votes.stream()
                    .filter(vote -> Boolean.TRUE.equals(vote.getReturnConfirmed()))
                    .toList();
        }

        return votes.stream()
                .map(vote -> new PollPassengerDTO(
                        vote.getVoteId(),
                        vote.getStudent().getUniversityStudentId(),
                        vote.getStudent().getStudentName(),
                        vote.getStudent().getRegistrationNumber(),
                        vote.getStudent().getCourseName(),
                        vote.getOption().getOptionId(),
                        vote.getOption().getStopName(),
                        vote.getReturnConfirmed(),
                        vote.getVoteTime()
                ))
                .toList();
    }

    @Transactional
    public TravelDTO startReturn(UUID travelId, UUID busDriverUserId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new ObjectNotFoundException("Viagem não encontrada."));

        validateDriverOwnership(travel, busDriverUserId);

        if (travel.getStatus() != TravelStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Apenas viagens em andamento podem iniciar o retorno.");
        }

        if (travel.getDirection() == PollDirection.INBOUND) {
            throw new IllegalArgumentException("A viagem de retorno já foi iniciada.");
        }

        travel.setDirection(PollDirection.INBOUND);
        Travel updatedTravel = travelRepository.save(travel);
        return travelMapper.toDTO(updatedTravel);
    }

    @Transactional
    public TravelDTO completeTravel(UUID travelId, UUID busDriverUserId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new ObjectNotFoundException("Viagem não encontrada."));

        validateDriverOwnership(travel, busDriverUserId);

        if (travel.getStatus() != TravelStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Apenas viagens em andamento podem ser concluídas.");
        }

        travel.setStatus(TravelStatus.COMPLETED);
        Travel updatedTravel = travelRepository.save(travel);
        return travelMapper.toDTO(updatedTravel);
    }

    @Transactional
    public TravelDTO cancelTravel(UUID travelId, UUID userId, String cancellationReason) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new ObjectNotFoundException("Viagem não encontrada."));

        validateCancellationPermission(travel, userId);

        if (travel.getStatus() == TravelStatus.COMPLETED || travel.getStatus() == TravelStatus.CANCELLED) {
            throw new IllegalArgumentException("Não é possível cancelar uma viagem que já foi concluída ou cancelada.");
        }

        travel.setStatus(TravelStatus.CANCELLED);
        travel.setCancellationReason(cancellationReason != null && !cancellationReason.isBlank()
                ? cancellationReason
                : "Viagem cancelada sem motivo especificado.");

        Travel updatedTravel = travelRepository.save(travel);
        return travelMapper.toDTO(updatedTravel);
    }

    private void validateDriverOwnership(Travel travel, UUID busDriverUserId) {
        if (!travel.getBusDriver().getUser().getUserId().equals(busDriverUserId)) {
            throw new UserIsNotOwnerException("Você não tem permissão para alterar esta viagem pois não é o motorista responsável.");
        }
    }

    private void validateCancellationPermission(Travel travel, UUID userId) {
        boolean isDriver = travel.getBusDriver().getUser().getUserId().equals(userId);
        boolean isMunicipality = travel.getMunicipality().getUser().getUserId().equals(userId);

        if (!isDriver && !isMunicipality) {
            throw new UserIsNotOwnerException("Você não tem permissão para cancelar esta viagem.");
        }
    }

    private void validateMunicipalityOrDriverAccess(Travel travel, UUID userId) {
        boolean isDriver = travel.getBusDriver().getUser().getUserId().equals(userId);
        boolean isMunicipality = travel.getMunicipality().getUser().getUserId().equals(userId);

        if (!isDriver && !isMunicipality) {
            throw new CrossMunicipalityAccessException("Você não tem permissão para visualizar os dados desta viagem.");
        }
    }
}
