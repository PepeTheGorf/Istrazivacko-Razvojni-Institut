package org.example.stakeholderservice.service;

import org.example.stakeholderservice.dto.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllTeamMembers();

    UserDTO getUserById(Long userId);
}
