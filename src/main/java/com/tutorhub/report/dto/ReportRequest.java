package com.tutorhub.report.dto;

import jakarta.validation.constraints.*;

public record ReportRequest(@NotBlank @Pattern(regexp = "USER|REVIEW|BOOKING|MESSAGE") String targetType, @NotNull @Positive Long targetId, @NotBlank @Size(max = 40) String reason, @Size(max = 2000) String description) {}
