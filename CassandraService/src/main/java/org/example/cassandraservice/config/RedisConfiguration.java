package org.example.cassandraservice.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

@Configuration
public class RedisConfiguration {

    // Konfiguracija za povezivanje na Redis server
    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private int redisPort;

    // Kreiranje fabrike konekcija za Redis, odnosno povezivanje sa Redis serverom
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);

        return new LettuceConnectionFactory(configuration);
    }

    // Konfiguracija upravljanja kešom
    @Bean
    public RedisCacheManager cacheManager() {
        // Podrazumevana konfiguracija keša za sve vrednosti koje nemaju specifičnu konfiguraciju
        // Postavljanje životnog veka na 10 mimnuta i onemogućavanje null vrednoti u kešu
        RedisCacheConfiguration cacheConfig = myDefaultCacheConfig(Duration.ofMinutes(10)).disableCachingNullValues();

        // Specifična konfiguracija za keširanje korisinka
        // Naziv keša: "users" -> označava da će sve vrednosti u kešu biti oblika users::ključ
        // Životni vek: 5 minuta
        return RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(cacheConfig)
                .withCacheConfiguration("users", myDefaultCacheConfig(Duration.ofMinutes(5)))
                .build();
    }
    
    // Metoda za konfiguraciju keširanja
    private RedisCacheConfiguration myDefaultCacheConfig(Duration duration) {
        return RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(duration)
                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}