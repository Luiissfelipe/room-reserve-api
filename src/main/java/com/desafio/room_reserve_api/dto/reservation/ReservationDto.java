package com.desafio.room_reserve_api.dto.reservation;

import com.desafio.room_reserve_api.model.Reservation;
import com.desafio.room_reserve_api.model.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationDto(
        Long id,
        String purpose,
        LocalDateTime startDate,
        LocalDateTime endDate,
        ReservationStatus reservationStatus,
        String userName,
        String roomName,
        LocalDateTime creationDate,
        LocalDateTime updateDate
) {
    public ReservationDto(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getPurpose(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getReservationStatus(),
                reservation.getUser().getName(),
                reservation.getRoom().getName(),
                reservation.getCreationDate(),
                reservation.getUpdateDate()
        );
    }
}
