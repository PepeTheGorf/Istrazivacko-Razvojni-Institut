package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.ProjectTaskDTO;
import org.example.projectrealizationservice.model.*;
import org.example.projectrealizationservice.repository.ProjectRepository;
import org.example.projectrealizationservice.repository.TaskRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
    
    @GetMapping
    public String test() {
        return "Test endpoint is working!";
    }
}
