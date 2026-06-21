package org.example.projectrealizationservice.config;

import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Configuration
@Profile("!test")
@EnableNeo4jRepositories(
        basePackages = "org.example.projectrealizationservice.saga.graph.repository",
        transactionManagerRef = "neo4jTransactionManager"
)
public class Neo4jConfig {

    @Bean
    public Neo4jTransactionManager neo4jTransactionManager(Driver driver,
                                                           DatabaseSelectionProvider databaseSelectionProvider) {
        return new Neo4jTransactionManager(driver, databaseSelectionProvider);
    }
}
