package com.tutorhub.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolveReportRequest(@NotBlank @Size(max = 1000) String resolution) {}
