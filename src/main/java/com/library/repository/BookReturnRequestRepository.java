package com.library.repository;

import com.library.entity.BookReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookReturnRequestRepository extends JpaRepository<BookReturnRequest, Long> {

    Optional<BookReturnRequest> findByIssueRequestId(Long issueRequestId);

    // Always use this for status checks — latest by creation time
    Optional<BookReturnRequest> findTopByIssueRequestIdOrderByCreatedAtDesc(Long issueRequestId);

    Optional<BookReturnRequest> findTopByIssueRequestIdAndStatusOrderByCreatedAtDesc(
            Long issueRequestId, BookReturnRequest.ReturnStatus status);

    List<BookReturnRequest> findByStatusOrderByCreatedAtDesc(BookReturnRequest.ReturnStatus status);

    List<BookReturnRequest> findByIssueRequestIdIn(List<Long> issueRequestIds);

    void deleteByIssueRequestId(Long issueRequestId);

    void deleteByIssueRequestIdIn(List<Long> issueRequestIds);
}