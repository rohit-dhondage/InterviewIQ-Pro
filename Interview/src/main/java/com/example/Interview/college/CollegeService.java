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

    public List<College> getAllColleges() {
        return collegeRepository.findAll();
    }

    public List<Department> getDepartments(Long collegeId) {
        if (!collegeRepository.existsById(collegeId)) {
            throw new ApiException("No college found for id: " + collegeId, HttpStatus.NOT_FOUND);
        }
        return departmentRepository.findByCollegeId(collegeId);
    }
}