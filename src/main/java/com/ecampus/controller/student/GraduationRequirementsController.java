package com.ecampus.controller.student;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ecampus.dto.CourseTypeProgressDTO;
import com.ecampus.dto.OverallCourseTypeProgressDTO;
import com.ecampus.model.Batches;
import com.ecampus.model.Students;
import com.ecampus.service.StudentGraduationRequirementsService;

@Controller
@RequestMapping("/student/graduation-requirements")
public class GraduationRequirementsController {

    @Autowired
    private StudentGraduationRequirementsService srgService;

    @GetMapping("/current-semester")
    public String CurrentSemester(Authentication authentication, Model model) {
        StudentContext context = buildStudentContext(authentication, model);

        List<CourseTypeProgressDTO> progressList = srgService
                .buildCurrentSemesterProgress(context.stdid(), context.batchId(),
                        context.schemeId(), context.splid(), context.currentSemester());

        // Compute summary stats
        long totalCompleted = progressList.stream().mapToLong(CourseTypeProgressDTO::getCompletedCount).sum();
        long totalRequired = progressList.stream().mapToLong(CourseTypeProgressDTO::getRequiredCount).sum();
        long typesFulfilled = progressList.stream()
                .filter(p -> p.getCompletedCount() >= p.getRequiredCount())
                .count();
        long typesWithRequirement = progressList.size();

        model.addAttribute("progressList", progressList);
        model.addAttribute("totalCompleted", totalCompleted);
        model.addAttribute("totalRequired", totalRequired);
        model.addAttribute("typesFulfilled", typesFulfilled);
        model.addAttribute("typesWithRequirement", typesWithRequirement);

        return "student/graduation-requirements/current-semester";
    }

        @GetMapping("/overall-progress")
        public String overallProgress(Authentication authentication, Model model) {
                StudentContext context = buildStudentContext(authentication, model);

                List<OverallCourseTypeProgressDTO> progressList = srgService
                                .buildOverallProgress(context.stdid(), context.schemeId(), context.splid());

                long totalCompletedCourses = progressList.stream().mapToLong(OverallCourseTypeProgressDTO::getCompletedCourses).sum();
                long totalRequiredCourses = progressList.stream().mapToLong(OverallCourseTypeProgressDTO::getMinCourses).sum();
                long totalRemainingCourses = Math.max(0L, totalRequiredCourses - totalCompletedCourses);

                BigDecimal totalCompletedCredits = progressList.stream()
                                .map(OverallCourseTypeProgressDTO::getCompletedCredits)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalRequiredCredits = progressList.stream()
                                .map(OverallCourseTypeProgressDTO::getMinCredits)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalRemainingCredits = totalRequiredCredits.subtract(totalCompletedCredits).max(BigDecimal.ZERO);

                long fullySatisfiedTypes = progressList.stream()
                                .filter(OverallCourseTypeProgressDTO::isFullySatisfied)
                                .count();

                model.addAttribute("progressList", progressList);
                model.addAttribute("totalCompletedCourses", totalCompletedCourses);
                model.addAttribute("totalRequiredCourses", totalRequiredCourses);
                model.addAttribute("totalRemainingCourses", totalRemainingCourses);
                model.addAttribute("totalCompletedCredits", formatDecimal(totalCompletedCredits));
                model.addAttribute("totalRequiredCredits", formatDecimal(totalRequiredCredits));
                model.addAttribute("totalRemainingCredits", formatDecimal(totalRemainingCredits));
                model.addAttribute("typesFullySatisfied", fullySatisfiedTypes);
                model.addAttribute("totalTypes", progressList.size());

                return "student/graduation-requirements/overall-progress";
        }

        private StudentContext buildStudentContext(Authentication authentication, Model model) {
                String username = authentication.getName();
                Long stdid = srgService.getStudentIdByUsername(username);
                Students student = srgService.getStudent(stdid);

                Long batchId = student.getStdbchid();
                Batches batch = srgService.getBatch(batchId);
                String currentSemester = srgService.getCurrentSemesterName(batchId);
                String studentName = (student.getStdfirstname() != null ? student.getStdfirstname() : "")
                                + (student.getStdlastname() != null ? " " + student.getStdlastname() : "");

                model.addAttribute("studentName", studentName.trim());
                model.addAttribute("studentId", student.getStdinstid());
                model.addAttribute("batchName", batch.getBchname());
                model.addAttribute("currentSemester", currentSemester);

                return new StudentContext(stdid, batchId, batch.getSchemeId(), batch.getSplid(), currentSemester);
        }

        private String formatDecimal(BigDecimal value) {
                BigDecimal normalized = (value != null ? value : BigDecimal.ZERO).stripTrailingZeros();
                if (normalized.scale() < 0) {
                        normalized = normalized.setScale(0);
                }
                return normalized.toPlainString();
        }

        private record StudentContext(Long stdid, Long batchId, Long schemeId, Long splid, String currentSemester) {
        }
}
