package com.desafio.room_reserve_api.repository;

import com.desafio.room_reserve_api.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

}
