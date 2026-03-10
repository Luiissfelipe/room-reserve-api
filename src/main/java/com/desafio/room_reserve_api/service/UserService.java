package com.desafio.room_reserve_api.service;

import com.desafio.room_reserve_api.dto.user.CreateUserDto;
import com.desafio.room_reserve_api.dto.user.UpdateUserDto;
import com.desafio.room_reserve_api.dto.user.UserDto;
import com.desafio.room_reserve_api.exception.ValidationException;
import com.desafio.room_reserve_api.model.User;
import com.desafio.room_reserve_api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void createUser(CreateUserDto dto) {
        boolean isUserExists = userRepository.existsByEmail(dto.email());

        if (isUserExists) {
            throw new ValidationException("Usuário já cadastrado com esse email!");
        }

        userRepository.save(new User(dto));
    }

    public List<UserDto> listUsers() {
        return userRepository
                .findAllByActiveTrue()
                .stream()
                .map(UserDto::new)
                .toList();
    }

    public UserDto listUserById(Long id) {
        return userRepository.findById(id)
                .map(UserDto::new)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }

    @Transactional
    public void updateUser(Long id, UpdateUserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado!"));
        user.updateUser(dto);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado!"));
        user.disableUser();
    }
}
