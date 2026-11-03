package com.tutorhub.tutor.repository;
import com.tutorhub.tutor.entity.TutorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TutorProfileRepository extends JpaRepository<TutorProfile, Long> {}
