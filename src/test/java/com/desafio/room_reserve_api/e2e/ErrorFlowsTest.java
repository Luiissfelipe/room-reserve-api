package com.desafio.room_reserve_api.e2e;

import com.desafio.room_reserve_api.dto.reservation.CreateReservationDto;
import com.desafio.room_reserve_api.dto.room.CreateRoomDto;
import com.desafio.room_reserve_api.dto.user.CreateUserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
// Essa anotação limpa o banco de dados antes de CADA teste
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ErrorFlowsTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();
    private String baseUrl;

    @BeforeEach
    void setUp() {
        // Configuramos a URL base uma vez só para reaproveitar
        baseUrl = "http://localhost:" + port + "/api/v1";
    }

    // CENÁRIOS DE ERRO: USUÁRIO
    @Test
    @DisplayName("1. Deve retornar 400 Bad Request ao criar usuário com e-mail duplicado")
    void testDuplicateUserEmail() {
        CreateUserDto user = new CreateUserDto("João", "joao@email.com", "senha123");

        // Criamos o usuário pela primeira vez (Sucesso)
        restTemplate.postForEntity(baseUrl + "/users", user, String.class);

        // Tentamos criar de novo com o mesmo e-mail e capturamos a explosão!
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.postForEntity(baseUrl + "/users", user, String.class);
        });

        // Verificamos se a API bloqueou corretamente com o status 400
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    @DisplayName("2. Deve retornar 404 Not Found ao buscar um usuário que não existe")
    void testGetUserNotFound() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            // Buscando o ID 999 que não existe
            restTemplate.getForEntity(baseUrl + "/users/999", String.class);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // CENÁRIOS DE ERRO: SALA
    @Test
    @DisplayName("3. Deve retornar 400 Bad Request ao criar sala com nome duplicado")
    void testDuplicateRoomName() {
        CreateRoomDto room = new CreateRoomDto("Sala B", 10, "Térreo");
        restTemplate.postForEntity(baseUrl + "/rooms", room, String.class);

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.postForEntity(baseUrl + "/rooms", room, String.class);
        });

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // CENÁRIOS DE ERRO: RESERVAS
    @Test
    @DisplayName("4. Deve retornar 404 Not Found ao tentar reservar para um usuário inexistente")
    void testReservationUserNotFound() {
        // Criamos uma sala válida
        CreateRoomDto room = new CreateRoomDto("Sala C", 5, "1º Andar");
        restTemplate.postForEntity(baseUrl + "/rooms", room, String.class);

        // Tentamos reservar passando o ID de usuário 999
        CreateReservationDto res = new CreateReservationDto(
                "Reunião", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1),
                999L, 1L
        );

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.postForEntity(baseUrl + "/reservations", res, String.class);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("5. Deve retornar 404 Not Found ao tentar reservar uma sala inexistente")
    void testReservationRoomNotFound() {
        // Criamos um usuário válido
        CreateUserDto user = new CreateUserDto("Maria", "maria@email.com", "senha123");
        restTemplate.postForEntity(baseUrl + "/users", user, String.class);

        // Tentamos reservar passando o ID de sala 999
        CreateReservationDto res = new CreateReservationDto(
                "Reunião", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1),
                1L, 999L
        );

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.postForEntity(baseUrl + "/reservations", res, String.class);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("6. Deve retornar 400 Bad Request ao criar reserva com conflito de horário")
    void testReservationTimeConflict() {
        // 1. Criar dependências
        restTemplate.postForEntity(baseUrl + "/users", new CreateUserDto("Carlos", "carlos@email.com", "123"), String.class);
        restTemplate.postForEntity(baseUrl + "/rooms", new CreateRoomDto("Sala TI", 5, "Andar 2"), String.class);

        // 2. Definir o mesmo horário para as duas reservas
        LocalDateTime start = LocalDateTime.now().plusDays(5).withHour(14).withMinute(0);
        LocalDateTime end = start.plusHours(2);

        // 3. Cadastrar a primeira reserva (Sucesso)
        CreateReservationDto res1 = new CreateReservationDto("Reunião 1", start, end, 1L, 1L);
        restTemplate.postForEntity(baseUrl + "/reservations", res1, String.class);

        // 4. Cadastrar a segunda reserva NO MESMO HORÁRIO E MESMA SALA
        CreateReservationDto res2 = new CreateReservationDto("Reunião Conflitante", start, end, 1L, 1L);

        // 5. Verificar se a API não criou duas reservas no mesmo horário
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.postForEntity(baseUrl + "/reservations", res2, String.class);
        });

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}