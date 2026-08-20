package com.github.valdpq.mentoringplatform.mentor;

import com.github.valdpq.mentoringplatform.mentor.dto.MentorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MentorService {

    private final MentorRepository mentorRepository;

    public Page<MentorResponse> getMentors(String specialization, Pageable pageable) {

        Page<Mentor> mentors = specialization == null
                ? mentorRepository.findAll(pageable)
                : mentorRepository.findAllBySpecializationContainingIgnoreCase(specialization, pageable);

        return mentors.map(this::toMentorResponse);
    }

    private MentorResponse toMentorResponse(Mentor mentor) {
        return new MentorResponse(
                mentor.getId(),
                mentor.getFirstName(),
                mentor.getLastName(),
                mentor.getBio(),
                mentor.getSpecialization(),
                mentor.getPricePerHour(),
                mentor.getAvgRating(),
                mentor.getReviewsCount()
        );
    }
}
