package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_return_requests")
@Data
@NoArgsConstructor
public class BookReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "issue_request_id")
    private BookIssueRequest issueRequest;

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Column(name = "evidence_image_url")
    private String evidenceImageUrl;

    @Enumerated(EnumType.STRING)
    private ReturnStatus status = ReturnStatus.PENDING_RETURN;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public enum ReturnStatus {
        PENDING_RETURN, RETURNED, CANCELLED, REJECTED
    }
}
