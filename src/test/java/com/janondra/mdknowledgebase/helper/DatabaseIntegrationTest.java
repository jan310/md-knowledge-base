package com.janondra.mdknowledgebase.helper;

import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@JdbcTest
@Testcontainers
public abstract class DatabaseIntegrationTest {

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:18.3-alpine3.23");

}
