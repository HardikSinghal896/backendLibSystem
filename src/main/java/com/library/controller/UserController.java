package com.library.controller;

import com.library.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final BookService bookService;

    @GetMapping("/me/books")
    public ResponseEntity<?> getMyBooks(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(bookService.getUserBooks(userId));
    }

    @GetMapping("/me/books/history")
    public ResponseEntity<?> getMyBooksHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(bookService.getAllUserBooks(userId));
    }
}