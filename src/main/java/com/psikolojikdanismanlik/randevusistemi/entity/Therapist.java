package com.psikolojikdanismanlik.randevusistemi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "therapists")
public class Therapist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false)
    private String experience;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String about;

    @OneToMany(mappedBy = "therapist", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "therapist", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Availability> availabilities = new ArrayList<>();

}
