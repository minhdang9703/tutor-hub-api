package com.tutorhub.catalog.repository;
import com.tutorhub.catalog.entity.Subject; import java.util.List; import org.springframework.data.jpa.repository.JpaRepository;
public interface SubjectRepository extends JpaRepository<Subject,Integer>{ List<Subject> findByActiveTrueOrderByName(); }
