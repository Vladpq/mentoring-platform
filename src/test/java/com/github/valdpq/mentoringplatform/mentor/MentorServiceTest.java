package com.github.valdpq.mentoringplatform.mentor;

import com.github.valdpq.mentoringplatform.mentor.dto.MentorResponse;
import com.github.valdpq.mentoringplatform.mentor.dto.UpdateMentorProfileRequest;
import com.github.valdpq.mentoringplatform.user.Role;
import com.github.valdpq.mentoringplatform.user.User;
import com.github.valdpq.mentoringplatform.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MentorServiceTest {

    @Mock
    private MentorRepository mentorRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MentorService mentorService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getMentors_shouldReturnPagedMentors_whenNoFilterProvided() {

        Pageable pageable = PageRequest.of(0, 10);
        List<Mentor> mentors = List.of(new Mentor());
        Page<Mentor> mentorPage = new PageImpl<>(mentors, pageable, mentors.size());

        when(mentorRepository.findAll(pageable))
                .thenReturn(mentorPage);

        Page<MentorResponse> result = mentorService.getMentors(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(mentorRepository).findAll(pageable);
    }

    @Test
    public void getMentors_shouldReturnFilteredMentors_whenSpecializationProvided() {

        String javaSpecialization = "java";
        Mentor mentor = Mentor.builder()
                .specialization(javaSpecialization)
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        List<Mentor> mentors = List.of(mentor);
        Page<Mentor> mentorPage = new PageImpl<>(mentors, pageable, mentors.size());

        when(mentorRepository.findAllBySpecializationContainingIgnoreCase(javaSpecialization, pageable))
                .thenReturn(mentorPage);

        Page<MentorResponse> result = mentorService.getMentors(javaSpecialization, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(mentorRepository).findAllBySpecializationContainingIgnoreCase(javaSpecialization, pageable);
    }


    @Test
    public void updateMentorProfile_shouldThrowMentorNotFoundException_whenMentorNotFound() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "mentor@test.com", null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .role(Role.MENTOR)
                .build();

        UpdateMentorProfileRequest updateMentorProfileRequest = new UpdateMentorProfileRequest(
                "bio", "specialization", BigDecimal.valueOf(20));

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(mentorRepository.findByUserId(user.getId()))
                .thenReturn(Optional.empty());

        assertThrows(MentorNotFoundException.class, () -> mentorService.updateMentorProfile(updateMentorProfileRequest));
    }

    @Test
    public void updateMentorProfile_shouldUpdateFieldsAndReturnResponse_whenValidRequest() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "mentor@test.com", null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .role(Role.MENTOR)
                .build();
        Mentor mentor = Mentor.builder()
                .id(1L)
                .user(user)
                .build();

        UpdateMentorProfileRequest updateMentorProfileRequest = new UpdateMentorProfileRequest(
                "bio", "specialization", BigDecimal.valueOf(20));

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(mentorRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(mentor));

        MentorResponse response = mentorService.updateMentorProfile(updateMentorProfileRequest);

        assertNotNull(response);
        assertEquals("bio", response.bio());
        assertEquals("specialization", response.specialization());
        assertEquals(BigDecimal.valueOf(20), response.pricePerHour());
        verify(mentorRepository).save(mentor);
    }
}
