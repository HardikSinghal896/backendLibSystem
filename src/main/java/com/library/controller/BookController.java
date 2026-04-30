package com.library.controller;

import com.library.dto.Dto;
import com.library.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping("/issue")
    public ResponseEntity<?> requestIssue(HttpServletRequest request,
                                          @Valid @RequestBody Dto.BookIssueDto dto) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(bookService.requestIssue(userId, dto));
    }

    @PostMapping("/return")
    public ResponseEntity<?> requestReturn(HttpServletRequest request,
                                           @RequestBody Dto.BookReturnDto dto) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(bookService.requestReturn(userId, dto.getIssueRequestId()));
    }

    @PostMapping("/issue/{id}/cancel")
    public ResponseEntity<?> cancelIssue(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        bookService.cancelIssueRequest(userId, id);
        return ResponseEntity.ok(java.util.Map.of("message", "Issue request cancelled"));
    }

    @PostMapping("/return/{id}/cancel")
    public ResponseEntity<?> cancelReturn(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        bookService.cancelReturnRequest(userId, id);
        return ResponseEntity.ok(java.util.Map.of("message", "Return request cancelled"));
    }
}