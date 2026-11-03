package com.tutorhub.catalog.repository;
import com.tutorhub.catalog.entity.District; import java.util.List; import org.springframework.data.jpa.repository.JpaRepository;
public interface DistrictRepository extends JpaRepository<District,Integer>{ List<District> findByCityIgnoreCaseOrderByName(String city); List<District> findAllByOrderByCityAscNameAsc(); }
