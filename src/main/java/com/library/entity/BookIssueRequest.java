package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_issue_requests")
@Data
@NoArgsConstructor
public class BookIssueRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "book_name")
    private String bookName;

    @Column(name = "book_number", unique = true)
    private String bookNumber;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "user_signature")
    private String userSignature;

    @Column(name = "librarian_signature")
    private String librarianSignature;

    @Column(name = "librarian_signature_url")
    private String librarianSignatureUrl;

    @Enumerated(EnumType.STRING)
    private IssueStatus status = IssueStatus.PENDING_ISSUE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public enum IssueStatus {
        PENDING_ISSUE, ISSUED, CANCELLED, REJECTED
    }
}
