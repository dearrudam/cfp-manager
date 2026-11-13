package com.dearrudam.user.boundary;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dearrudam.user.entity.UserRepository;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class CoordinatorResourceTest {

    @Inject
    UserRepository users;

    @Inject
    EntityManager em;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(() -> users.listAll().forEach(em::remove));
    }

    @Test
    void registersCoordinator() {
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("Ada Lovelace", "ada@example.com", "correct horse"))
        .when().post("/api/coordinators")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("Ada Lovelace"))
                .body("email", is("ada@example.com"))
                .body("roles", contains("COORDINATOR"))
                .body("createdAt", notNullValue());
    }

    @Test
    void rejectsDuplicateEmail() {
        var request = new CoordinatorRequest("Ada Lovelace", "ada@example.com", "correct horse");
        given()
                .contentType("application/json")
                .body(request)
        .when().post("/api/coordinators")
        .then().statusCode(201);

        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("Ada Byron", "ada@example.com", "another pass"))
        .when().post("/api/coordinators")
        .then()
                .statusCode(409)
                .body("error", notNullValue())
                .body("field", is("email"));
    }

    @Test
    void rejectsInvalidInput() {
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("Ada Lovelace", "not-an-email", "short"))
        .when().post("/api/coordinators")
        .then()
                .statusCode(400)
                .body("error", notNullValue())
                .body("field", notNullValue());
    }
}
