package org.example.elasticsearchservice.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.http.HttpHost;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.rest.uris:http://localhost:9200}")
    private String elasticsearchUri;

    @Bean
    public RestClient restClient() {
        String host = extractHost(elasticsearchUri);
        int port = extractPort(elasticsearchUri);
        
        return RestClient.builder(
                new HttpHost(host, port, "http")
        ).build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }

    private String extractHost(String uri) {
        try {
            String trimmed = uri.replaceAll("^https?://", "").replaceAll("/.*$", "");
            return trimmed.split(":")[0];
        } catch (Exception e) {
            return "localhost";
        }
    }

    private int extractPort(String uri) {
        try {
            String trimmed = uri.replaceAll("^https?://", "").replaceAll("/.*$", "");
            if (trimmed.contains(":")) {
                return Integer.parseInt(trimmed.split(":")[1]);
            }
        } catch (Exception e) {
            System.err.println("Error parsing port from URI: " + e.getMessage());
        }
        return 9200;
    }
}
