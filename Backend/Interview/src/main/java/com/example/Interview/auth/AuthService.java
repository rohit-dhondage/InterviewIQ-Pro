package com.example.Interview.auth;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.auth.Jwt.JwtService;
import com.example.Interview.auth.Repository.UserRepository;
import com.example.Interview.auth.dto.AuthResponse;
import com.example.Interview.auth.dto.LoginRequest;
import com.example.Interview.auth.dto.RegisterRequest;
import com.example.Interview.college.College;
import com.example.Interview.college.CollegeRepository;
import com.example.Interview.college.Department;
import com.example.Interview.college.DepartmentRepository;
import com.example.Interview.exception.ApiException;
import com.example.Interview.student.Student;
import com.example.Interview.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CollegeRepository collegeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException("An account with this email already exists", HttpStatus.CONFLICT);
        }

        College college = collegeRepository.findById(request.collegeId())
                .orElseGet(() -> collegeRepository.findAll().stream().findFirst()
                        .orElseGet(() -> collegeRepository.save(College.builder()
                                .name("PVG's College of Engineering & S. S. Dhamankar Institute of Management, Nashik")
                                .address("Nashik")
                                .build())));

        Department department = departmentRepository.findById(request.departmentId())
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst()
                        .orElseGet(() -> departmentRepository.save(Department.builder()
                                .name("Information Technology")
                                .college(college)
                                .build())));

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .targetRole(request.targetRole())
                .role(Role.STUDENT)
                .build();

        userRepository.save(user);

        Student student = Student.builder()
                .user(user)
                .college(college)
                .department(department)
                .year(request.year())
                .rollNo(request.rollNo())
                .build();

        studentRepository.save(student);

        String token = jwtService.generateToken(user);
        return toAuthResponse(user, token);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        String token = jwtService.generateToken(user);
        return toAuthResponse(user, token);
    }

    private AuthResponse toAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}