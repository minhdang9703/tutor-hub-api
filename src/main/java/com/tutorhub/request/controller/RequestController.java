package com.tutorhub.request.controller;

import com.tutorhub.common.pagination.PageResponse;
import com.tutorhub.common.security.CurrentUser;
import com.tutorhub.request.dto.*;
import com.tutorhub.request.service.RequestService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/requests")
public class RequestController {
    private final RequestService service; private final CurrentUser current;
    public RequestController(RequestService service, CurrentUser current) { this.service = service; this.current = current; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public TutorRequestResponse create(@Valid @RequestBody TutorRequestInput input) { return service.create(current.require(), input); }
    @GetMapping public PageResponse<TutorRequestResponse> open(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) { return service.open(current.require(), page, size); }
    @GetMapping("/mine") public PageResponse<TutorRequestResponse> mine(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) { return service.mine(current.require(), page, size); }
    @GetMapping("/{id}") public TutorRequestResponse get(@PathVariable Long id) { return service.get(current.require(), id); }
    @PatchMapping("/{id}") public TutorRequestResponse update(@PathVariable Long id, @Valid @RequestBody TutorRequestInput input) { return service.update(current.require(), id, input); }
    @PostMapping("/{id}/close") @ResponseStatus(HttpStatus.NO_CONTENT) public void close(@PathVariable Long id) { service.close(current.require(), id); }
    @PostMapping("/{id}/applications") @ResponseStatus(HttpStatus.CREATED) public ApplicationResponse apply(@PathVariable Long id, @Valid @RequestBody ApplicationInput input) { return service.apply(current.require(), id, input); }
    @GetMapping("/{id}/applications") public List<ApplicationResponse> applications(@PathVariable Long id) { return service.applications(current.require(), id); }
}
