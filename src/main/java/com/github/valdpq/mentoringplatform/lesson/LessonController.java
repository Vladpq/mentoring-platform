package com.github.valdpq.mentoringplatform.lesson;

import com.github.valdpq.mentoringplatform.lesson.dto.CreateLessonRequest;
import com.github.valdpq.mentoringplatform.lesson.dto.LessonResponse;
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
}
