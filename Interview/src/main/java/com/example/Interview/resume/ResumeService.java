package com.example.Interview.resume;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.exception.ApiException;
import com.example.Interview.student.Student;
import com.example.Interview.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final StudentRepository studentRepository;
    private final ResumeStorageService storageService;
    private final ResumeTextExtractor textExtractor;
    private final ResumeScoringService scoringService;

    @Transactional
    public Resume upload(User user, MultipartFile file) {
        Student student = resolveStudent(user);

        String storedPath = storageService.store(student.getId(), file);
        byte[] fileBytes = storageService.loadAsBytes(storedPath);
        String text = textExtractor.extractText(fileBytes);

        ResumeScoringService.ScoringResult scoring = scoringService.score(text);

        Resume resume = Resume.builder()
                .student(student)
                .resumeUrl(storedPath)
                .atsScore(scoring.atsScore())
                .feedback(scoring.feedback())
                .build();

        return resumeRepository.save(resume);
    }

    public List<Resume> history(User user) {
        Student student = resolveStudent(user);
        return resumeRepository.findByStudentIdOrderByUploadedAtDesc(student.getId());
    }

    public Resume latest(User user) {
        return history(user).stream()
                .findFirst()
                .orElseThrow(() -> new ApiException("No resume uploaded yet", HttpStatus.NOT_FOUND));
    }

    private Student resolveStudent(User user) {
        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("No student profile linked to this account", HttpStatus.NOT_FOUND));
    }
}