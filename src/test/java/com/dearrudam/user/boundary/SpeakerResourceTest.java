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
class SpeakerResourceTest {

    @Inject
    TestDataCleaner cleaner;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(cleaner::clean);
    }

    @Test
    @UserRequirement(R1_1)
    void registersSpeaker() {
        given()
                .contentType("application/json")
                .body(new SpeakerRequest("Alan Turing", "alan@example.com", "correct horse", "Father of CS"))
        .when().post("/api/speakers")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("Alan Turing"))
                .body("email", is("alan@example.com"))
                .body("roles", containsInAnyOrder("SPEAKER"))
                .body("bio", is("Father of CS"))
                .body("createdAt", notNullValue());
    }

    @Test
    @UserRequirement({R1_2, R1_3})
    void grantsRoleOnlyWhenPasswordMatches() {
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("Alan Turing", "alan@example.com", "correct horse"))
        .when().post("/api/coordinators")
        .then().statusCode(201);

        given()
                .contentType("application/json")
                .body(new SpeakerRequest("Alan Turing", "alan@example.com", "correct horse", "Father of CS"))
        .when().post("/api/speakers")
        .then()
                .statusCode(200)
                .body("roles", containsInAnyOrder("COORDINATOR", "SPEAKER"))
                .body("bio", is("Father of CS"));

        given()
                .contentType("application/json")
                .body(new SpeakerRequest("Alan Turing", "alan@example.com", "wrong password", "New bio"))
        .when().post("/api/speakers")
        .then()
                .statusCode(401)
                .body("error", notNullValue())
                .body("field", is("password"));
    }

    @Test
    @UserRequirement(R1_4)
    void rejectsInvalidInput() {
        given()
                .contentType("application/json")
                .body(new SpeakerRequest("Alan Turing", "not-an-email", "short", null))
        .when().post("/api/speakers")
        .then()
                .statusCode(400)
                .body("error", notNullValue())
                .body("field", notNullValue());
    }
}
