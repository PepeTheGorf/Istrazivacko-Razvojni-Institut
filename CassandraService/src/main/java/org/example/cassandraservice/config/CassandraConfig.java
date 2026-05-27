package org.example.cassandraservice.config;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
public class CassandraConfig {

    @Value("${spring.cassandra.contact-points:cassandra:9042}")
    private String contactPoints;

    @Value("${spring.cassandra.keyspace-name:prompt_analytics}")
    private String keyspaceName;

    @Value("${spring.cassandra.local-datacenter:datacenter1}")
    private String localDatacenter;

    @Bean
    public CqlSession cqlSession() {
        String[] parts = contactPoints.split(":");
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9042;

        return CqlSession.builder()
                .addContactPoint(new InetSocketAddress(host, port))
                .withLocalDatacenter(localDatacenter)
                .withKeyspace(keyspaceName)
                .build();
    }
}
