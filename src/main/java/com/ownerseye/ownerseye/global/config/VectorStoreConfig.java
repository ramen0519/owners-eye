package com.ownerseye.ownerseye.global.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class VectorStoreConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        JedisPooled jedisPooled = new JedisPooled(redisHost, redisPort);
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName("owners-eye-index")
                .initializeSchema(true)
                .build();
    }
}
