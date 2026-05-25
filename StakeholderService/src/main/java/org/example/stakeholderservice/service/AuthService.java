package org.example.stakeholderservice.service;

import org.example.stakeholderservice.dto.AuthResponseDTO;
import org.example.stakeholderservice.dto.LoginRequestDTO;
import org.example.stakeholderservice.dto.RegisterRequestDTO;

public interface AuthService {

    AuthResponseDTO register(RegisterRequestDTO request);

    AuthResponseDTO login(LoginRequestDTO request);
}
