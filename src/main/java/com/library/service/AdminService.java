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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final BookIssueRequestRepository issueRepo;
    private final BookReturnRequestRepository returnRepo;
    private final UserRepository userRepo;
    private final BookService bookService;

    // ----- Issue Approvals -----

    public List<Dto.BookStatusDto> getPendingIssues() {
        return issueRepo.findByStatusOrderByCreatedAtDesc(BookIssueRequest.IssueStatus.PENDING_ISSUE)
                .stream().map(bookService::toStatusDto).collect(Collectors.toList());
    }

    public Dto.BookStatusDto approveIssue(Long id, String librarianSignature, String signatureUrl) {
        BookIssueRequest req = issueRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Request has been removed"));
        if (req.getStatus() != BookIssueRequest.IssueStatus.PENDING_ISSUE)
            throw new RuntimeException("Invalid request state");
        req.setStatus(BookIssueRequest.IssueStatus.ISSUED);
        req.setLibrarianSignature(librarianSignature);
        req.setLibrarianSignatureUrl(signatureUrl);
        return bookService.toStatusDto(issueRepo.save(req));
    }

    public Dto.BookStatusDto rejectIssue(Long id) {
        BookIssueRequest req = issueRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Request has been removed"));
        if (req.getStatus() != BookIssueRequest.IssueStatus.PENDING_ISSUE)
            throw new RuntimeException("Invalid request state");
        req.setStatus(BookIssueRequest.IssueStatus.REJECTED);
        return bookService.toStatusDto(issueRepo.save(req));
    }

    // ----- Return Approvals -----

    public List<Dto.BookStatusDto> getPendingReturns() {
        return returnRepo.findByStatusOrderByCreatedAtDesc(BookReturnRequest.ReturnStatus.PENDING_RETURN)
                .stream()
                // Only include if this row is still the latest return request for its issue
                // This prevents stale PENDING_RETURN rows from old cycles showing up
                .filter(r -> returnRepo
                        .findTopByIssueRequestIdOrderByCreatedAtDesc(r.getIssueRequest().getId())
                        .map(latest -> latest.getId().equals(r.getId()))
                        .orElse(false))
                .map(r -> bookService.toStatusDto(r.getIssueRequest()))
                .collect(Collectors.toList());
    }

    public Dto.BookStatusDto approveReturn(Long returnRequestId, String evidenceImageUrl) {
        BookReturnRequest req = returnRepo.findById(returnRequestId)
                .orElseThrow(() -> new RuntimeException("Request has been removed"));
        if (req.getStatus() != BookReturnRequest.ReturnStatus.PENDING_RETURN)
            throw new RuntimeException("Invalid request state");
        req.setStatus(BookReturnRequest.ReturnStatus.RETURNED);
        req.setEvidenceImageUrl(evidenceImageUrl);
        returnRepo.save(req);
        return bookService.toStatusDto(req.getIssueRequest());
    }

    public Dto.BookStatusDto rejectReturn(Long returnRequestId) {
        BookReturnRequest req = returnRepo.findById(returnRequestId)
                .orElseThrow(() -> new RuntimeException("Request has been removed"));
        if (req.getStatus() != BookReturnRequest.ReturnStatus.PENDING_RETURN)
            throw new RuntimeException("Invalid request state");
        req.setStatus(BookReturnRequest.ReturnStatus.REJECTED);
        returnRepo.save(req);
        return bookService.toStatusDto(req.getIssueRequest());
    }

    // ----- Active Books -----

    public List<Dto.ActiveUserDto> getActiveBooksByUser() {
        List<BookIssueRequest> activeBooks = issueRepo.findActiveBooks();

        Map<Long, List<BookIssueRequest>> byUser = activeBooks.stream()
                .collect(Collectors.groupingBy(b -> b.getUser().getId()));

        return byUser.entrySet().stream().map(e -> {
            Dto.ActiveUserDto dto = new Dto.ActiveUserDto();
            dto.setUserId(e.getKey());
            dto.setName(e.getValue().get(0).getUser().getName());
            dto.setActiveBooksCount(e.getValue().size());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<Dto.BookStatusDto> getActiveBooksForUser(Long userId) {
        return issueRepo.findActiveBooksByUserId(userId)
                .stream().map(bookService::toStatusDto).collect(Collectors.toList());
    }

    // ----- User Management -----

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public List<User> getPendingUsers() {
        return userRepo.findByStatus("PENDING");
    }

    public User approveUser(Long id) {
        User user = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus("APPROVED");
        return userRepo.save(user);
    }

    public User rejectUser(Long id) {
        User user = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus("REJECTED");
        return userRepo.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        List<BookIssueRequest> issues = issueRepo.findByUserIdOrderByCreatedAtDesc(userId);
        List<Long> issueIds = issues.stream().map(BookIssueRequest::getId).collect(Collectors.toList());
        if (!issueIds.isEmpty()) {
            returnRepo.deleteByIssueRequestIdIn(issueIds);
            issueRepo.deleteByUserId(userId);
        }
        userRepo.deleteById(userId);
    }
}