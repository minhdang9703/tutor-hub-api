package com.tutorhub.tutor.dto;
import jakarta.validation.constraints.*;
public record TutorSubjectRequest(@NotNull Integer subjectId,@Min(1)@Max(12) short gradeFrom,@Min(1)@Max(12) short gradeTo) { public boolean validRange(){ return gradeTo >= gradeFrom; } }
