package com.desafio.room_reserve_api.e2e;

import com.desafio.room_reserve_api.dto.reservation.CreateReservationDto;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CancellationErrorFlowsTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();
    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1";
    }

    @Test
    @DisplayName("1. Deve retornar 404 Not Found ao tentar cancelar uma reserva que não existe")
    void shouldReturn404WhenCancelingNonExistentReservation() {
        // Tentamos mandar um DELETE para o ID 999 (que não existe no banco zerado)
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(
                    baseUrl + "/reservations/999",
                    HttpMethod.DELETE,
                    null,
                    String.class
            );
        });

        // Verifica se a API respondeu corretamente com 404
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode(), "A API deveria ter retornado 404 Not Found!");
    }

    @Test
    @DisplayName("2. Deve retornar 400 Bad Request ao tentar cancelar uma reserva DUAS VEZES")
    void shouldReturn400WhenCancelingAlreadyCancelledReservation() {
        // Passo 1: Criar Usuário, Sala e Reserva válidos
        restTemplate.postForEntity(baseUrl + "/users", new CreateUserDto("Pedro", "pedro@email.com", "123"), String.class);
        restTemplate.postForEntity(baseUrl + "/rooms", new CreateRoomDto("Sala de Reunião", 5, "Térreo"), String.class);

        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0);
        CreateReservationDto newReservation = new CreateReservationDto(
                "Reunião de Vendas", start, start.plusHours(1), 1L, 1L
        );

        restTemplate.postForEntity(baseUrl + "/reservations", newReservation, String.class);

        // Passo 2: Cancelar a reserva pela PRIMEIRA vez (Sucesso)
        ResponseEntity<String> firstDelete = restTemplate.exchange(
                baseUrl + "/reservations/1",
                HttpMethod.DELETE,
                null,
                String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, firstDelete.getStatusCode(), "O primeiro cancelamento falhou!");

        // Passo 3: Tentar cancelar a reserva pela SEGUNDA vez (Falha Esperada)
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(
                    baseUrl + "/reservations/1",
                    HttpMethod.DELETE,
                    null,
                    String.class
            );
        });

        // Verifica se a API retorna um 400 Bad Request
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode(), "A API deixou cancelar uma reserva que já estava cancelada!");
    }
}