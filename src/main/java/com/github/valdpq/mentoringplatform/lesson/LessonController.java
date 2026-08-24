package com.github.valdpq.mentoringplatform.lesson;

import com.github.valdpq.mentoringplatform.lesson.dto.CreateLessonRequest;
import com.github.valdpq.mentoringplatform.lesson.dto.LessonResponse;
import com.github.valdpq.mentoringplatform.lesson.dto.UpdateLessonStatusRequest;
import com.github.valdpq.mentoringplatform.review.ReviewService;
import com.github.valdpq.mentoringplatform.review.dto.CreateReviewRequest;
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
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<LessonResponse> bookLesson(@Valid @RequestBody CreateLessonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.bookLesson(request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('STUDENT', 'MENTOR')")
    public ResponseEntity<Page<LessonResponse>> getMyLessons(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.getMyLessons(pageable));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<LessonResponse> changeLessonStatus(@PathVariable Long id,
                                                             @Valid @RequestBody UpdateLessonStatusRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.changeLessonStatus(id, request));
    }

    @PostMapping("/{lessonId}/review")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ReviewResponse> createReview(@PathVariable Long lessonId,
                                                       @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(lessonId, request));
    }
}
