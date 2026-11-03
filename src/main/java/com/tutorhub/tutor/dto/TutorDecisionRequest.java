package com.tutorhub.tutor.dto;
import jakarta.validation.constraints.Size;
public record TutorDecisionRequest(@Size(max=500) String reason) {}
