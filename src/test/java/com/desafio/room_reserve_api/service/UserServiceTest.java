package com.desafio.room_reserve_api.service;

import com.desafio.room_reserve_api.dto.user.CreateUserDto;
import com.desafio.room_reserve_api.dto.user.UpdateUserDto;
import com.desafio.room_reserve_api.dto.user.UserDto;
import com.desafio.room_reserve_api.exception.ValidationException;
import com.desafio.room_reserve_api.model.Role;
import com.desafio.room_reserve_api.model.User;
import com.desafio.room_reserve_api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User activeUser;
    private CreateUserDto createDto;
    private UpdateUserDto updateDto;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setName("Luís Felipe");
        activeUser.setEmail("luis@email.com");
        activeUser.setPassword("senha123");
        activeUser.setRole(Role.USER);
        activeUser.setActive(true);

        createDto = new CreateUserDto("Luís Felipe", "luis@email.com", "senha123");
        updateDto = new UpdateUserDto("Luís Atualizado", "luis.novo@email.com", "novaSenha123");
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso quando o e-mail não existir")
    void shouldCreateUserSuccessfully() {
        when(userRepository.existsByEmail(createDto.email())).thenReturn(false);

        userService.createUser(createDto);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar ValidationException ao tentar criar usuário com e-mail já cadastrado")
    void shouldThrowExceptionWhenCreatingUserWithExistingEmail() {
        when(userRepository.existsByEmail(createDto.email())).thenReturn(true);

        assertThrows(ValidationException.class, () -> userService.createUser(createDto));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar apenas usuários ativos")
    void shouldListAllActiveUsers() {
        when(userRepository.findAllByActiveTrue()).thenReturn(List.of(activeUser));

        List<UserDto> result = userService.listUsers();

        assertEquals(1, result.size());
        assertEquals("Luís Felipe", result.getFirst().name());
        verify(userRepository, times(1)).findAllByActiveTrue();
    }

    @Test
    @DisplayName("Deve retornar UserDto ao buscar por ID válido")
    void shouldReturnUserByIdSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        UserDto result = userService.listUserById(1L);

        assertNotNull(result);
        assertEquals(activeUser.getEmail(), result.email());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao buscar por ID inexistente")
    void shouldThrowExceptionWhenUserNotFoundById() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.listUserById(1L));
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso")
    void shouldUpdateUserSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        userService.updateUser(1L, updateDto);

        verify(userRepository, times(1)).save(activeUser);
        assertEquals("Luís Atualizado", activeUser.getName());
        assertEquals("luis.novo@email.com", activeUser.getEmail());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao atualizar usuário inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(1L, updateDto));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve inativar o usuário com sucesso (Soft Delete)")
    void shouldDeleteUserSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        userService.deleteUser(1L);

        assertFalse(activeUser.getActive());
        verify(userRepository, times(1)).save(activeUser);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao deletar usuário inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(1L));

        verify(userRepository, never()).save(any());
    }
}