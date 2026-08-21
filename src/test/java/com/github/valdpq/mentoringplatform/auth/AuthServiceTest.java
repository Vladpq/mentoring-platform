package com.github.valdpq.mentoringplatform.auth;

import com.github.valdpq.mentoringplatform.auth.dto.AuthResponse;
import com.github.valdpq.mentoringplatform.auth.dto.LoginRequest;
import com.github.valdpq.mentoringplatform.auth.dto.RegisterRequest;
import com.github.valdpq.mentoringplatform.mentor.Mentor;
import com.github.valdpq.mentoringplatform.mentor.MentorRepository;
import com.github.valdpq.mentoringplatform.student.Student;
import com.github.valdpq.mentoringplatform.student.StudentRepository;
import com.github.valdpq.mentoringplatform.user.Role;
import com.github.valdpq.mentoringplatform.user.User;
import com.github.valdpq.mentoringplatform.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private MentorRepository mentorRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    public void register_shouldThrowEmailAlreadyExistsException_whenEmailAlreadyRegistered() {

        when(userRepository.existsByEmail(anyString()))
                .thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(
                new RegisterRequest("email@test.com", "password",
                        "test", "mentor", RegistrableRole.MENTOR)));
    }


    @Test
    public void register_shouldSaveMentorProfileAndReturnToken_whenRoleIsMentor() {

        RegisterRequest registerRequest =
                new RegisterRequest("mentor@test.com", "password",
                        "test", "mentor", RegistrableRole.MENTOR);

        User mentorUser = User.builder()
                .id(1L)
                .email(registerRequest.email())
                .password(registerRequest.password())
                .role(Role.MENTOR)
                .build();

        when(userRepository.existsByEmail(registerRequest.email()))
                .thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenReturn(mentorUser);
        when(jwtService.generateToken(any()))
                .thenReturn("test token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("test token", response.token());
        verify(userRepository).save(any(User.class));
        verify(mentorRepository).save(any(Mentor.class));
        verify(studentRepository, never()).save(any());
    }

    @Test
    public void register_shouldSaveStudentProfileAndReturnToken_whenRoleIsStudent() {

        RegisterRequest registerRequest =
                new RegisterRequest("student@test.com", "password",
                        "test", "student", RegistrableRole.STUDENT);

        User studentUser = User.builder()
                .id(1L)
                .email(registerRequest.email())
                .password(registerRequest.password())
                .role(Role.STUDENT)
                .build();

        when(userRepository.existsByEmail(registerRequest.email()))
                .thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenReturn(studentUser);
        when(jwtService.generateToken(any()))
                .thenReturn("test token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("test token", response.token());
        verify(userRepository).save(any(User.class));
        verify(studentRepository).save(any(Student.class));
        verify(mentorRepository, never()).save(any());
    }

    @Test
    public void login_shouldReturnToken_whenLoggedIn() {

        LoginRequest loginRequest = new LoginRequest("email@test.com", "password");

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal())
                .thenReturn(userDetails);
        when(jwtService.generateToken(userDetails))
                .thenReturn("test token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test token", response.token());

    }

    @Test
    public void login_shouldThrowBadCredentialsException_whenInvalidEmailOrPassword() {
        LoginRequest loginRequest = new LoginRequest("email@test.com", "password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }
}
