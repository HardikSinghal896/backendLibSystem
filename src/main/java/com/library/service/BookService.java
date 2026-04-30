package com.library.service;

import com.library.dto.Dto;
import com.library.entity.BookIssueRequest;
import com.library.entity.BookReturnRequest;
import com.library.entity.User;
import com.library.repository.BookIssueRequestRepository;
import com.library.repository.BookReturnRequestRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookIssueRequestRepository issueRepo;
    private final BookReturnRequestRepository returnRepo;
    private final UserRepository userRepo;

    public BookIssueRequest requestIssue(Long userId, Dto.BookIssueDto dto) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate issuedDate = LocalDate.parse(dto.getIssuedDate());
        LocalDate returnDate = LocalDate.parse(dto.getReturnDate());
        if (returnDate.isBefore(issuedDate)) {
            throw new RuntimeException("Return date must be after issue date");
        }

        // Block only if THIS user already has a PENDING_ISSUE for the same bookNumber
        // Different users can request the same bookNumber independently
        issueRepo.findTopByUserIdAndBookNumberAndStatusOrderByCreatedAtDesc(
                        userId, dto.getBookNumber(), BookIssueRequest.IssueStatus.PENDING_ISSUE)
                .ifPresent(existing -> {
                    throw new RuntimeException("You already have a pending issue request for this book");
                });

        BookIssueRequest req = new BookIssueRequest();
        req.setUser(user);
        req.setBookName(dto.getBookName());
        req.setBookNumber(dto.getBookNumber());
        req.setAdmissionDate(dto.getAdmissionDate() != null ? LocalDate.parse(dto.getAdmissionDate()) : null);
        req.setIssuedDate(issuedDate);
        req.setReturnDate(returnDate);
        req.setUserSignature(dto.getUserSignature());
        req.setStatus(BookIssueRequest.IssueStatus.PENDING_ISSUE);
        return issueRepo.save(req);
    }
    public BookReturnRequest requestReturn(Long userId, Long issueRequestId) {
        BookIssueRequest issueReq = issueRepo.findById(issueRequestId)
                .orElseThrow(() -> new RuntimeException("Issue request not found"));

        if (!issueReq.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (issueReq.getStatus() != BookIssueRequest.IssueStatus.ISSUED) {
            throw new RuntimeException("Book is not in ISSUED state");
        }

        // Only block if the latest return request is still active
        returnRepo.findTopByIssueRequestIdOrderByCreatedAtDesc(issueRequestId).ifPresent(r -> {
            if (r.getStatus() == BookReturnRequest.ReturnStatus.PENDING_RETURN) {
                throw new RuntimeException("Return request already pending");
            }
            if (r.getStatus() == BookReturnRequest.ReturnStatus.RETURNED) {
                throw new RuntimeException("Book has already been returned");
            }
            // REJECTED or CANCELLED → allow re-raise
        });

        BookReturnRequest returnReq = new BookReturnRequest();
        returnReq.setIssueRequest(issueReq);
        returnReq.setRequestDate(LocalDate.now());
        returnReq.setStatus(BookReturnRequest.ReturnStatus.PENDING_RETURN);
        return returnRepo.save(returnReq);
    }

    public void cancelIssueRequest(Long userId, Long issueId) {
        BookIssueRequest req = issueRepo.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue request not found"));
        if (!req.getUser().getId().equals(userId)) throw new RuntimeException("Unauthorized");
        if (req.getStatus() != BookIssueRequest.IssueStatus.PENDING_ISSUE)
            throw new RuntimeException("Can only cancel a PENDING_ISSUE request");
        req.setStatus(BookIssueRequest.IssueStatus.CANCELLED);
        issueRepo.save(req);
    }

    public void cancelReturnRequest(Long userId, Long returnId) {
        BookReturnRequest req = returnRepo.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Return request not found"));
        if (!req.getIssueRequest().getUser().getId().equals(userId)) throw new RuntimeException("Unauthorized");
        if (req.getStatus() != BookReturnRequest.ReturnStatus.PENDING_RETURN)
            throw new RuntimeException("Can only cancel a PENDING_RETURN request");
        req.setStatus(BookReturnRequest.ReturnStatus.CANCELLED);
        returnRepo.save(req);
    }

    public List<Dto.BookStatusDto> getUserBooks(Long userId) {
        List<BookIssueRequest> issues = issueRepo.findByUserIdOrderByCreatedAtDesc(userId);
        return issues.stream()
                .map(this::toStatusDto)
                // Hide CANCELLED and RETURNED from user dashboard
                .filter(dto -> !dto.getStatus().equals("CANCELLED") && !dto.getStatus().equals("RETURNED"))
                .collect(Collectors.toList());
    }

    public List<Dto.BookStatusDto> getAllUserBooks(Long userId) {
        return issueRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toStatusDto).collect(Collectors.toList());
    }

    public Dto.BookStatusDto toStatusDto(BookIssueRequest issue) {
        Dto.BookStatusDto dto = new Dto.BookStatusDto();
        dto.setIssueId(issue.getId());
        dto.setBookName(issue.getBookName());
        dto.setBookNumber(issue.getBookNumber());
        dto.setAdmissionDate(issue.getAdmissionDate() != null ? issue.getAdmissionDate().toString() : null);
        dto.setIssuedDate(issue.getIssuedDate() != null ? issue.getIssuedDate().toString() : null);
        dto.setReturnDate(issue.getReturnDate() != null ? issue.getReturnDate().toString() : null);
        dto.setUserSignature(issue.getUserSignature());
        dto.setLibrarianSignature(issue.getLibrarianSignature());
        dto.setLibrarianSignatureUrl(issue.getLibrarianSignatureUrl());
        dto.setUserId(issue.getUser().getId());
        dto.setUserName(issue.getUser().getName());

        // Effective status: use latest return request only if it's active (PENDING_RETURN or RETURNED)
        Optional<BookReturnRequest> returnReq = returnRepo.findTopByIssueRequestIdOrderByCreatedAtDesc(issue.getId());
        if (returnReq.isPresent()) {
            BookReturnRequest r = returnReq.get();
            if (r.getStatus() == BookReturnRequest.ReturnStatus.PENDING_RETURN
                    || r.getStatus() == BookReturnRequest.ReturnStatus.RETURNED) {
                dto.setStatus(r.getStatus().name());
                dto.setReturnRequestId(r.getId());
                dto.setEvidenceImageUrl(r.getEvidenceImageUrl());
            } else {
                // REJECTED or CANCELLED — book is still with user, show issue status
                dto.setStatus(issue.getStatus().name());
            }
        } else {
            dto.setStatus(issue.getStatus().name());
        }

        return dto;
    }
}