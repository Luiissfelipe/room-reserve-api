package com.desafio.room_reserve_api.service;

import com.desafio.room_reserve_api.dto.reservation.CreateReservationDto;
import com.desafio.room_reserve_api.dto.reservation.UpdateReservationDto;
import com.desafio.room_reserve_api.exception.ValidationException;
import com.desafio.room_reserve_api.model.Reservation;
import com.desafio.room_reserve_api.model.ReservationStatus;
import com.desafio.room_reserve_api.model.Room;
import com.desafio.room_reserve_api.model.User;
import com.desafio.room_reserve_api.repository.ReservationRepository;
import com.desafio.room_reserve_api.repository.RoomRepository;
import com.desafio.room_reserve_api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o usuário não for encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        CreateReservationDto dto = new CreateReservationDto(
                "Reunião",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                1L,
                1L);

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Room room = new Room();
        room.setId(1L);
        room.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> reservationService.createReservation(dto));
        verifyNoInteractions(roomRepository, reservationRepository);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando a sala não for encontrada")
    void shouldThrowExceptionWhenRoomNotFound() {
        // Arrange
        CreateReservationDto dto = new CreateReservationDto(
                "Reunião",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                1L,
                1L);

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Room room = new Room();
        room.setId(1L);
        room.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> reservationService.createReservation(dto));
        verifyNoInteractions(reservationRepository);
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando o usuário for inativo")
    void shouldThrowExceptionWhenUserIsInactive() {
        // Arrange
        CreateReservationDto dto = new CreateReservationDto(
                "Reunião",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                1L,
                1L);

        User user = new User();
        user.setId(1L);
        user.setActive(false);

        Room room = new Room();
        room.setId(1L);
        room.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        // Act & Assert
        assertThrows(ValidationException.class, () -> reservationService.createReservation(dto));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando a sala for inativa")
    void shouldThrowExceptionWhenRoomIsInactive() {
        // Arrange
        CreateReservationDto dto = new CreateReservationDto(
                "Reunião",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                1L,
                1L);

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Room room = new Room();
        room.setId(1L);
        room.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        // Act & Assert
        assertThrows(ValidationException.class, () -> reservationService.createReservation(dto));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando houver conflito de horários")
    void shouldThrowExceptionWhenTimeOverlaps() {
        // Arrange
        CreateReservationDto dto = new CreateReservationDto(
                "Reunião",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                1L,
                1L);

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Room room = new Room();
        room.setId(1L);
        room.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        when(reservationRepository.existsConflictingReservation(anyLong(), anyList(), any(), any()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> reservationService.createReservation(dto));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve criar reserva com sucesso quando todos os dados forem válidos")
    void shouldCreateReservationSuccessfully() {
        // Arrange
        CreateReservationDto dto = new CreateReservationDto(
                "Reunião",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                1L,
                1L);

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Room room = new Room();
        room.setId(1L);
        room.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        when(reservationRepository.existsConflictingReservation(anyLong(), anyList(), any(), any()))
                .thenReturn(false);

        // Act
        reservationService.createReservation(dto);

        // Assert
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao tentar atualizar reserva inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentReservation() {
        // Arrange
        UpdateReservationDto updateDto = new UpdateReservationDto(
                "Novo Propósito da Reunião",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(3)
        );

        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> reservationService.updateReservation(1L, updateDto));

        verify(reservationRepository, never())
                .existsConflictingReservationForUpdate(anyLong(), anyLong(), anyList(), any(), any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ValidationException ao tentar atualizar para um horário com conflito")
    void shouldThrowExceptionWhenUpdateCausesTimeOverlap() {
        // Arrange
        UpdateReservationDto updateDto = new UpdateReservationDto(
                "Novo Propósito da Reunião",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(3)
        );

        Room room = new Room();
        room.setId(1L);
        room.setActive(true);

        Reservation existingReservation = new Reservation();
        existingReservation.setId(1L);
        existingReservation.setRoom(room);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(existingReservation));


        when(reservationRepository.existsConflictingReservationForUpdate(
                anyLong(), anyLong(), anyList(), any(), any()
        )).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> reservationService.updateReservation(1L, updateDto));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar reserva com sucesso quando não houver conflito")
    void shouldUpdateReservationSuccessfully() {
        // Arrange
        UpdateReservationDto updateDto = new UpdateReservationDto(
                "Novo Propósito da Reunião",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(3)
        );

        Room room = new Room();
        room.setId(1L);
        room.setActive(true);

        Reservation existingReservation = new Reservation();
        existingReservation.setId(1L);
        existingReservation.setRoom(room);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(existingReservation));


        when(reservationRepository.existsConflictingReservationForUpdate(
                anyLong(), anyLong(), anyList(), any(), any()
        )).thenReturn(false);

        // Act
        reservationService.updateReservation(1L, updateDto);

        // Assert
        verify(reservationRepository, times(1)).save(existingReservation);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao tentar cancelar reserva inexistente")
    void shouldThrowExceptionWhenCancelingNonExistentReservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reservationService.deleteReservation(1L));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve cancelar reserva com sucesso")
    void shouldCancelReservationSuccessfully() {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStartDate(LocalDateTime.now().plusDays(1));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // Act
        reservationService.deleteReservation(1L);

        // Assert
        assertEquals(ReservationStatus.CANCELLED, reservation.getReservationStatus());
        verify(reservationRepository, times(1)).save(reservation);
    }

    @Test
    @DisplayName("Deve lançar ValidationException ao tentar cancelar reserva que já iniciou")
    void shouldThrowExceptionWhenCancelingPastReservation() {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStartDate(LocalDateTime.now().minusDays(1));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // Act & Assert
        assertThrows(ValidationException.class, () -> reservationService.deleteReservation(1L));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao tentar completar reserva inexistente")
    void shouldThrowExceptionWhenCompletingNonExistentReservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reservationService.completedReservation(1L));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ValidationException ao tentar completar reserva não finalizada")
    void shouldThrowExceptionWhenCompletingOngoingReservation() {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setEndDate(LocalDateTime.now().plusDays(1));
        reservation.setReservationStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // Act & Assert
        assertThrows(ValidationException.class, () -> reservationService.completedReservation(1L));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve completar reserva com sucesso")
    void shouldCompleteReservationSuccessfully() {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setEndDate(LocalDateTime.now().minusDays(1));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // Act
        reservationService.completedReservation(1L);

        // Assert
        assertEquals(ReservationStatus.COMPLETED, reservation.getReservationStatus());
        verify(reservationRepository, times(1)).save(reservation);
    }

}