package com.desafio.room_reserve_api.e2e;

import com.desafio.room_reserve_api.dto.reservation.CreateReservationDto;
import com.desafio.room_reserve_api.dto.reservation.ReservationDto;
import com.desafio.room_reserve_api.dto.room.CreateRoomDto;
import com.desafio.room_reserve_api.dto.user.CreateUserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RoomDesactivationFlowTest {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1";
        // Instanciamos com a fábrica do Apache para garantir suporte a todos os verbos HTTP
        restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }

    @Test
    @DisplayName("Fluxo com Sala Inativada: Proteger novas reservas e manter histórico")
    void shouldExecuteRoomDeactivationFlow() {

        // Passo 1: Criar Usuário e Sala válidos
        restTemplate.postForEntity(baseUrl + "/users", new CreateUserDto("Usuário Padrão", "user@email.com", "senha123"), String.class);
        restTemplate.postForEntity(baseUrl + "/rooms", new CreateRoomDto("Sala Auditório", 50, "Térreo"), String.class);
        Long userId = 1L;
        Long roomId = 1L;

        // Passo 2: Verificar se um usuário cria uma reserva nessa sala
        LocalDateTime startFirst = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);
        CreateReservationDto firstReservation = new CreateReservationDto(
                "Palestra Anual", startFirst, startFirst.plusHours(2), userId, roomId
        );

        ResponseEntity<String> createResponse = restTemplate.postForEntity(baseUrl + "/reservations", firstReservation, String.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode(), "Falha ao criar a reserva inicial na sala ativa.");

        // Passo 3: Verificar se a sala fica inativa
        ResponseEntity<String> deactivateResponse = restTemplate.exchange(
                baseUrl + "/rooms/" + roomId, HttpMethod.DELETE, null, String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deactivateResponse.getStatusCode(), "Falha ao inativar a sala.");

        // Passo 4: Verificar se novas reservas nessa sala são bloqueadas
        LocalDateTime startSecond = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0);
        CreateReservationDto blockedReservation = new CreateReservationDto(
                "Reunião Proibida", startSecond, startSecond.plusHours(1), userId, roomId
        );

        // A API deve retornar uma exceção
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.postForEntity(baseUrl + "/reservations", blockedReservation, String.class);
        });

        // Verificamos se a API negou com um 400 Bad Request
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode(), "A API não bloqueou a criação de reserva em uma sala inativa!");

        // Passo 5: Verificar se reservas antigas permanecem registradas
        // Nós inativamos a sala, mas a primeira reserva não pode sumir do banco de dados!
        ResponseEntity<ReservationDto> oldReservationResponse = restTemplate.getForEntity(
                baseUrl + "/reservations/1", ReservationDto.class
        );

        assertEquals(HttpStatus.OK, oldReservationResponse.getStatusCode(), "A reserva antiga desapareceu após inativar a sala!");
        assertNotNull(oldReservationResponse.getBody());
        assertEquals("Palestra Anual", oldReservationResponse.getBody().purpose(), "Os dados da reserva antiga foram perdidos ou corrompidos!");
    }
}