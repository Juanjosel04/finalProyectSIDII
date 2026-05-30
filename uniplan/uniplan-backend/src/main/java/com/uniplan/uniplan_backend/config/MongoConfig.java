package com.uniplan.uniplan_backend.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {
        return "uniplan_mongo";
    }

    @Override
    public MongoClient mongoClient() {

        MongoClientSettings settings = MongoClientSettings.builder()

                .applyConnectionString(new ConnectionString(mongoUri))

                // Pool pequeño para Atlas M0 (free tier)
                .applyToConnectionPoolSettings(pool -> pool
                        .maxSize(5)
                        .minSize(1)
                        .maxWaitTime(10, TimeUnit.SECONDS)
                        .maxConnectionIdleTime(60, TimeUnit.SECONDS)
                        .maxConnectionLifeTime(120, TimeUnit.SECONDS)
                )

                // TLS explícito con settings seguros para Atlas
                .applyToSslSettings(ssl -> ssl
                        .enabled(true)
                        .invalidHostNameAllowed(false)
                )

                // Timeouts razonables
                .applyToSocketSettings(socket -> socket
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                )

                .applyToClusterSettings(cluster -> cluster
                        .serverSelectionTimeout(15, TimeUnit.SECONDS)
                )

                .build();

        return MongoClients.create(settings);
    }

    @Override
    public boolean autoIndexCreation() {
        return false;
    }
}
