package com.desafio.room_reserve_api.dto.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateRoomDto(
        @NotBlank
        String name,
        @NotNull
        @Positive
        Integer capacity,
        @NotBlank
        String location
) {
}
