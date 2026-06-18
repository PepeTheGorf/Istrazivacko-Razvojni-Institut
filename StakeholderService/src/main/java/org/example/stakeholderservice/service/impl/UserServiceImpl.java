package org.example.stakeholderservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.stakeholderservice.dto.UserDTO;
import org.example.stakeholderservice.model.Role;
import org.example.stakeholderservice.repository.UserRepository;
import org.example.stakeholderservice.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDTO> getAllTeamMembers() {
        return userRepository.getAllByRole(Role.valueOf("TEAM_MEMBER")).stream()
                .map(UserDTO::toDto)
                .toList();
    }
}
