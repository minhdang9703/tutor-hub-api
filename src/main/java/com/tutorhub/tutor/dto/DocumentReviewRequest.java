package com.tutorhub.tutor.dto;
import jakarta.validation.constraints.*;
public record DocumentReviewRequest(@Pattern(regexp="APPROVED|REJECTED") String status,@Size(max=500) String reason) {}
