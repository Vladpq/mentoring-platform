package com.github.valdpq.mentoringplatform.mentor;

import com.github.valdpq.mentoringplatform.mentor.dto.MentorResponse;
import com.github.valdpq.mentoringplatform.mentor.dto.UpdateMentorProfileRequest;
import com.github.valdpq.mentoringplatform.user.User;
import com.github.valdpq.mentoringplatform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentorService {

    private final MentorRepository mentorRepository;
    private final UserRepository userRepository;

    public Page<MentorResponse> getMentors(String specialization, Pageable pageable) {

        Page<Mentor> mentors = specialization == null
                ? mentorRepository.findAll(pageable)
                : mentorRepository.findAllBySpecializationContainingIgnoreCase(specialization, pageable);

        return mentors.map(this::toMentorResponse);
    }

    @Transactional
    public MentorResponse updateMentorProfile(UpdateMentorProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("No authenticated user found in context");
        }

        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Mentor mentor = mentorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new MentorNotFoundException("Mentor not found"));

        if (request.bio() != null) {
            mentor.setBio(request.bio());
        }

        if (request.specialization() != null) {
            mentor.setSpecialization(request.specialization());
        }

        if (request.pricePerHour() != null) {
            mentor.setPricePerHour(request.pricePerHour());
        }

        mentorRepository.save(mentor);

        return this.toMentorResponse(mentor);
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
