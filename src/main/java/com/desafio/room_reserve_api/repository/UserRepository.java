package com.desafio.room_reserve_api.repository;

import com.desafio.room_reserve_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    List<User> findAllByActiveTrue();
}
