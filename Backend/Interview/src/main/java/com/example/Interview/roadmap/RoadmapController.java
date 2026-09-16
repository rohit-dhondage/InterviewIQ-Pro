package com.example.Interview.roadmap;

import com.example.Interview.auth.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roadmap/me")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;

    @GetMapping
    public List<RoadmapItem> getRoadmap(@AuthenticationPrincipal User user) {
        return roadmapService.getRoadmap(user);
    }

    @PostMapping("/generate")
    public List<RoadmapItem> generateRoadmap(@AuthenticationPrincipal User user) {
        return roadmapService.generateRoadmap(user);
    }

    @PatchMapping("/{itemId}/status")
    public RoadmapItem updateStatus(@AuthenticationPrincipal User user, 
                                    @PathVariable Long itemId, 
                                    @RequestParam RoadmapItem.Status status) {
        return roadmapService.updateStatus(user, itemId, status);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@AuthenticationPrincipal User user, @PathVariable Long itemId) {
        roadmapService.deleteItem(user, itemId);
    }
}
