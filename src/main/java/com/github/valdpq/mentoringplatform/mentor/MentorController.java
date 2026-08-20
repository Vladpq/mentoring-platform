package com.github.valdpq.mentoringplatform.mentor;

import com.github.valdpq.mentoringplatform.mentor.dto.MentorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;

    @GetMapping
    public ResponseEntity<Page<MentorResponse>> getMentors(
            @RequestParam(required = false) String specialization,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(mentorService.getMentors(specialization, pageable));
    }
}
