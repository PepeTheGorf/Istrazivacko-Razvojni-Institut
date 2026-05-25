package org.example.stakeholderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class StakeholderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StakeholderServiceApplication.class, args);
    }
}
