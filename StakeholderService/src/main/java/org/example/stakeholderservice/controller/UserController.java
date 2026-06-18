package org.example.stakeholderservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.stakeholderservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    
    @GetMapping("/team-members")
    public ResponseEntity<?> getAllTeamMembers() {
        try {
            return ResponseEntity.ok(userService.getAllTeamMembers());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
