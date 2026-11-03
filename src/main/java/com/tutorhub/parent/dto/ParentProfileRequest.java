package com.tutorhub.parent.dto;
import jakarta.validation.constraints.Size;
public record ParentProfileRequest(@Size(max=255) String address, Integer districtId) {}
