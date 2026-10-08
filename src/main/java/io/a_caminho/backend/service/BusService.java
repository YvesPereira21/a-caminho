package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.bus.BusCreateDTO;
import io.a_caminho.backend.dto.bus.BusDTO;
import io.a_caminho.backend.dto.bus.BusUpdateDTO;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.mapper.BusMapper;
import io.a_caminho.backend.model.Bus;
import io.a_caminho.backend.model.BusDriver;
import io.a_caminho.backend.model.Municipality;
import io.a_caminho.backend.repository.BusDriverRepository;
import io.a_caminho.backend.repository.BusRepository;
import io.a_caminho.backend.repository.MunicipalityRepository;
import io.a_caminho.backend.repository.TravelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BusService {

    private final BusRepository busRepository;
    private final BusMapper busMapper;
    private final MunicipalityRepository municipalityRepository;
    private final BusDriverRepository busDriverRepository;
    private final TravelRepository travelRepository;

    public BusService(
            BusRepository busRepository,
            BusMapper busMapper,
            MunicipalityRepository municipalityRepository,
            BusDriverRepository busDriverRepository,
            TravelRepository travelRepository
    ) {
        this.busRepository = busRepository;
        this.busMapper = busMapper;
        this.municipalityRepository = municipalityRepository;
        this.busDriverRepository = busDriverRepository;
        this.travelRepository = travelRepository;
    }

    @Transactional
    public BusDTO createBus(UUID municipalityUserId, BusCreateDTO busCreateDTO) {
        Municipality municipality = municipalityRepository.findByUser_UserId(municipalityUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Prefeitura não encontrada."));

        boolean busExists = busRepository.existsByMunicipality_MunicipalityIdAndBusNameIgnoreCase(
                municipality.getMunicipalityId(),
                busCreateDTO.busName()
        );
        if (busExists) {
            throw new ObjectAlreadyExistsException("Já existe um ônibus cadastrado com este nome.");
        }

        Bus newBus = busMapper.toEntity(busCreateDTO);
        newBus.setMunicipality(municipality);

        return busMapper.toDTO(busRepository.save(newBus));
    }

    @Transactional(readOnly = true)
    public BusDTO getBusById(UUID municipalityUserId, UUID busId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ObjectNotFoundException("Ônibus não encontrado."));

        validateBusOwnership(bus, municipalityUserId);

        return busMapper.toDTO(bus);
    }

    @Transactional(readOnly = true)
    public List<BusDTO> getAllBusesByMunicipality(UUID municipalityUserId) {
        municipalityRepository.findByUser_UserId(municipalityUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Prefeitura não encontrada."));

        return busRepository.findAllByMunicipality_User_UserId(municipalityUserId)
                .stream()
                .map(busMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BusDTO> getAllBusesForDriver(UUID busDriverUserId) {
        BusDriver busDriver = busDriverRepository.findByUser_UserId(busDriverUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Motorista não encontrado."));

        return busRepository.findAllByMunicipality_MunicipalityId(busDriver.getMunicipality().getMunicipalityId())
                .stream()
                .map(busMapper::toDTO)
                .toList();
    }

    @Transactional
    public BusDTO updateBus(UUID municipalityUserId, UUID busId, BusUpdateDTO busUpdateDTO) {
        Municipality municipality = municipalityRepository.findByUser_UserId(municipalityUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Prefeitura não encontrada."));

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ObjectNotFoundException("Ônibus não encontrado."));

        validateBusOwnership(bus, municipalityUserId);

        if (busUpdateDTO.busName() != null && !busUpdateDTO.busName().equalsIgnoreCase(bus.getBusName())) {
            boolean busExists = busRepository.existsByMunicipality_MunicipalityIdAndBusNameIgnoreCase(
                    municipality.getMunicipalityId(),
                    busUpdateDTO.busName()
            );
            if (busExists) {
                throw new ObjectAlreadyExistsException("Já existe um ônibus cadastrado com este nome.");
            }
        }

        Bus updatedBus = busMapper.updateEntityFromDTO(busUpdateDTO, bus);
        return busMapper.toDTO(busRepository.save(updatedBus));
    }

    @Transactional
    public void deleteBus(UUID municipalityUserId, UUID busId) {
        municipalityRepository.findByUser_UserId(municipalityUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Prefeitura não encontrada."));

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ObjectNotFoundException("Ônibus não encontrado."));

        validateBusOwnership(bus, municipalityUserId);

        if (travelRepository.existsByBus_BusId(busId)) {
            throw new IllegalArgumentException("Não é possível excluir este ônibus pois já existem viagens vinculadas a ele.");
        }

        busRepository.delete(bus);
    }

    private void validateBusOwnership(Bus bus, UUID municipalityUserId) {
        if (!bus.getMunicipality().getUser().getId().equals(municipalityUserId)) {
            throw new UserIsNotOwnerException("Você não tem permissão para acessar este recurso.");
        }
    }
}
