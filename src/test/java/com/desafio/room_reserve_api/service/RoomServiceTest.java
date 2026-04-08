package com.desafio.room_reserve_api.service;

import com.desafio.room_reserve_api.dto.room.CreateRoomDto;
import com.desafio.room_reserve_api.dto.room.RoomDto;
import com.desafio.room_reserve_api.dto.room.UpdateRoomDto;
import com.desafio.room_reserve_api.exception.ValidationException;
import com.desafio.room_reserve_api.model.Room;
import com.desafio.room_reserve_api.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository repository;

    @InjectMocks
    private RoomService roomService;

    private Room activeRoom;
    private CreateRoomDto createDto;
    private UpdateRoomDto updateDto;

    @BeforeEach
    void setUp() {
        activeRoom = new Room();
        activeRoom.setId(1L);
        activeRoom.setName("Sala A");
        activeRoom.setActive(true);

        createDto = new CreateRoomDto("Sala A", 10, "1º Andar");
        updateDto = new UpdateRoomDto("Sala B", 20, "2º Andar");
    }

    @Test
    @DisplayName("Deve listar apenas salas ativas")
    void shouldListAllActiveRooms() {
        when(repository.findAllByActiveTrue()).thenReturn(List.of(activeRoom));

        List<RoomDto> result = roomService.listRooms();

        assertEquals(1, result.size());
        verify(repository, times(1)).findAllByActiveTrue();
    }

    @Test
    @DisplayName("Deve retornar RoomDto ao buscar por ID válido")
    void shouldReturnRoomByIdSuccessfully() {
        when(repository.findById(1L)).thenReturn(Optional.of(activeRoom));

        RoomDto result = roomService.listRoomById(1L);

        assertNotNull(result);
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao buscar por ID inexistente")
    void shouldThrowExceptionWhenRoomNotFoundById() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> roomService.listRoomById(1L));
    }

    @Test
    @DisplayName("Deve criar sala com sucesso")
    void shouldCreateRoomSuccessfully() {
        when(repository.existsByName(createDto.name())).thenReturn(false);

        roomService.createRoom(createDto);

        verify(repository, times(1)).save(any(Room.class));
    }

    @Test
    @DisplayName("Deve lançar ValidationException ao criar sala com nome duplicado")
    void shouldThrowExceptionWhenCreatingRoomWithExistingName() {
        when(repository.existsByName(createDto.name())).thenReturn(true);

        assertThrows(ValidationException.class, () -> roomService.createRoom(createDto));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar sala com sucesso")
    void shouldUpdateRoomSuccessfully() {
        when(repository.findById(1L)).thenReturn(Optional.of(activeRoom));

        roomService.updateRoom(1L, updateDto);

        verify(repository, times(1)).save(activeRoom);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao atualizar sala inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentRoom() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> roomService.updateRoom(1L, updateDto));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve desativar a sala com sucesso (Soft Delete)")
    void shouldDeleteRoomSuccessfully() {
        when(repository.findById(1L)).thenReturn(Optional.of(activeRoom));

        roomService.deleteRoom(1L);

        verify(repository, times(1)).save(activeRoom);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao deletar sala inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentRoom() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> roomService.deleteRoom(1L));

        verify(repository, never()).save(any());
    }
}