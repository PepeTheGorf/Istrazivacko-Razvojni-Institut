package org.example.elasticsearchservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ElasticsearchServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchServiceApplication.class, args);
    }
}
