package com.desafio.room_reserve_api.controller;

import com.desafio.room_reserve_api.dto.user.CreateUserDto;
import com.desafio.room_reserve_api.dto.user.UpdateUserDto;
import com.desafio.room_reserve_api.dto.user.UserDto;
import com.desafio.room_reserve_api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody @Valid CreateUserDto dto) {
        service.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário criado com sucesso!");
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> listUsers() {
        List<UserDto> users = service.listUsers();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> listUserById(@PathVariable Long id) {
        UserDto user = service.listUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody @Valid UpdateUserDto dto) {
        service.updateUser(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body("Usuário atualizado com sucesso!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
