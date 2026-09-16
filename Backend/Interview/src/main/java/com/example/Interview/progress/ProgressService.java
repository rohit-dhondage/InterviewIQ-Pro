package com.example.Interview.progress;

import com.example.Interview.exception.ApiException;
import com.example.Interview.student.Student;
import com.example.Interview.student.StudentRepository;
import com.example.Interview.auth.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final StudentRepository studentRepository;

    /**
     * Called by ResumeService and MockInterviewService whenever scores are updated.
     */
    @Transactional
    public void record(Student student, Double resumeScore, Double overallScore,
                       Double technicalScore, Double communicationScore) {
        Progress progress = Progress.builder()
                .student(student)
                .resumeScore(resumeScore)
                .overallScore(overallScore)
                .technicalScore(technicalScore)
                .communicationScore(communicationScore)
                .readinessScore(student.getReadinessScore())
                .build();
        progressRepository.save(progress);
    }

    public List<Progress> getHistory(User user) {
        Student student = resolveStudent(user);
        return progressRepository.findByStudentIdOrderByRecordedAtDesc(student.getId());
    }

    public Progress getLatest(User user) {
        Student student = resolveStudent(user);
        return progressRepository.findFirstByStudentIdOrderByRecordedAtDesc(student.getId())
                .orElseThrow(() -> new ApiException("No progress recorded yet", HttpStatus.NOT_FOUND));
    }

    private Student resolveStudent(User user) {
        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("Student profile not found", HttpStatus.NOT_FOUND));
    }
}
