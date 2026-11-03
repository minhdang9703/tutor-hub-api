package com.tutorhub.admin.controller;

import com.tutorhub.admin.service.TutorAdminService;
import com.tutorhub.common.pagination.PageResponse;
import com.tutorhub.common.security.CurrentUser;
import com.tutorhub.tutor.dto.*;
import com.tutorhub.tutor.service.TutorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/admin") @PreAuthorize("hasRole('ADMIN')")
public class TutorAdminController {
    private final TutorService tutors; private final TutorAdminService admin; private final CurrentUser current;
    public TutorAdminController(TutorService tutors, TutorAdminService admin, CurrentUser current) { this.tutors = tutors; this.admin = admin; this.current = current; }
    @GetMapping("/tutors") PageResponse<TutorProfileResponse> pending(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return tutors.pending(Math.max(0, page), Math.clamp(size, 1, 100)); }
    @GetMapping("/tutors/{id}") TutorProfileResponse detail(@PathVariable long id) { return tutors.adminDetail(id); }
    @PostMapping("/tutors/{id}/verify") @ResponseStatus(HttpStatus.NO_CONTENT) void verify(@PathVariable long id) { admin.verify(current.require().id(), id); }
    @PostMapping("/tutors/{id}/reject") @ResponseStatus(HttpStatus.NO_CONTENT) void reject(@PathVariable long id, @Valid @RequestBody TutorDecisionRequest request) { admin.reject(current.require().id(), id, request.reason()); }
    @PostMapping("/documents/{id}/review") @ResponseStatus(HttpStatus.NO_CONTENT) void reviewDocument(@PathVariable long id, @Valid @RequestBody DocumentReviewRequest request) { admin.reviewDocument(current.require().id(), id, request.status(), request.reason()); }
}
