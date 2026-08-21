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
    private final MentorRepository mentorRepository;
    private final StudentRepository studentRepository;

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

        if (registerRequest.role() == RegistrableRole.MENTOR) {
            Mentor mentor = Mentor.builder()
                    .user(user)
                    .firstName(registerRequest.firstName())
                    .lastName(registerRequest.lastName())
                    .build();

            mentorRepository.save(mentor);
        } else {
            Student student = Student.builder()
                    .user(user)
                    .firstName(registerRequest.firstName())
                    .lastName(registerRequest.lastName())
                    .build();

            studentRepository.save(student);
        }

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
