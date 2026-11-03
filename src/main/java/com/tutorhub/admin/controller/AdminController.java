package com.tutorhub.admin.controller;

import com.tutorhub.admin.dto.*;
import com.tutorhub.admin.service.AdminService;
import com.tutorhub.common.pagination.PageResponse;
import com.tutorhub.common.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/admin") @PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService admin; private final CurrentUser current;
    public AdminController(AdminService admin, CurrentUser current) { this.admin = admin; this.current = current; }
    @GetMapping("/users") PageResponse<AdminUserResponse> users(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return admin.users(Math.max(page, 0), Math.clamp(size, 1, 100)); }
    @PostMapping("/users/{id}/suspend") @ResponseStatus(HttpStatus.NO_CONTENT) void suspend(@PathVariable long id) { admin.setUserStatus(current.require().id(), id, "SUSPENDED"); }
    @PostMapping("/users/{id}/activate") @ResponseStatus(HttpStatus.NO_CONTENT) void activate(@PathVariable long id) { admin.setUserStatus(current.require().id(), id, "ACTIVE"); }
    @GetMapping("/reports") PageResponse<AdminReportResponse> reports(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return admin.reports(Math.max(page, 0), Math.clamp(size, 1, 100)); }
    @PostMapping("/reports/{id}/resolve") @ResponseStatus(HttpStatus.NO_CONTENT) void resolve(@PathVariable long id, @Valid @RequestBody ResolveReportRequest request) { admin.resolve(current.require().id(), id, request.resolution()); }
    @PostMapping("/reviews/{id}/hide") @ResponseStatus(HttpStatus.NO_CONTENT) void hideReview(@PathVariable long id) { admin.hideReview(current.require().id(), id); }
    @GetMapping("/audit-logs") PageResponse<AuditLogResponse> auditLogs(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return admin.auditLogs(Math.max(page, 0), Math.clamp(size, 1, 100)); }
    @GetMapping("/stats") AdminStatsResponse stats() { return admin.stats(); }
}
