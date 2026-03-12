package com.ecampus.repository;

import com.ecampus.model.SchemeCourses;
import com.ecampus.model.SchemeCoursesId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchemeCoursesRepository extends JpaRepository<SchemeCourses, SchemeCoursesId> {
    // Find all scheme courses for a scheme (all specializations)
    List<SchemeCourses> findBySchemeId(Long schemeId);

    // Find all scheme courses for a scheme limited to a set of splids, ordered by programYear and termSeqNo
    List<SchemeCourses> findBySchemeIdAndSplidInOrderByProgramYearAscTermSeqNoAsc(Long schemeId, List<Long> splids);

    // Find courses for specific splid, termName and programYear, ordered by courseSrNo
    List<SchemeCourses> findBySchemeIdAndSplidAndTermNameAndProgramYearOrderByCourseSrNo(
            Long schemeId, Long splid, String termName, Long programYear);
    
    // Find courses for multiple splids, termName and programYear, ordered by splid then courseSrNo
    List<SchemeCourses> findBySchemeIdAndSplidInAndTermNameAndProgramYearOrderBySplidAscCourseSrNoAsc(
            Long schemeId, List<Long> splids, String termName, Long programYear);

    // Find all scheme courses for a scheme, splid, and matching semester names
    List<SchemeCourses> findBySchemeIdAndSplidAndSemesterNameIn(Long schemeId, Long splid, List<String> semesterNames);
}
