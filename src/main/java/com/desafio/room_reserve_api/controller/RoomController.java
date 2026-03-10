package com.desafio.room_reserve_api.controller;

import com.desafio.room_reserve_api.dto.room.CreateRoomDto;
import com.desafio.room_reserve_api.dto.room.RoomDto;
import com.desafio.room_reserve_api.dto.room.UpdateRoomDto;
import com.desafio.room_reserve_api.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService service;

    public RoomController(RoomService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> listRooms() {
        List<RoomDto> rooms = service.listRooms();
        return ResponseEntity.status(HttpStatus.OK).body(rooms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDto> listRoomById(@PathVariable Long id) {
        RoomDto room = service.listRoomById(id);
        return ResponseEntity.status(HttpStatus.OK).body(room);
    }

    @PostMapping
    public ResponseEntity<String> createRoom(@RequestBody @Valid CreateRoomDto dto) {
        service.createRoom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Sala criada com sucesso!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateRoom(@PathVariable Long id, @RequestBody @Valid UpdateRoomDto dto) {
        service.updateRoom(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body("Sala atualizada com sucesso!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRoom(@PathVariable Long id) {
        service.deleteRoom(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
