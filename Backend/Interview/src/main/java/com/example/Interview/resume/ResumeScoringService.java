package com.example.Interview.resume;

import com.example.Interview.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeScoringService {

    private final ChatClient chatClient;

    private static final String SCORING_PROMPT = """
            You are an ATS (Applicant Tracking System) and technical resume reviewer
            for a college campus placement platform. Evaluate the resume text below.

            Score strictly out of 100 based on: formatting/parseability for ATS systems,
            clarity of impact (quantified achievements vs vague duties), relevant technical
            skills presentation, and completeness (contact info, education, projects, skills).

            Give concise, actionable feedback the student can act on immediately —
            specific lines to fix, not generic advice like "add more detail".

            Resume text:
            ---
            {resumeText}
            ---
            """;

    public ScoringResult score(String resumeText) {
        try {
            ScoringResult result = chatClient
                    .prompt()
                    .user(u -> u.text(SCORING_PROMPT).param("resumeText", resumeText))
                    .call()
                    .entity(ScoringResult.class);

            if (result == null) {
                throw new ApiException("Resume scoring returned no result, please try again", HttpStatus.SERVICE_UNAVAILABLE);
            }
            // Defensive clamp in case the model drifts outside 0-100
            double clamped = Math.max(0, Math.min(100, result.atsScore()));
            return new ScoringResult(clamped, result.feedback());

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Resume scoring call failed", e);
            throw new ApiException("Resume scoring is temporarily unavailable, please try again shortly", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public record ScoringResult(double atsScore, String feedback) {}
}