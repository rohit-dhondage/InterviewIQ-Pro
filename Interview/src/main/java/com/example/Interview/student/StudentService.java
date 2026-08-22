package com.example.Interview.student;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.college.College;
import com.example.Interview.college.CollegeRepository;
import com.example.Interview.college.Department;
import com.example.Interview.college.DepartmentRepository;
import com.example.Interview.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final CollegeRepository collegeRepository;
    private final DepartmentRepository departmentRepository;

    public Student getStudentProfile(User user) {
        return resolveStudent(user);
    }

    @Transactional
    public Student updateStudentProfile(User user, UpdateProfileRequest request) {
        Student student = resolveStudent(user);

        if (request.collegeId() != null) {
            College college = collegeRepository.findById(request.collegeId())
                    .orElseThrow(() -> new ApiException("College not found", HttpStatus.NOT_FOUND));
            student.setCollege(college);
        }

        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ApiException("Department not found", HttpStatus.NOT_FOUND));
            student.setDepartment(department);
        }

        if (request.year() != null) student.setYear(request.year());
        if (request.rollNo() != null) student.setRollNo(request.rollNo());
        if (request.cgpa() != null) student.setCgpa(request.cgpa());

        return studentRepository.save(student);
    }

    private Student resolveStudent(User user) {
        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("Student profile not found", HttpStatus.NOT_FOUND));
    }

    public record UpdateProfileRequest(Long collegeId, Long departmentId, Integer year, String rollNo, Double cgpa) {}
}
