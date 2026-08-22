package com.github.valdpq.mentoringplatform.lesson;

import com.github.valdpq.mentoringplatform.lesson.dto.CreateLessonRequest;
import com.github.valdpq.mentoringplatform.lesson.dto.LessonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<LessonResponse> bookLesson(@Valid @RequestBody CreateLessonRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.bookLesson(request));
    }
}
