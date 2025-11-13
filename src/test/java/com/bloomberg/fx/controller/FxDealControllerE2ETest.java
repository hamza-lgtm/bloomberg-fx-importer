package com.bloomberg.fx.controller;

// ... imports ...
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import com.bloomberg.fx.dto.FxDealRequestDTO;
import com.bloomberg.fx.repository.FxDealRepository;



import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class FxDealControllerE2ETest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private FxDealRepository repository;

    @Container
    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        repository.deleteAll(); // Clean DB before each test
    }

    @Test
    void testImportSingleSuccess() {
        FxDealRequestDTO deal = createValidDTO("id-1");

        given()
                .contentType(ContentType.JSON)
                .body(List.of(deal))
        .when()
                .post("/api/v1/deals/import")
        .then()
                .statusCode(201)
                .body("successCount", is(1))
                .body("failureCount", is(0));

        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void testImportDeduplicationFailure() {
        // Step 1: Import a deal successfully
        FxDealRequestDTO deal1 = createValidDTO("id-100");
        given().contentType(ContentType.JSON).body(List.of(deal1)).post("/api/v1/deals/import");
        assertThat(repository.count()).isEqualTo(1);

        // Step 2: Try to import the *same deal* again
        given()
                .contentType(ContentType.JSON)
                .body(List.of(deal1))
        .when()
                .post("/api/v1/deals/import")
        .then()
                .statusCode(201) // API call is successful (partial success)
                .body("successCount", is(0))
                .body("failureCount", is(1))
                .body("failures[0].dealId", is("id-100"))
                // **** THIS IS THE CORRECTED LINE ****
                .body("failures[0].error", containsString("Duplicate deal"));
        
        // Verify database still only has the one original deal
        assertThat(repository.count()).isEqualTo(1);
    }
    @Test
    void testImportValidationFailure() {
        FxDealRequestDTO invalidDeal = createValidDTO("id-200");
        invalidDeal.setFromCurrency("INVALID"); // Fails @Size(min=3, max=3)

        given()
                .contentType(ContentType.JSON)
                .body(List.of(invalidDeal))
        .when()
                .post("/api/v1/deals/import")
        .then()
                .statusCode(201)
                .body("successCount", is(0))
                .body("failureCount", is(1))
                .body("failures[0].dealId", is("id-200"))
                .body("failures[0].error", containsString("From Currency ISO Code must be 3 characters"));

        assertThat(repository.count()).isZero();
    }

    @Test
    void testImportNoRollbackPartialSuccess() {
        FxDealRequestDTO validDeal1 = createValidDTO("id-301");
        
        FxDealRequestDTO invalidDeal = createValidDTO("id-302");
        invalidDeal.setDealAmount(BigDecimal.valueOf(-100)); // Fails @Positive
        
        FxDealRequestDTO validDeal2 = createValidDTO("id-303");

        List<FxDealRequestDTO> deals = List.of(validDeal1, invalidDeal, validDeal2);
        
        given()
                .contentType(ContentType.JSON)
                .body(deals)
        .when()
                .post("/api/v1/deals/import")
        .then()
                .statusCode(201)
                .body("successCount", is(2))
                .body("failureCount", is(1))
                .body("failures[0].dealId", is("id-302"));

        // Crucial Assertion: Check that the two valid deals were saved
        assertThat(repository.count()).isEqualTo(2);
        assertThat(repository.findById("id-301")).isPresent();
        assertThat(repository.findById("id-303")).isPresent();
        assertThat(repository.findById("id-302")).isNotPresent();
    }
    
    // Helper method
    private FxDealRequestDTO createValidDTO(String id) {
        FxDealRequestDTO dto = new FxDealRequestDTO();
        dto.setDealUniqueId(id);
        dto.setFromCurrency("USD");
        dto.setToCurrency("EUR");
        dto.setDealTimestamp(Instant.now().minusSeconds(60));
        dto.setDealAmount(BigDecimal.valueOf(1000.50));
        return dto;
    }
}