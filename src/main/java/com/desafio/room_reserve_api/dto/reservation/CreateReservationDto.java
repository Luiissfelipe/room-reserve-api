package com.desafio.room_reserve_api.dto.reservation;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateReservationDto(
        @NotBlank
        String purpose,
        @FutureOrPresent
        @NotNull
        LocalDateTime startDate,
        @Future
        @NotNull
        LocalDateTime endDate,
        @NotNull
        Long userId,
        @NotNull
        Long roomId
) {
}
