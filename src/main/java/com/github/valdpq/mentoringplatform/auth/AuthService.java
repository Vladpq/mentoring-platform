package com.github.valdpq.mentoringplatform.auth;

import com.github.valdpq.mentoringplatform.auth.dto.AuthResponse;
import com.github.valdpq.mentoringplatform.auth.dto.LoginRequest;
import com.github.valdpq.mentoringplatform.auth.dto.RegisterRequest;
import com.github.valdpq.mentoringplatform.user.Role;
import com.github.valdpq.mentoringplatform.user.User;
import com.github.valdpq.mentoringplatform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .role(Role.valueOf(registerRequest.role().name()))
                .build();
        userRepository.save(user);

        return new AuthResponse(generateTokenFromUser(user));
    }

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token);
    }

    private String generateTokenFromUser(User user) {
        return jwtService.generateToken(org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                .build());
    }
}
