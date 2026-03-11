package com.desafio.room_reserve_api.controller;

import com.desafio.room_reserve_api.dto.reservation.CreateReservationDto;
import com.desafio.room_reserve_api.dto.reservation.ReservationDto;
import com.desafio.room_reserve_api.dto.reservation.UpdateReservationDto;
import com.desafio.room_reserve_api.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ResponseEntity<List<ReservationDto>> listReservations() {
        List<ReservationDto> reservations = service.listReservations();
        return ResponseEntity.status(HttpStatus.OK).body(reservations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto> listReservationById(@PathVariable Long id) {
        ReservationDto reservation = service.listReservationById(id);
        return ResponseEntity.status(HttpStatus.OK).body(reservation);
    }

    @PostMapping
    public ResponseEntity<String> createReservation(@RequestBody @Valid CreateReservationDto dto) {
        service.createReservation(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Reserva criada com sucesso!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateReservation(
            @PathVariable Long id,
            @RequestBody @Valid UpdateReservationDto dto
    ) {
        service.updateReservation(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body("Reserva atualizada com sucesso!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReservation(@PathVariable Long id) {
        service.deleteReservation(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/completed")
    public ResponseEntity<String> completedReservation(@PathVariable Long id) {
        service.completedReservation(id);
        return ResponseEntity.status(HttpStatus.OK).body("Reserva atualizada para finalizada!");
    }
}
