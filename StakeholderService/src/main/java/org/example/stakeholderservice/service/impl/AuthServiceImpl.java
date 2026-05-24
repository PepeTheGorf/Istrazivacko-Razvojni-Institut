package org.example.stakeholderservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.stakeholderservice.dto.AuthResponseDTO;
import org.example.stakeholderservice.dto.LoginRequestDTO;
import org.example.stakeholderservice.dto.RegisterRequestDTO;
import org.example.stakeholderservice.exception.EmailAlreadyExistsException;
import org.example.stakeholderservice.model.User;
import org.example.stakeholderservice.repository.UserRepository;
import org.example.stakeholderservice.security.JwtService;
import org.example.stakeholderservice.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .createdAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getId(), saved.getEmail(), saved.getRole());

        return toAuthResponse(saved, token);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        return toAuthResponse(user, token);
    }

    private AuthResponseDTO toAuthResponse(User user, String token) {
        return AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
