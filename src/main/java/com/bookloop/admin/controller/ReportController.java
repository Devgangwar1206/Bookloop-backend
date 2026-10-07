package com.bookloop.admin.controller;

import com.bookloop.admin.dto.AdminReportResponse;
import com.bookloop.admin.dto.ReportCreateRequest;
import com.bookloop.admin.service.AdminService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final AdminService adminService;

    @PostMapping
    public ResponseEntity<AdminReportResponse> submitReport(
            @RequestBody ReportCreateRequest request,
            Authentication authentication) {

        String email = authentication != null ? authentication.getName() : null;

        return ResponseEntity.ok(
                adminService.createReport(
                        request.getBookId(),
                        request.getReason(),
                        request.getDetails(),
                        email
                )
        );
    }
}
