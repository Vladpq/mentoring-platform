package com.github.valdpq.mentoringplatform.mentor;

import com.github.valdpq.mentoringplatform.mentor.dto.MentorResponse;
import com.github.valdpq.mentoringplatform.mentor.dto.UpdateMentorProfileRequest;
import com.github.valdpq.mentoringplatform.review.ReviewService;
import com.github.valdpq.mentoringplatform.review.dto.ReviewResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<Page<MentorResponse>> getMentors(
            @RequestParam(required = false) String specialization,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(mentorService.getMentors(specialization, pageable));
    }

    @PreAuthorize("hasRole('MENTOR')")
    @PatchMapping("/my")
    public ResponseEntity<MentorResponse> updateMentorProfile(@RequestBody @Valid UpdateMentorProfileRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(mentorService.updateMentorProfile(request));
    }

    @GetMapping("/{mentorId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getMentorReviews(@PathVariable Long mentorId, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.getMentorReviews(mentorId, pageable));
    }
}
