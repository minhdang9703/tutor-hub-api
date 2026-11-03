package com.tutorhub.parent.repository;
import com.tutorhub.parent.entity.Child; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface ChildRepository extends JpaRepository<Child,Long>{ List<Child> findByParentUserId(Long id); Optional<Child> findByIdAndParentUserId(Long id,Long parentId); }
