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
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class SpeakerListingTest {

    @Inject
    TestDataCleaner cleaner;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(cleaner::clean);
    }

    @Test
    @UserRequirement({R3_1, R3_3, R3_4})
    void listsOnlyUsersWithSpeakerRole() {
        given()
                .contentType("application/json")
                .body(new SpeakerRequest("Speaker One", "speaker@example.com", "correct horse", "Talks"))
        .when().post("/api/speakers")
        .then().statusCode(201);
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("Coordinator Only", "coordinator@example.com", "correct horse"))
        .when().post("/api/coordinators")
        .then().statusCode(201);
        registerSpeakerWithCoordinatorRole("both@example.com", "Both Roles");

        given().when().get("/api/speakers")
        .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("name", contains("Both Roles", "Speaker One"))
                .body("$", everyItem(aMapWithSize(2)));
    }

    @Test
    @UserRequirement(R3_2)
    void excludesAuthenticatedCaller() {
        given()
                .contentType("application/json")
                .body(new SpeakerRequest("Self Speaker", "self@example.com", "correct horse", "Bio"))
        .when().post("/api/speakers")
        .then().statusCode(201);
        given()
                .contentType("application/json")
                .body(new SpeakerRequest("Other Speaker", "other@example.com", "correct horse", "Bio"))
        .when().post("/api/speakers")
        .then().statusCode(201);

        var token = login("self@example.com", "correct horse");

        given()
                .header("Authorization", "Bearer " + token)
        .when().get("/api/speakers")
        .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("name", containsInAnyOrder("Other Speaker"));
    }

    private void registerSpeakerWithCoordinatorRole(String email, String name) {
        given()
                .contentType("application/json")
                .body(new SpeakerRequest(name, email, "correct horse", "Speaker bio"))
        .when().post("/api/speakers")
        .then().statusCode(201);
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest(name, email, "correct horse"))
        .when().post("/api/coordinators")
        .then().statusCode(200);
    }

    private String login(String email, String password) {
        return given()
                .contentType("application/json")
                .body(new LoginRequest(email, password))
        .when().post("/api/auth/login")
        .then().statusCode(200)
                .extract().path("token");
    }
}
