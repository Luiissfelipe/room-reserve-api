package com.desafio.room_reserve_api.dto.user;

import com.desafio.room_reserve_api.model.Role;
import com.desafio.room_reserve_api.model.User;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String name,
        String email,
        Role role,
        LocalDateTime creationDate,
        LocalDateTime updateDate
) {
    public UserDto(User user) {
        this(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreationDate(),
                user.getUpdateDate()
        );
    }
}
