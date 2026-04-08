package com.desafio.room_reserve_api.e2e;

import com.desafio.room_reserve_api.dto.reservation.CreateReservationDto;
import com.desafio.room_reserve_api.dto.reservation.ReservationDto;
import com.desafio.room_reserve_api.dto.room.CreateRoomDto;
import com.desafio.room_reserve_api.dto.user.CreateUserDto;
import com.desafio.room_reserve_api.model.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CancellationFlowTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    @DisplayName("Fluxo de Cancelamento: Criar, Cancelar, Sumir da Lista e Checar Status por ID")
    void shouldExecuteCancellationFlow() {
        // Caminho base da API
        String baseUrl = "http://localhost:" + port + "/api/v1";

        // Passo 1: Criar Usuário e Sala
        CreateUserDto newUser = new CreateUserDto("João Cancelador", "joao.cancela@email.com", "senha123");
        restTemplate.postForEntity(baseUrl + "/users", newUser, String.class);
        Long userId = 1L;

        CreateRoomDto newRoom = new CreateRoomDto("Sala de Testes", 10, "Subsolo");
        restTemplate.postForEntity(baseUrl + "/rooms", newRoom, String.class);
        Long roomId = 1L;

        // Passo 2: Verificar se o usuário cria a reserva
        LocalDateTime start = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0).withSecond(0);
        LocalDateTime end = start.plusHours(1);

        CreateReservationDto newReservation = new CreateReservationDto(
                "Reunião que será cancelada", start, end, userId, roomId
        );

        ResponseEntity<String> createResponse = restTemplate.postForEntity(baseUrl + "/reservations", newReservation, String.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode(), "Falha ao criar a reserva");

        // Passo 3: Verificar se o usuário consegue cancelar a reserva (DELETE)
        Long reservationId = 1L;

        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                baseUrl + "/reservations/" + reservationId,
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode(), "Falha ao cancelar a reserva");

        // Passo 4: Verificar se a reserva SUMIU da listagem geral
        ResponseEntity<CustomPage<ReservationDto>> listResponse = restTemplate.exchange(
                baseUrl + "/reservations/list",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<CustomPage<ReservationDto>>() {}
        );

        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        List<ReservationDto> reservations = listResponse.getBody().content();

        // A regra de negócio manda ocultar as canceladas. Então a lista tem que estar VAZIA!
        assertTrue(reservations.isEmpty(), "A reserva cancelada não deveria aparecer na listagem!");

        // Passo 5: Buscar direto pelo ID para provar que sofreu Soft Delete
        ResponseEntity<ReservationDto> getByIdResponse = restTemplate.getForEntity(
                baseUrl + "/reservations/" + reservationId,
                ReservationDto.class
        );

        assertEquals(HttpStatus.OK, getByIdResponse.getStatusCode(), "A reserva foi apagada fisicamente do banco!");

        ReservationDto cancelledReservation = getByIdResponse.getBody();
        assertNotNull(cancelledReservation);

        // O status foi atualizado para CANCELLED?
        assertEquals(ReservationStatus.CANCELLED, cancelledReservation.reservationStatus(), "O status não foi atualizado para CANCELLED!");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CustomPage<T>(List<T> content) {}
}