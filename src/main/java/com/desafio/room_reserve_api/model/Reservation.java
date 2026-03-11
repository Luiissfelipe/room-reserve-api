package com.desafio.room_reserve_api.model;

import com.desafio.room_reserve_api.dto.reservation.CreateReservationDto;
import com.desafio.room_reserve_api.dto.reservation.UpdateReservationDto;
import com.desafio.room_reserve_api.exception.ValidationException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    private String purpose;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false)
    private ReservationStatus reservationStatus;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @CreatedDate
    @Column(name = "creation_date", updatable = false)
    private LocalDateTime creationDate;

    @LastModifiedDate
    @Column(name = "update_date")
    private LocalDateTime updateDate;

    public Reservation(CreateReservationDto dto, User user, Room room) {
        this.purpose = dto.purpose();
        this.startDate = dto.startDate();
        this.endDate = dto.endDate();
        if (!isPeriodValid()) {
            throw new ValidationException("A data final deve ser posterior à data de inicio!");
        }
        this.reservationStatus = ReservationStatus.CONFIRMED;
        this.user = user;
        this.room = room;
    }

    public void updateReservation(UpdateReservationDto dto) {
        this.purpose = dto.purpose();
        this.startDate = dto.startDate();
        this.endDate = dto.endDate();
        if (!isPeriodValid()) {
            throw new ValidationException("A data final deve ser posterior à data de inicio!");
        }
    }

    private boolean isPeriodValid() {
        return this.startDate.isBefore(this.endDate);
    }

    public void cancel() {
        if (this.reservationStatus == ReservationStatus.CANCELLED) {
            return;
        }

        if (LocalDateTime.now().isAfter(this.startDate) || LocalDateTime.now().isEqual(this.startDate)) {
            throw new ValidationException("Não é possível cancelar uma reserva que já iniciou ou já passou.");
        }

        this.reservationStatus = ReservationStatus.CANCELLED;
    }

    public void completed() {
        if (this.reservationStatus == ReservationStatus.COMPLETED) {
            return;
        }

        if (LocalDateTime.now().isBefore(this.endDate)) {
            throw new ValidationException("Não é possível completar uma reserva que ainda não chegou na data final.");
        }

        this.reservationStatus = ReservationStatus.COMPLETED;
    }
}