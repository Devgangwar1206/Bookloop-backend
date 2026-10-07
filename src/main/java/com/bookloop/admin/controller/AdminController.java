package com.bookloop.admin.controller;

 
import com.bookloop.admin.dto.AdminOverviewResponse;
import com.bookloop.admin.dto.AdminReportResponse;
import com.bookloop.admin.dto.AdminUserResponse;
import com.bookloop.admin.dto.ResolveReportRequest;
import com.bookloop.admin.dto.UpdateUserStatusRequest;
import com.bookloop.admin.service.AdminService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // =========================================================
    // ADMIN OVERVIEW
    // =========================================================

    @GetMapping("/overview")
    public ResponseEntity<AdminOverviewResponse> getAdminOverview() {

        return ResponseEntity.ok(
                adminService.getAdminOverview()
        );
    }

    // =========================================================
    // USER STATUS
    // =========================================================

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UpdateUserStatusRequest request) {

        return ResponseEntity.ok(
                adminService.updateUserStatus(
                        userId,
                        request.getStatus()
                )
        );
    }

    // =========================================================
    // USER VERIFICATION
    // =========================================================

    @PatchMapping("/users/{userId}/verify")
    public ResponseEntity<AdminUserResponse> toggleUserVerification(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                adminService.toggleUserVerification(userId)
        );
    }

    // =========================================================
    // REPORT ACTION
    // =========================================================

    @PatchMapping("/reports/{reportId}")
    public ResponseEntity<AdminReportResponse> resolveReport(
            @PathVariable Long reportId,
            @RequestBody ResolveReportRequest request) {

        return ResponseEntity.ok(
                adminService.resolveReport(
                        reportId,
                        request.getAction()
                )
        );
    }

    // =========================================================
    // GET USERS
    // =========================================================

    @GetMapping("/users")
    public ResponseEntity<java.util.List<AdminUserResponse>> getAllUsers() {

        return ResponseEntity.ok(
                adminService.getAllUsers()
        );
    }

    // =========================================================
    // GET REPORTS
    // =========================================================

    @GetMapping("/reports")
    public ResponseEntity<java.util.List<AdminReportResponse>> getAllReports() {

        return ResponseEntity.ok(
                adminService.getAllReports()
        );
    }

    // =========================================================
    // CREATE REPORT
    // =========================================================

    @PostMapping("/reports")
    public ResponseEntity<AdminReportResponse> createReport(
            @RequestBody(required = false) com.bookloop.admin.dto.ReportCreateRequest body,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String details,
            Authentication authentication) {

        Long targetBookId = body != null && body.getBookId() != null ? body.getBookId() : bookId;
        String reportReason = body != null && body.getReason() != null ? body.getReason() : reason;
        String reportDetails = body != null && body.getDetails() != null ? body.getDetails() : details;

        return ResponseEntity.ok(
                adminService.createReport(
                        targetBookId,
                        reportReason,
                        reportDetails,
                        authentication.getName()
                )
        );
    }
}