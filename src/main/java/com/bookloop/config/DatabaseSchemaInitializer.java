package com.bookloop.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DatabaseSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN avatar TYPE TEXT;");
            log.info("Database migration: users.avatar column type verified as TEXT");
        } catch (Exception e) {
            log.debug("Note on users.avatar migration: {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE conversations ALTER COLUMN current_user_avatar TYPE TEXT;");
            jdbcTemplate.execute("ALTER TABLE conversations ALTER COLUMN other_user_avatar TYPE TEXT;");
            log.info("Database migration: conversations avatar columns verified as TEXT");
        } catch (Exception e) {
            log.debug("Note on conversations avatar migration: {}", e.getMessage());
        }
    }
}
