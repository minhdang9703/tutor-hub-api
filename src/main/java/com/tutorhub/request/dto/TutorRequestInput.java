package com.tutorhub.request.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record TutorRequestInput(Long childId, @NotNull Integer subjectId, @NotNull @Min(1) @Max(12) Short grade,
        Integer districtId, @NotBlank @Pattern(regexp="ONLINE|AT_HOME") String mode, @PositiveOrZero Long budgetMin,
        @PositiveOrZero Long budgetMax, @Min(1) @Max(14) Short sessionsPerWeek, @Size(max=500) String scheduleNote,
        @Size(max=10000) String description, @Future Instant expiresAt) {
    @AssertTrue(message="budgetMax must be at least budgetMin") public boolean validBudget() { return budgetMin == null || budgetMax == null || budgetMax >= budgetMin; }
}
