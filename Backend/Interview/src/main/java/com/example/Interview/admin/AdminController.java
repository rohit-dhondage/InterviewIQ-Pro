package com.example.Interview.admin;

import com.example.Interview.admin.dto.*;
import com.example.Interview.analytics.AnalyticsService;
import com.example.Interview.college.College;
import com.example.Interview.college.Department;
import com.example.Interview.company.Company;
import com.example.Interview.tpo.TpoProfile;
import com.example.Interview.tpo.dto.TpoProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AnalyticsService analyticsService;

    // --- Analytics ---
    @GetMapping("/analytics")
    public PlatformAnalyticsResponse getAnalytics() {
        return analyticsService.getPlatformAnalytics();
    }

    // --- College Management ---
    @PostMapping("/colleges")
    @ResponseStatus(HttpStatus.CREATED)
    public College createCollege(@Valid @RequestBody CreateCollegeRequest req) {
        return adminService.createCollege(req);
    }

    @PutMapping("/colleges/{id}")
    public College updateCollege(@PathVariable Long id, @Valid @RequestBody CreateCollegeRequest req) {
        return adminService.updateCollege(id, req);
    }

    @PostMapping("/colleges/{id}/departments")
    @ResponseStatus(HttpStatus.CREATED)
    public Department addDepartment(@PathVariable Long id, @Valid @RequestBody CreateDepartmentRequest req) {
        return adminService.addDepartment(id, req);
    }

    @DeleteMapping("/departments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeDepartment(@PathVariable Long id) {
        adminService.removeDepartment(id);
    }

    // --- TPO Management ---
    @PostMapping("/tpo")
    @ResponseStatus(HttpStatus.CREATED)
    public TpoProfileResponse createTpoAccount(@Valid @RequestBody CreateTpoRequest req) {
        TpoProfile profile = adminService.createTpoAccount(req);
        return TpoProfileResponse.from(profile);
    }

    @GetMapping("/colleges/{id}/tpos")
    public List<TpoProfileResponse> getTposForCollege(@PathVariable Long id) {
        return adminService.getTposForCollege(id).stream()
                .map(TpoProfileResponse::from)
                .toList();
    }

    @DeleteMapping("/tpo/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateTpo(@PathVariable Long userId) {
        adminService.deactivateTpo(userId);
    }

    // --- Company Management ---
    @PostMapping("/companies")
    @ResponseStatus(HttpStatus.CREATED)
    public Company createCompany(@Valid @RequestBody CreateCompanyRequest req) {
        return adminService.createCompany(req);
    }

    @PutMapping("/companies/{id}")
    public Company updateCompany(@PathVariable Long id, @Valid @RequestBody CreateCompanyRequest req) {
        return adminService.updateCompany(id, req);
    }

    @DeleteMapping("/companies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompany(@PathVariable Long id) {
        adminService.deleteCompany(id);
    }
}
