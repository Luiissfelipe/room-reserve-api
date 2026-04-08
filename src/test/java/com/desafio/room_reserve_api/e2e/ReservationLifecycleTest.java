package com.desafio.room_reserve_api.e2e;

import com.desafio.room_reserve_api.dto.reservation.CreateReservationDto;
import com.desafio.room_reserve_api.dto.reservation.ReservationDto;
import com.desafio.room_reserve_api.dto.reservation.UpdateReservationDto;
import com.desafio.room_reserve_api.dto.room.CreateRoomDto;
import com.desafio.room_reserve_api.dto.user.CreateUserDto;
import com.desafio.room_reserve_api.model.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
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
class ReservationLifecycleTest {

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
    @DisplayName("Ciclo de Vida: Criar, Provar Bloqueio de Tempo, Atualizar e Finalizar")
    void shouldExecuteFullReservationLifecycle() throws InterruptedException {
        // Passo 1: Criar usuário e sala
        restTemplate.postForEntity(baseUrl + "/users", new CreateUserDto("Senhor do Tempo", "tempo@email.com", "123"), String.class);
        restTemplate.postForEntity(baseUrl + "/rooms", new CreateRoomDto("Sala Cronos", 8, "Andar 3"), String.class);

        // Passo 2: Criar a reserva (Duração relâmpago de 1 segundo!)
        LocalDateTime start = LocalDateTime.now().plusSeconds(1);
        LocalDateTime end = start.plusSeconds(1);

        CreateReservationDto createDto = new CreateReservationDto(
                "Pauta Relâmpago", start, end, 1L, 1L
        );

        ResponseEntity<String> createResponse = restTemplate.postForEntity(baseUrl + "/reservations", createDto, String.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode(), "Falha ao criar reserva inicial");

        // Passo 3: Provar que a API bloqueia finalização antecipada
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(baseUrl + "/reservations/1/completed", HttpMethod.PATCH, null, String.class);
        });

        // Se a sua API defendeu bem, ela deve retornar um 400 Bad Request
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode(), "A API não bloqueou a finalização antecipada!");

        // Passo 4: Fazer Outras Mudanças (PUT)
        UpdateReservationDto updateDto = new UpdateReservationDto("Pauta Relâmpago Atualizada", start, end);
        ResponseEntity<String> updateResponse = restTemplate.exchange(
                baseUrl + "/reservations/1", HttpMethod.PUT, new HttpEntity<>(updateDto), String.class
        );
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode(), "Falha ao atualizar a reserva");

        // Passo 5: Esperar a reserva acabar na vida real
        System.out.println("Aguardando 3 segundos para a reserva expirar naturalmente no banco de dados...");
        Thread.sleep(3000);

        // Passo 6: Mudar para Completo
        ResponseEntity<String> completeResponse = restTemplate.exchange(
                baseUrl + "/reservations/1/completed", HttpMethod.PATCH, null, String.class
        );
        assertEquals(HttpStatus.OK, completeResponse.getStatusCode(), "Falha ao finalizar após o tempo acabar!");

        // Passo 7: Verificação Final
        ResponseEntity<ReservationDto> getFinal = restTemplate.getForEntity(baseUrl + "/reservations/1", ReservationDto.class);
        ReservationDto finalReservation = getFinal.getBody();

        assertNotNull(finalReservation);
        assertEquals("Pauta Relâmpago Atualizada", finalReservation.purpose());
        assertEquals(ReservationStatus.COMPLETED, finalReservation.reservationStatus(), "O status não mudou para COMPLETED!");
    }
}