package com.example.Interview.resume;

import com.example.Interview.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Handles physical storage of resume files. Callers depend only on
 * store()/loadAsBytes() — swapping local disk for S3/Cloudinary later
 * means changing this class only, not ResumeService or the controller.
 */
@Service
@Slf4j
public class ResumeStorageService {

    private final Path baseDir;

    public ResumeStorageService(@Value("${app.storage.resume-dir}") String resumeDir) {
        this.baseDir = Path.of(resumeDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create resume storage directory: " + baseDir, e);
        }
    }

    /**
     * Stores the file under a per-student subfolder with a random filename
     * (never trusts the client-supplied original filename for the path).
     * Returns the relative path to persist on the Resume entity.
     */
    public String store(Long studentId, MultipartFile file) {
        validate(file);

        String extension = extensionOf(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + extension;
        Path studentDir = baseDir.resolve(String.valueOf(studentId)).normalize();

        if (!studentDir.startsWith(baseDir)) {
            // Defensive: studentId is a Long from the authenticated principal, so
            // this should be unreachable, but never resolve a path outside baseDir.
            throw new ApiException("Invalid storage path", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            Files.createDirectories(studentDir);
            Path target = studentDir.resolve(storedName);
            file.transferTo(target);
            // Store relative path (base dir is deployment-specific, not portable)
            return baseDir.relativize(target).toString().replace('\\', '/');
        } catch (IOException e) {
            log.error("Failed to store resume for student {}", studentId, e);
            throw new ApiException("Could not save resume file, please try again", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public byte[] loadAsBytes(String relativePath) {
        Path target = baseDir.resolve(relativePath).normalize();
        if (!target.startsWith(baseDir)) {
            throw new ApiException("Invalid resume path", HttpStatus.BAD_REQUEST);
        }
        try {
            return Files.readAllBytes(target);
        } catch (IOException e) {
            log.error("Failed to read resume at {}", relativePath, e);
            throw new ApiException("Could not read resume file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("Resume file is required", HttpStatus.BAD_REQUEST);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new ApiException("Only PDF resumes are supported", HttpStatus.BAD_REQUEST);
        }
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".pdf";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}