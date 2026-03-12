package com.ecampus.controller.student;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ecampus.dto.CourseTypeProgressDTO;
import com.ecampus.model.Batches;
import com.ecampus.model.Students;
import com.ecampus.service.StudentDashboardService;

@Controller
@RequestMapping("/student")
public class StudentDashboardController {

    @Autowired
    private StudentDashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        Long stdid = dashboardService.getStudentIdByUsername(username);
        Students student = dashboardService.getStudent(stdid);

        Long batchId = student.getStdbchid();
        Batches batch = dashboardService.getBatch(batchId);
        String currentSemester = dashboardService.getCurrentSemesterName(batchId);

        Long schemeId = batch.getSchemeId();
        Long splid = batch.getSplid();

        List<CourseTypeProgressDTO> progressList = dashboardService
                .buildCurrentSemesterProgress(stdid, batchId, schemeId, splid, currentSemester);

        String studentName = (student.getStdfirstname() != null ? student.getStdfirstname() : "")
                + (student.getStdlastname() != null ? " " + student.getStdlastname() : "");

        // Compute summary stats
        long totalCompleted = progressList.stream().mapToLong(CourseTypeProgressDTO::getCompletedCount).sum();
        long totalRequired = progressList.stream().mapToLong(CourseTypeProgressDTO::getRequiredCount).sum();
        long typesFulfilled = progressList.stream()
                .filter(p -> p.getRequiredCount() > 0 && p.getCompletedCount() >= p.getRequiredCount())
                .count();
        long typesWithRequirement = progressList.stream()
                .filter(p -> p.getRequiredCount() > 0)
                .count();

        model.addAttribute("studentName", studentName.trim());
        model.addAttribute("studentId", student.getStdinstid());
        model.addAttribute("batchName", batch.getBchname());
        model.addAttribute("currentSemester", currentSemester);
        model.addAttribute("progressList", progressList);
        model.addAttribute("totalCompleted", totalCompleted);
        model.addAttribute("totalRequired", totalRequired);
        model.addAttribute("typesFulfilled", typesFulfilled);
        model.addAttribute("typesWithRequirement", typesWithRequirement);

        return "student-dashboard";
    }
}
