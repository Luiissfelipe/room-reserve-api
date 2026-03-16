package com.desafio.room_reserve_api.service;

import com.desafio.room_reserve_api.dto.reservation.CreateReservationDto;
import com.desafio.room_reserve_api.dto.reservation.ReservationDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository, RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    public Page<ReservationDto> listReservations(Pageable pageable) {
        return reservationRepository
                .findAllByReservationStatusNot(ReservationStatus.CANCELLED, pageable)
                .map(ReservationDto::new);
    }

    public ReservationDto listReservationById(Long id) {
        return reservationRepository
                .findById(id)
                .map(ReservationDto::new)
                .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada!"));
    }

    /**
     * Anotado com @Transactional para garantir a atomicidade da operação.
     * Isso assegura que a leitura (verificação de conflito) e a gravação (criação da reserva)
     * ocorram dentro da mesma transação do banco de dados. Se a gravação falhar,
     * toda a operação sofre rollback, prevenindo condições de corrida e inconsistência de dados
     */
    @Transactional
    public void createReservation(CreateReservationDto dto) {
        User user = userRepository
                .findById(dto.userId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado!"));

        Room room = roomRepository
                .findById(dto.roomId())
                .orElseThrow(() -> new EntityNotFoundException("Sala não encontrada!"));

        if (!user.getActive()) {
            throw new ValidationException("Não é possível criar reserva para um usuário inativo.");
        }

        if (!room.getActive()) {
            throw new ValidationException("Esta sala está inativa e não pode ser reservada.");
        }

        boolean existsConflictingReservation = reservationRepository
                .existsConflictingReservation(
                        dto.roomId(),
                        List.of(ReservationStatus.CONFIRMED, ReservationStatus.IN_PROGRESS),
                        dto.startDate(),
                        dto.endDate()
                );

        if (existsConflictingReservation) {
            throw new ValidationException("Já existe uma reserva na data selecionada!");
        }
        reservationRepository.save(new Reservation(dto, user, room));
    }

    @Transactional
    public void updateReservation(Long id, UpdateReservationDto dto) {
        Reservation reservation = reservationRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada;"));

        boolean existsConflictingReservation = reservationRepository
                .existsConflictingReservationForUpdate(
                        reservation.getRoom().getId(),
                        reservation.getId(),
                        List.of(ReservationStatus.CONFIRMED, ReservationStatus.IN_PROGRESS),
                        dto.startDate(),
                        dto.endDate()
                );

        if (existsConflictingReservation) {
            throw new ValidationException("Já existe uma reserva na data selecionada!");
        }

        reservation.updateReservation(dto);
        reservationRepository.save(reservation);
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada!"));

        reservation.cancel();
        reservationRepository.save(reservation);
    }

    @Transactional
    public void completedReservation(Long id) {
        Reservation reservation = reservationRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada!"));

        reservation.completed();
        reservationRepository.save(reservation);
    }
}
