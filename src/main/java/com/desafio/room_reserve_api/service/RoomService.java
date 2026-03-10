package com.desafio.room_reserve_api.service;

import com.desafio.room_reserve_api.dto.room.CreateRoomDto;
import com.desafio.room_reserve_api.dto.room.RoomDto;
import com.desafio.room_reserve_api.dto.room.UpdateRoomDto;
import com.desafio.room_reserve_api.exception.ValidationException;
import com.desafio.room_reserve_api.model.Room;
import com.desafio.room_reserve_api.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository repository;

    public RoomService(RoomRepository repository) {
        this.repository = repository;
    }

    public List<RoomDto> listRooms() {
        return repository
                .findAllByActiveTrue()
                .stream()
                .map(RoomDto::new)
                .toList();
    }

    public RoomDto listRoomById(Long id) {
        return repository
                .findById(id)
                .map(RoomDto::new)
                .orElseThrow(() -> new EntityNotFoundException("Sala não encontrada!"));
    }

    @Transactional
    public void createRoom(CreateRoomDto dto) {
        boolean isRoomExistis = repository.existsByName(dto.name());

        if (isRoomExistis) {
            throw new ValidationException("Já existe uma sala com esse nome!");
        }
        repository.save(new Room(dto));

    }

    @Transactional
    public void updateRoom(Long id, UpdateRoomDto dto) {
        Room room = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sala não encontrada!"));

        room.updateRoom(dto);
        repository.save(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sala não encontrada!"));

        room.disableRoom();
        repository.save(room);
    }
}
