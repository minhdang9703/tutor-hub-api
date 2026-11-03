package com.tutorhub.report.controller;

import com.tutorhub.common.security.CurrentUser;
import com.tutorhub.report.dto.ReportRequest;
import com.tutorhub.report.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/reports")
public class ReportController {
    private final ReportService reports; private final CurrentUser current;
    public ReportController(ReportService reports, CurrentUser current) { this.reports = reports; this.current = current; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) void create(@Valid @RequestBody ReportRequest request) { reports.create(current.require(), request); }
}
