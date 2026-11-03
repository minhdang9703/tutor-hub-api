package com.tutorhub.parent.controller;

import com.tutorhub.common.security.CurrentUser;
import com.tutorhub.parent.dto.ParentProfileRequest;
import com.tutorhub.parent.dto.ParentProfileResponse;
import com.tutorhub.parent.service.ParentProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/parents/me")
public class ParentProfileController {
    private final ParentProfileService service; private final CurrentUser current;
    public ParentProfileController(ParentProfileService service, CurrentUser current) { this.service = service; this.current = current; }
    @GetMapping public ParentProfileResponse get() { return service.get(current.require()); }
    @PutMapping public ParentProfileResponse update(@Valid @RequestBody ParentProfileRequest request) { return service.update(current.require(), request); }
}
