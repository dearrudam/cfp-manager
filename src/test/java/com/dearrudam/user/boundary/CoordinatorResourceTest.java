package com.dearrudam.user.boundary;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dearrudam.shared.TestDataCleaner;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class CoordinatorResourceTest {

    @Inject
    TestDataCleaner cleaner;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(cleaner::clean);
    }

    @Test
    void registersCoordinator() {
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("Grace Hopper", "grace@example.com", "correct horse"))
        .when().post("/api/coordinators")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("Grace Hopper"))
                .body("email", is("grace@example.com"))
                .body("roles", containsInAnyOrder("COORDINATOR"))
                .body("createdAt", notNullValue());
    }

    @Test
    void grantsRoleOnlyWhenPasswordMatches() {
        given()
                .contentType("application/json")
                .body(new SpeakerRequest("Grace Hopper", "grace@example.com", "correct horse", null))
        .when().post("/api/speakers")
        .then().statusCode(201);

        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("Grace Hopper", "grace@example.com", "correct horse"))
        .when().post("/api/coordinators")
        .then()
                .statusCode(200)
                .body("roles", containsInAnyOrder("SPEAKER", "COORDINATOR"));

        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("Grace Hopper", "grace@example.com", "wrong password"))
        .when().post("/api/coordinators")
        .then()
                .statusCode(401)
                .body("error", notNullValue())
                .body("field", is("password"));
    }

    @Test
    void rejectsInvalidInput() {
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("Grace Hopper", "not-an-email", "short"))
        .when().post("/api/coordinators")
        .then()
                .statusCode(400)
                .body("error", notNullValue())
                .body("field", notNullValue());
    }
}
