package com.example.Interview.college;

import com.example.Interview.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollegeService {

    private final CollegeRepository collegeRepository;
    private final DepartmentRepository departmentRepository;
    private final com.example.Interview.tpo.TpoProfileRepository tpoProfileRepository;

    public List<College> getAllColleges() {
        return collegeRepository.findAll();
    }

    public List<Department> getDepartments(Long collegeId) {
        if (!collegeRepository.existsById(collegeId)) {
            throw new ApiException("No college found for id: " + collegeId, HttpStatus.NOT_FOUND);
        }
        return departmentRepository.findByCollegeId(collegeId);
    }

    public List<com.example.Interview.student.dto.TpoContactResponse> getTpoContacts(Long collegeId) {
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new ApiException("College not found", HttpStatus.NOT_FOUND));

        List<com.example.Interview.tpo.TpoProfile> tpos = tpoProfileRepository.findByCollegeId(collegeId);

        return tpos.stream()
                .map(tpo -> new com.example.Interview.student.dto.TpoContactResponse(
                        tpo.getUser().getFullName(),
                        tpo.getUser().getEmail(),
                        tpo.getContactNumber(),
                        college.getName()
                ))
                .toList();
    }
}