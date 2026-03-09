package com.desafio.room_reserve_api.repository;

import com.desafio.room_reserve_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
