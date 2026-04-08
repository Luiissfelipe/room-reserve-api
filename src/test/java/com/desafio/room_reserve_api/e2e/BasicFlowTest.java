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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BasicFlowTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    @DisplayName("Fluxo completo básico: Criar Usuário, Sala, Reserva e checar Listagem/Status")
    void shouldExecuteBasicReservationFlow() {
        // Caminho base da API
        String baseUrl = "http://localhost:" + port + "/api/v1";

        // Passo 1: Verificar se é possível criar um usuário
        CreateUserDto newUser = new CreateUserDto("Ana Silva", "ana.silva@email.com", "senhaForte123");
        ResponseEntity<String> userResponse = restTemplate.postForEntity(baseUrl + "/users", newUser, String.class);

        assertEquals(HttpStatus.CREATED, userResponse.getStatusCode(), "Falha ao criar usuário");
        Long userId = 1L; // O H2 em memória sempre começa do ID 1

        // Passo 2: Verificar se é possível criar uma sala
        CreateRoomDto newRoom = new CreateRoomDto("Sala de Conferência", 20, "Andar Térreo");
        ResponseEntity<String> roomResponse = restTemplate.postForEntity(baseUrl + "/rooms", newRoom, String.class);

        assertEquals(HttpStatus.CREATED, roomResponse.getStatusCode(), "Falha ao criar sala");
        Long roomId = 1L; // A primeira sala também recebe ID 1

        // Passo 3: Verificar se o usuário consegue reservar a sala
        LocalDateTime start = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0).withSecond(0);
        LocalDateTime end = start.plusHours(2);

        CreateReservationDto newReservation = new CreateReservationDto(
                "Reunião de Planejamento", start, end, userId, roomId
        );

        // Chamando o POST no seu ReservationController (/reservations)
        ResponseEntity<String> resResponse = restTemplate.postForEntity(baseUrl + "/reservations", newReservation, String.class);
        assertEquals(HttpStatus.CREATED, resResponse.getStatusCode(), "Falha ao criar reserva");

        // Passo 4 e 5: Verificar se a reserva aparece na listagem e status

        // Fazendo o GET na rota de listagem e mapeando para a CustomPage
        ResponseEntity<CustomPage<ReservationDto>> getResponse = restTemplate.exchange(
                baseUrl + "/reservations/list",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<CustomPage<ReservationDto>>() {}
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode(), "Falha ao buscar listagem de reservas");

        // Pegando a lista de DTOs de dentro da paginação
        List<ReservationDto> reservations = getResponse.getBody().content();

        assertNotNull(reservations);
        assertFalse(reservations.isEmpty(), "A lista de reservas não deveria estar vazia");

        // Extraindo a reserva que foi criada
        ReservationDto savedReservation = reservations.get(0);

        // Validando os dados da reserva salva
        assertEquals("Reunião de Planejamento", savedReservation.purpose());
        assertEquals("Ana Silva", savedReservation.userName());
        assertEquals("Sala de Conferência", savedReservation.roomName());
        assertEquals(ReservationStatus.CONFIRMED, savedReservation.reservationStatus(), "Status da reserva deveria ser CONFIRMED");
    }

    // RECORD AUXILIAR PARA LER A PAGINAÇÃO DO SPRING
    @JsonIgnoreProperties(ignoreUnknown = true)
    record CustomPage<T>(List<T> content) {}
}