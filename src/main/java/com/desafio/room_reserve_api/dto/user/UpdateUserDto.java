package com.desafio.room_reserve_api.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserDto(
        @NotBlank
        String name,
        @NotBlank
        String email,
        @NotBlank
        String password
) {
}
