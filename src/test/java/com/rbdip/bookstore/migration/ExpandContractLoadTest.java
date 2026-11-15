package com.rbdip.bookstore.migration;

import static org.assertj.core.api.Assertions.assertThat;

import com.rbdip.bookstore.order.CreateOrderRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.flyway.target=4")
class ExpandContractLoadTest {

    private static final int WRITERS = 4;
    private static final long TRAFFIC_TIMEOUT_SECONDS = 10;

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bookstore_migration_load")
            .withUsername("bookstore")
            .withPassword("bookstore");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Test
    void apiServesRequestsWhileContractMigrationDropsFullName() throws Exception {
        Long productId = createProduct();
        CountDownLatch firstRequest = new CountDownLatch(1);
        AtomicBoolean running = new AtomicBoolean(true);
        List<Exception> errors = new CopyOnWriteArrayList<>();
        AtomicInteger completedRequests = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(WRITERS);

        for (int i = 0; i < WRITERS; i++) {
            String firstName = "Ivan" + i;
            pool.submit(() -> runTraffic(productId, firstName, running, firstRequest, errors, completedRequests));
        }

        assertThat(firstRequest.await(TRAFFIC_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                .as("API должен принимать запросы до начала contract-миграции")
                .isTrue();

        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();

        running.set(false);
        pool.shutdown();
        assertThat(pool.awaitTermination(TRAFFIC_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                .as("Нагрузочные потоки должны завершиться")
                .isTrue();

        assertThat(errors).as("Ни один запрос к API не должен упасть во время миграции").isEmpty();
        assertThat(completedRequests.get()).as("Нагрузка должна была реально пройти").isPositive();

        List<String> columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns"
                        + " WHERE table_schema = 'public' AND table_name = 'customers'",
                String.class);
        assertThat(columns).doesNotContain("full_name").contains("first_name", "last_name");

        ResponseEntity<List> listed = restTemplate.getForEntity("/orders", List.class);
        assertThat(listed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listed.getBody()).isNotEmpty();
    }

    private void runTraffic(
            Long productId,
            String firstName,
            AtomicBoolean running,
            CountDownLatch firstRequest,
            List<Exception> errors,
            AtomicInteger completedRequests) {
        while (running.get()) {
            try {
                sendOrder(productId, firstName);
                sendList();
                completedRequests.incrementAndGet();
                firstRequest.countDown();
            } catch (Exception e) {
                errors.add(e);
            }
        }
    }

    private void sendOrder(Long productId, String firstName) {
        CreateOrderRequest request = new CreateOrderRequest(
                firstName + " Petrov",
                "Moscow, Lenina 1",
                "+79990000000",
                "regular",
                null,
                List.of(new CreateOrderRequest.Item(productId, 1)));
        ResponseEntity<Map> response = restTemplate.postForEntity("/orders", request, Map.class);
        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new IllegalStateException("POST /orders returned " + response.getStatusCode());
        }
    }

    private void sendList() {
        ResponseEntity<List> response = restTemplate.getForEntity("/orders", List.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new IllegalStateException("GET /orders returned " + response.getStatusCode());
        }
    }

    private Long createProduct() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/products", Map.of("name", "Clean Code", "price", new BigDecimal("35.00")), Map.class);
        return Long.valueOf(response.getBody().get("id").toString());
    }
}
