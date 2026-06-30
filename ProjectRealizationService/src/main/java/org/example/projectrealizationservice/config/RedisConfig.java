package org.example.projectrealizationservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/*
 * Redis caching disabled — all cache annotations are commented out in services.
 *
@Configuration
@Profile("!test")
public class RedisConfig {

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private int redisPort;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    public RedisCacheManager cacheManager(LettuceConnectionFactory redisConnectionFactory) { ... }
}
*/

@Configuration
@Profile("!test")
public class RedisConfig {
}
