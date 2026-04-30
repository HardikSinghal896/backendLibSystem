package com.library.controller;

import com.library.dto.Dto;
import com.library.service.AdminService;
import com.library.service.CloudinaryService;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final CloudinaryService cloudinaryService;

    // ----- Active Books -----

    @GetMapping("/books/active/users")
    public ResponseEntity<?> getActiveBookUsers() {
        return ResponseEntity.ok(adminService.getActiveBooksByUser());
    }

    @GetMapping("/books/active/users/{userId}")
    public ResponseEntity<?> getActiveBooksForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getActiveBooksForUser(userId));
    }

    // ----- Issue Approvals -----

    @GetMapping("/approvals/issues")
    public ResponseEntity<?> getPendingIssues() {
        return ResponseEntity.ok(adminService.getPendingIssues());
    }

    @PostMapping(value = "/approvals/issues/{id}/approve", consumes = "multipart/form-data")
    public ResponseEntity<?> approveIssue(@PathVariable Long id,
                                          @RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "librarianSignature", defaultValue = "Librarian") String librarianSignature) throws java.io.IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Signature image is required"));
        }
        String url = cloudinaryService.upload(file);
        return ResponseEntity.ok(adminService.approveIssue(id, librarianSignature, url));
    }

    @PostMapping("/approvals/issues/{id}/reject")
    public ResponseEntity<?> rejectIssue(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.rejectIssue(id));
    }

    // ----- Return Approvals -----

    @GetMapping("/approvals/returns")
    public ResponseEntity<?> getPendingReturns() {
        return ResponseEntity.ok(adminService.getPendingReturns());
    }

    @PostMapping(value = "/approvals/returns/{id}/approve", consumes = "multipart/form-data")
    public ResponseEntity<?> approveReturn(@PathVariable Long id,
                                           @RequestParam("file") MultipartFile file) throws java.io.IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Evidence image is required"));
        }
        String url = cloudinaryService.upload(file);
        return ResponseEntity.ok(adminService.approveReturn(id, url));
    }

    @PostMapping("/approvals/returns/{id}/reject")
    public ResponseEntity<?> rejectReturn(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.rejectReturn(id));
    }

    // ----- User Management -----

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/pending")
    public ResponseEntity<?> getPendingUsers() {
        return ResponseEntity.ok(adminService.getPendingUsers());
    }

    @PostMapping("/users/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approveUser(id));
    }

    @PostMapping("/users/{id}/reject")
    public ResponseEntity<?> rejectUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.rejectUser(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}