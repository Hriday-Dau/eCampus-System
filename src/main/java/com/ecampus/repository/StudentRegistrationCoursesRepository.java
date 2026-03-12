package com.ecampus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecampus.model.StudentRegistrationCourses;

@Repository
public interface StudentRegistrationCoursesRepository extends JpaRepository<StudentRegistrationCourses, Long> {

    List<StudentRegistrationCourses> findBySrcsrgidIn(List<Long> srgIds);
}
