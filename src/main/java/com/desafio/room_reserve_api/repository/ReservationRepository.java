package com.desafio.room_reserve_api.repository;

import com.desafio.room_reserve_api.model.Reservation;
import com.desafio.room_reserve_api.model.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findAllByReservationStatusNot(ReservationStatus status, Pageable pageable);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.room.id = :roomId " +
            "AND r.reservationStatus IN :statuses " +
            "AND r.startDate < :endDate " +
            "AND r.endDate > :startDate")
    boolean existsConflictingReservation(
            @Param("roomId") Long roomId,
            @Param("statuses") List<ReservationStatus> statuses,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.room.id = :roomId " +
            "AND r.id <> :reservationId " +
            "AND r.reservationStatus IN :statuses " +
            "AND r.startDate < :endDate " +
            "AND r.endDate > :startDate")
    boolean existsConflictingReservationForUpdate(
            @Param("roomId") Long roomId,
            @Param("reservationId") Long reservationId,
            @Param("statuses") List<ReservationStatus> statuses,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
