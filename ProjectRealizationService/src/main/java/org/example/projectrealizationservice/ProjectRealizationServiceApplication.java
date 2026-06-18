package org.example.projectrealizationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ProjectRealizationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectRealizationServiceApplication.class, args);
    }
}
