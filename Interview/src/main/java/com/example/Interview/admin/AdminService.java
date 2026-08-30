package com.example.Interview.admin;

import com.example.Interview.admin.dto.CreateCollegeRequest;
import com.example.Interview.admin.dto.CreateCompanyRequest;
import com.example.Interview.admin.dto.CreateDepartmentRequest;
import com.example.Interview.admin.dto.CreateTpoRequest;
import com.example.Interview.auth.AuthService;
import com.example.Interview.auth.Role;
import com.example.Interview.auth.Entity.User;
import com.example.Interview.auth.Repository.UserRepository;
import com.example.Interview.college.College;
import com.example.Interview.college.CollegeRepository;
import com.example.Interview.college.Department;
import com.example.Interview.college.DepartmentRepository;
import com.example.Interview.company.Company;
import com.example.Interview.company.CompanyRepository;
import com.example.Interview.exception.ApiException;
import com.example.Interview.tpo.TpoProfile;
import com.example.Interview.tpo.TpoProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final CollegeRepository collegeRepository;
    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final TpoProfileRepository tpoProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // --- College Management ---
    
    public College createCollege(CreateCollegeRequest req) {
        College college = College.builder()
                .name(req.name())
                .address(req.address())
                .build();
        return collegeRepository.save(college);
    }

    public College updateCollege(Long id, CreateCollegeRequest req) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ApiException("College not found", HttpStatus.NOT_FOUND));
        college.setName(req.name());
        college.setAddress(req.address());
        return collegeRepository.save(college);
    }

    public Department addDepartment(Long collegeId, CreateDepartmentRequest req) {
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new ApiException("College not found", HttpStatus.NOT_FOUND));
        Department dept = Department.builder()
                .name(req.name())
                .college(college)
                .build();
        return departmentRepository.save(dept);
    }

    public void removeDepartment(Long deptId) {
        if (!departmentRepository.existsById(deptId)) {
            throw new ApiException("Department not found", HttpStatus.NOT_FOUND);
        }
        departmentRepository.deleteById(deptId);
    }

    // --- TPO Management ---
    
    @Transactional
    public TpoProfile createTpoAccount(CreateTpoRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ApiException("Email already in use", HttpStatus.CONFLICT);
        }
        
        College college = collegeRepository.findById(req.collegeId())
                .orElseThrow(() -> new ApiException("College not found", HttpStatus.NOT_FOUND));
                
        User user = User.builder()
                .fullName(req.fullName())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(Role.TPO)
                .build();
        
        userRepository.save(user);
        
        TpoProfile profile = TpoProfile.builder()
                .user(user)
                .college(college)
                .contactNumber(req.contactNumber())
                .build();
                
        return tpoProfileRepository.save(profile);
    }
    
    public List<TpoProfile> getTposForCollege(Long collegeId) {
        return tpoProfileRepository.findByCollegeId(collegeId);
    }
    
    @Transactional
    public void deactivateTpo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        if (user.getRole() != Role.TPO) {
            throw new ApiException("User is not a TPO", HttpStatus.BAD_REQUEST);
        }
        
        TpoProfile profile = tpoProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("TPO Profile not found", HttpStatus.NOT_FOUND));
                
        tpoProfileRepository.delete(profile);
        userRepository.delete(user);
    }

    // --- Company Management ---

    public Company createCompany(CreateCompanyRequest req) {
        Company company = Company.builder()
                .name(req.name())
                .requiredSkills(req.requiredSkills() != null ? req.requiredSkills() : List.of())
                .minimumScore(req.minimumScore())
                .difficulty(req.difficulty())
                .build();
        return companyRepository.save(company);
    }

    public Company updateCompany(Long id, CreateCompanyRequest req) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ApiException("Company not found", HttpStatus.NOT_FOUND));
        company.setName(req.name());
        if (req.requiredSkills() != null) {
            company.setRequiredSkills(req.requiredSkills());
        }
        company.setMinimumScore(req.minimumScore());
        company.setDifficulty(req.difficulty());
        return companyRepository.save(company);
    }

    public void deleteCompany(Long id) {
        if (!companyRepository.existsById(id)) {
            throw new ApiException("Company not found", HttpStatus.NOT_FOUND);
        }
        companyRepository.deleteById(id);
    }
}
