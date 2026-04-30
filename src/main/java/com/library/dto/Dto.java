package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

public class Dto {

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class RegisterRequest {

        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Mobile is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number (must be 10 digits starting with 6–9)")
        private String mobile;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Aadhar number is required")
        @Pattern(regexp = "^\\d{12}$", message = "Aadhar must be exactly 12 digits")
        private String aadharNumber;

        private String aadharPdf; // optional

        @NotNull(message = "Admission date is required")
        private LocalDate admissionDate;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
    }

    @Data
    public static class BookIssueDto {

        @NotBlank(message = "Book name is required")
        private String bookName;

        @NotBlank(message = "Book number is required")
        private String bookNumber;

        @NotBlank(message = "Admission date is required")
        private String admissionDate; // yyyy-MM-dd

        @NotBlank(message = "Issue date is required")
        private String issuedDate;

        @NotBlank(message = "Return date is required")
        private String returnDate;

        @NotBlank(message = "Signature is required")
        private String userSignature;
    }

    @Data
    public static class BookReturnDto {
        private Long issueRequestId;
    }

    @Data
    public static class ApproveIssueDto {
        private String librarianSignature;
    }

    @Data
    public static class BookStatusDto {
        private Long issueId;
        private Long returnRequestId;
        private String bookName;
        private String bookNumber;
        private String admissionDate;
        private String issuedDate;
        private String returnDate;
        private String userSignature;
        private String librarianSignature;
        private String librarianSignatureUrl;
        private String evidenceImageUrl;
        private String status; // effective: PENDING_ISSUE | ISSUED | PENDING_RETURN | RETURNED
        private Long userId;
        private String userName;
    }

    @Data
    public static class ActiveUserDto {
        private Long userId;
        private String name;
        private int activeBooksCount;
    }
}