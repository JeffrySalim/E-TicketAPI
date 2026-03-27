package com.test.ticket.service;

import com.test.ticket.config.JwtUtil;
import com.test.ticket.dto.Auth.AuthRequest;
import com.test.ticket.dto.Auth.LoginResponse;
import com.test.ticket.entity.Enum.Role;
import com.test.ticket.entity.User;
import com.test.ticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void register(AuthRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new IllegalArgumentException("Email sudah terdaftar!");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    public LoginResponse login(AuthRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email atau password salah"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Email atau password salah");
        }

        String token = jwtUtil.generateToken(user);

        String rawRole = user.getRole().name();

        String cleanRole = rawRole.replace("ROLE_", "");

        return new LoginResponse(token,cleanRole);
    }
}
