package com.desafio.room_reserve_api.dto.room;

import com.desafio.room_reserve_api.model.Room;

import java.time.LocalDateTime;

public record RoomDto(
        Long id,
        String name,
        Integer capacity,
        Boolean active,
        String location,
        LocalDateTime creationDate,
        LocalDateTime updateDate
) {
    public RoomDto(Room room) {
        this(
                room.getId(),
                room.getName(),
                room.getCapacity(),
                room.getActive(),
                room.getLocation(),
                room.getCreationDate(),
                room.getUpdateDate()
        );
    }
}
