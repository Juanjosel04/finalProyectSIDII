package com.uniplan.uniplan_backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.Executor;

@Configuration
@EnableScheduling
@EnableAsync
public class AppConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("stats-sync-");
        executor.initialize();
        return executor;
    }

    /**
     * Ensures the event_statistics table has the correct schema.
     * Checks whether the primary key column (event_id) exists; if the table
     * has a stale schema it drops and recreates it safely (data is regenerated
     * by the startup sync, so no permanent data loss).
     */
    @Bean
    public CommandLineRunner createEventStatisticsTable(DataSource dataSource) {
        return args -> {
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {

                // Check if the table has the correct primary key column.
                // Use a separate connection for the probe so any failure does not
                // poison the main connection's transaction state.
                boolean needsDrop = false;
                try (Connection probe = dataSource.getConnection()) {
                    probe.setAutoCommit(true);
                    try (Statement ps = probe.createStatement()) {
                        ps.executeQuery("SELECT event_id FROM uniplan.event_statistics LIMIT 0");
                    } catch (Exception e) {
                        needsDrop = true; // column missing → stale schema
                    }
                } catch (Exception ignored) {}

                if (needsDrop) {
                    System.out.println("[AppConfig] event_statistics has stale schema — recreating.");
                    conn.setAutoCommit(true);
                    stmt.execute("DROP TABLE IF EXISTS uniplan.event_statistics");
                }

                conn.setAutoCommit(true);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS uniplan.event_statistics (
                        event_id             VARCHAR(255) PRIMARY KEY,
                        event_code           VARCHAR(100),
                        event_title          VARCHAR(500),
                        event_type           VARCHAR(100),
                        event_status         VARCHAR(50),
                        total_capacity       INTEGER,
                        registered           INTEGER NOT NULL DEFAULT 0,
                        cancelled            INTEGER NOT NULL DEFAULT 0,
                        attended             INTEGER NOT NULL DEFAULT 0,
                        waitlist             INTEGER NOT NULL DEFAULT 0,
                        occupancy_percentage DOUBLE PRECISION DEFAULT 0,
                        attendance_rate      DOUBLE PRECISION DEFAULT 0,
                        organizer_email      VARCHAR(255),
                        event_start_date     TIMESTAMP,
                        last_synced_at       TIMESTAMP
                    )
                    """);

            } catch (Exception e) {
                System.err.println("[AppConfig] Could not create event_statistics table: " + e.getMessage());
            }
        };
    }
}
