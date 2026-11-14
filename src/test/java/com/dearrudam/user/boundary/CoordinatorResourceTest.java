package com.dearrudam.user.boundary;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dearrudam.shared.TestDataCleaner;
import com.dearrudam.user.UserRequirement;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

import static com.dearrudam.user.UserRequirement.Rn.*;
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
    @UserRequirement(R2_1)
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
    @UserRequirement({R2_2, R2_3})
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
    @UserRequirement(R2_4)
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

    @Test
    @UserRequirement({R1_5, R2_5})
    void normalizesEmail() {
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("Grace Hopper", "Grace@Example.COM", "correct horse"))
        .when().post("/api/coordinators")
        .then()
                .statusCode(201)
                .body("email", is("grace@example.com"));

        given()
                .contentType("application/json")
                .body(new SpeakerRequest("Alan Turing", "ALAN@Example.com", "correct horse", null))
        .when().post("/api/speakers")
        .then()
                .statusCode(201)
                .body("email", is("alan@example.com"));
    }
}
