package com.dearrudam.user.boundary;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dearrudam.shared.TestDataCleaner;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class CoordinatorListingTest {

    @Inject
    TestDataCleaner cleaner;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(cleaner::clean);
    }

    @Test
    void listsOnlyUsersWithCoordinatorRole() {
        registerCoordinator("coordinator@example.com", "Coordinator One");
        given()
                .contentType("application/json")
                .body(new SpeakerRequest("Speaker Only", "speaker@example.com", "correct horse", "Talks"))
        .when().post("/api/speakers")
        .then().statusCode(201);
        registerSpeakerWithCoordinatorRole("both@example.com", "Both Roles");

        given().when().get("/api/coordinators")
        .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("name", containsInAnyOrder("Coordinator One", "Both Roles"))
                .body("$", everyItem(aMapWithSize(2)));
    }

    @Test
    void excludesAuthenticatedCaller() {
        registerCoordinator("self@example.com", "Self Coordinator");
        registerCoordinator("other@example.com", "Other Coordinator");

        var token = login("self@example.com", "correct horse");

        given()
                .header("Authorization", "Bearer " + token)
        .when().get("/api/coordinators")
        .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("name", containsInAnyOrder("Other Coordinator"));
    }

    private void registerCoordinator(String email, String name) {
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest(name, email, "correct horse"))
        .when().post("/api/coordinators")
        .then().statusCode(201);
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
