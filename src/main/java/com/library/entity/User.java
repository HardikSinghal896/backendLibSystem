package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String mobile;

    @Column(unique = true)
    private String email;

    @Column(name = "aadhar_number")
    private String aadharNumber;

    @Column(name = "aadhar_pdf")
    private String aadharPdf;

    private String password;

    @Column(name = "status")
    private String status = "PENDING";

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}