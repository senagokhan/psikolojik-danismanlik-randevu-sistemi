package com.psikolojikdanismanlik.randevusistemi.entity;

import com.psikolojikdanismanlik.randevusistemi.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "therapist_id")
    private Therapist therapist;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    private Note note;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    private Feedback feedback;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDate createdDate;

    @Column(name = "requested_reschedule_time")
    private LocalDateTime requestedRescheduleTime;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.createdDate = LocalDate.now();
        if (this.status == null) {
            this.status = Status.PENDING_APPROVAL;
        }
    }
}
