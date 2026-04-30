package com.library.repository;

import com.library.entity.BookIssueRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookIssueRequestRepository extends JpaRepository<BookIssueRequest, Long> {

    List<BookIssueRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<BookIssueRequest> findByStatusOrderByCreatedAtDesc(BookIssueRequest.IssueStatus status);

    // Per-user duplicate check: same user + same bookNumber + PENDING_ISSUE
    Optional<BookIssueRequest> findTopByUserIdAndBookNumberAndStatusOrderByCreatedAtDesc(
            Long userId, String bookNumber, BookIssueRequest.IssueStatus status);

    @Query("SELECT b FROM BookIssueRequest b WHERE b.status = 'ISSUED' OR " +
            "EXISTS (SELECT r FROM BookReturnRequest r WHERE r.issueRequest = b AND r.status = 'PENDING_RETURN')")
    List<BookIssueRequest> findActiveBooks();

    @Query("SELECT b FROM BookIssueRequest b WHERE b.user.id = :userId AND " +
            "(b.status = 'ISSUED' OR EXISTS (SELECT r FROM BookReturnRequest r WHERE r.issueRequest = b AND r.status = 'PENDING_RETURN'))")
    List<BookIssueRequest> findActiveBooksByUserId(Long userId);

    void deleteByUserId(Long userId);
}