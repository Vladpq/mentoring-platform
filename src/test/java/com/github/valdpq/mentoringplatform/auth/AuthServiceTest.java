package com.github.valdpq.mentoringplatform.auth;

import com.github.valdpq.mentoringplatform.auth.dto.AuthResponse;
import com.github.valdpq.mentoringplatform.auth.dto.LoginRequest;
import com.github.valdpq.mentoringplatform.auth.dto.RegisterRequest;
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

    @InjectMocks
    private AuthService authService;

    @Test
    public void register_shouldThrowEmailAlreadyExistsException_whenEmailAlreadyRegistered() {

        when(userRepository.existsByEmail(anyString()))
                .thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(
                new RegisterRequest("email@test.com", "password", RegistrableRole.MENTOR)));
    }


    @Test
    public void register_shouldSaveUserAndReturnToken_whenUserIsRegistered() {

        RegisterRequest registerRequest =
                new RegisterRequest("mentor@test.com", "password", RegistrableRole.MENTOR);

        User mentorUser = User.builder()
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
