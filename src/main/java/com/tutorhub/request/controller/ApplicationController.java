package com.tutorhub.request.controller;

import com.tutorhub.common.pagination.PageResponse;
import com.tutorhub.common.security.CurrentUser;
import com.tutorhub.request.dto.ApplicationResponse;
import com.tutorhub.request.service.RequestService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/applications")
public class ApplicationController {
    private final RequestService service; private final CurrentUser current;
    public ApplicationController(RequestService service, CurrentUser current) { this.service = service; this.current = current; }
    @GetMapping("/mine") public PageResponse<ApplicationResponse> mine(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) { return service.myApplications(current.require(), page, size); }
    @PostMapping("/{id}/accept") @ResponseStatus(HttpStatus.NO_CONTENT) public void accept(@PathVariable Long id) { service.accept(current.require(), id); }
    @PostMapping("/{id}/reject") @ResponseStatus(HttpStatus.NO_CONTENT) public void reject(@PathVariable Long id) { service.reject(current.require(), id); }
    @PostMapping("/{id}/withdraw") @ResponseStatus(HttpStatus.NO_CONTENT) public void withdraw(@PathVariable Long id) { service.withdraw(current.require(), id); }
}
