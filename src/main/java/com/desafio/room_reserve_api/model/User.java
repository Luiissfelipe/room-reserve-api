package com.desafio.room_reserve_api.model;

import com.desafio.room_reserve_api.dto.user.CreateUserDto;
import com.desafio.room_reserve_api.dto.user.UpdateUserDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    @NotNull
    private Boolean active;

    @CreatedDate
    @Column(name = "creation_date", updatable = false)
    private LocalDateTime creationDate;

    @LastModifiedDate
    @Column(name = "update_date")
    private LocalDateTime updateDate;

    public User(CreateUserDto dto) {
        this.name = dto.name();
        this.email = dto.email();
        this.password = dto.password();
        this.role = Role.USER;
        this.active = true;
    }

    public void updateUser(UpdateUserDto dto) {
        this.name = dto.name();
        this.email = dto.email();
        this.password = dto.password();
    }

    public void disableUser() {
        this.active = false;
    }
}