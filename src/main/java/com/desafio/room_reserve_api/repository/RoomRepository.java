package com.desafio.room_reserve_api.repository;

import com.desafio.room_reserve_api.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {

}
