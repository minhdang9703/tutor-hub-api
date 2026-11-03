package com.tutorhub.review.dto; import jakarta.validation.constraints.*; public record ReviewRequest(@Min(1) @Max(5) short rating,@Size(max=2000) String comment){}
