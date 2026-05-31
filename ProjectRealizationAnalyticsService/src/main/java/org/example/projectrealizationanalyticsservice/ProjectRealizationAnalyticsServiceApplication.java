package org.example.projectrealizationanalyticsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ProjectRealizationAnalyticsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectRealizationAnalyticsServiceApplication.class, args);
	}

}
