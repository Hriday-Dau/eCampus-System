package com.ecampus.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecampus.dto.CourseTypeProgressDTO;
import com.ecampus.model.*;
import com.ecampus.repository.*;

@Service
public class StudentDashboardService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private StudentsRepository studentsRepo;

    @Autowired
    private BatchesRepository batchesRepo;

    @Autowired
    private SemestersRepository semestersRepo;

    @Autowired
    private CourseTypesRepository courseTypesRepo;

    @Autowired
    private SchemeCoursesRepository schemeCoursesRepo;

    @Autowired
    private StudentRegistrationsRepository studentRegistrationsRepo;

    @Autowired
    private StudentRegistrationCoursesRepository studentRegCoursesRepo;

    @Autowired
    private Egcrstt1Repository egcrstt1Repo;

    /**
     * Returns the stdid for the logged-in user.
     */
    public Long getStudentIdByUsername(String username) {
        return userRepo.findIdByUname(username);
    }

    /**
     * Returns the Students entity.
     */
    public Students getStudent(Long stdid) {
        return studentsRepo.findStudent(stdid);
    }

    /**
     * Returns the Batches entity for a student.
     */
    public Batches getBatch(Long batchId) {
        return batchesRepo.findById(batchId).orElse(null);
    }

    /**
     * Returns the current (latest) semester name for a batch.
     */
    public String getCurrentSemesterName(Long batchId) {
        Long maxStrid = semestersRepo.findMaxSemesterId(batchId);
        if (maxStrid == null) return null;
        return semestersRepo.findById(maxStrid).map(Semesters::getStrname).orElse(null);
    }

    /**
     * Builds the course-type-wise progress for a student until the current semester.
     */
    public List<CourseTypeProgressDTO> buildCurrentSemesterProgress(Long stdid, Long batchId,
                                                                      Long schemeId, Long splid,
                                                                      String currentSemesterName) {
        // --- a. Get all course types for this scheme + splid ---
        List<CourseTypes> courseTypes = courseTypesRepo.findBySchemeIdAndSplidOrderByCtpid(schemeId, splid);

        // --- b. Generate semester name list up to current ---
        List<String> semesterNames = generateSemesterNamesUpTo(currentSemesterName);

        // --- b. Count required courses per ctpid from SchemeCourses ---
        Map<Long, Long> requiredByCtpid = new HashMap<>();
        if (!semesterNames.isEmpty()) {
            List<SchemeCourses> schemeCourses = schemeCoursesRepo
                    .findBySchemeIdAndSplidAndSemesterNameIn(schemeId, splid, semesterNames);
            requiredByCtpid = schemeCourses.stream()
                    .filter(sc -> sc.getCtpid() != null)
                    .collect(Collectors.groupingBy(SchemeCourses::getCtpid, Collectors.counting()));
        }

        // --- c. Count completed (passed) courses per ctpid ---
        Map<Long, Long> completedByCtpid = buildCompletedCounts(stdid);

        // --- Build progress list ---
        List<CourseTypeProgressDTO> progressList = new ArrayList<>();
        for (CourseTypes ct : courseTypes) {
            long required = requiredByCtpid.getOrDefault(ct.getCtpid(), 0L);
            long completed = completedByCtpid.getOrDefault(ct.getCtpid(), 0L);
            progressList.add(new CourseTypeProgressDTO(
                    ct.getCtpid(), ct.getCtpcode(), ct.getCtpname(), ct.getCrscat(),
                    required, completed));
        }

        // Also include any ctpid where student completed courses but is not in scheme course types
        Set<Long> knownCtpids = courseTypes.stream().map(CourseTypes::getCtpid).collect(Collectors.toSet());
        for (Map.Entry<Long, Long> entry : completedByCtpid.entrySet()) {
            if (!knownCtpids.contains(entry.getKey())) {
                CourseTypes extra = courseTypesRepo.findById(entry.getKey()).orElse(null);
                if (extra != null) {
                    long required = requiredByCtpid.getOrDefault(entry.getKey(), 0L);
                    progressList.add(new CourseTypeProgressDTO(
                            extra.getCtpid(), extra.getCtpcode(), extra.getCtpname(), extra.getCrscat(),
                            required, entry.getValue()));
                }
            }
        }

        return progressList;
    }

    /**
     * Builds the map of ctpid -> count of completed/passed courses for a student.
     */
    private Map<Long, Long> buildCompletedCounts(Long stdid) {
        // Get all student registrations
        List<StudentRegistrations> registrations = studentRegistrationsRepo.findregisteredsemesters(stdid);
        if (registrations.isEmpty()) return Collections.emptyMap();

        List<Long> srgIds = registrations.stream()
                .map(StudentRegistrations::getSrgid)
                .toList();

        // Get all registration courses
        List<StudentRegistrationCourses> regCourses = studentRegCoursesRepo.findBySrcsrgidIn(srgIds);
        if (regCourses.isEmpty()) return Collections.emptyMap();

        // Map tcrid -> curr_ctpid
        Map<Long, Long> tcrToCtpMap = new HashMap<>();
        for (StudentRegistrationCourses rc : regCourses) {
            Long ctpid = rc.getCurrCtpid() != null ? rc.getCurrCtpid() : rc.getOrigCtpid();
            if (ctpid != null) {
                tcrToCtpMap.put(rc.getSrctcrid(), ctpid);
            }
        }

        List<Long> tcrIds = new ArrayList<>(tcrToCtpMap.keySet());
        if (tcrIds.isEmpty()) return Collections.emptyMap();

        // Get grade entries
        List<Egcrstt1> grades = egcrstt1Repo.findByStudIdAndTcridIn(stdid, tcrIds);

        // Filter: only passed (entry exists AND obtgr_id not 5 and not 8)
        Set<Long> passedTcrIds = grades.stream()
                .filter(g -> g.getObtgrId() != null && g.getObtgrId() != 5L && g.getObtgrId() != 8L)
                .map(Egcrstt1::getTcrid)
                .collect(Collectors.toSet());

        // Count by ctpid
        Map<Long, Long> completedByCtpid = new HashMap<>();
        for (Long tcrid : passedTcrIds) {
            Long ctpid = tcrToCtpMap.get(tcrid);
            if (ctpid != null) {
                completedByCtpid.merge(ctpid, 1L, Long::sum);
            }
        }
        return completedByCtpid;
    }

    /**
     * Generates the ordered list of semester names up to and including the given semester.
     * Pattern: Semester I, Semester II, Summer I, Semester III, Semester IV, Summer II, ...
     */
    private List<String> generateSemesterNamesUpTo(String targetName) {
        if (targetName == null || targetName.isBlank()) return Collections.emptyList();

        List<String> result = new ArrayList<>();
        for (int group = 1; group <= 10; group++) {
            String sem1 = "Semester " + toRoman(2 * group - 1);
            result.add(sem1);
            if (sem1.equalsIgnoreCase(targetName)) return result;

            String sem2 = "Semester " + toRoman(2 * group);
            result.add(sem2);
            if (sem2.equalsIgnoreCase(targetName)) return result;

            String summer = "Summer " + toRoman(group);
            result.add(summer);
            if (summer.equalsIgnoreCase(targetName)) return result;
        }

        // If target was not matched by the pattern, return whatever we generated
        // This handles edge cases where naming might differ
        return result;
    }

    private static String toRoman(int num) {
        String[] r = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X",
                       "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"};
        if (num >= 1 && num < r.length) return r[num];
        return String.valueOf(num);
    }
}
