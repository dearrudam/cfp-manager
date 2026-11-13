package com.dearrudam.user.boundary;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dearrudam.user.entity.UserRepository;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class SpeakerListingTest {

    @Inject
    UserRepository users;

    @Inject
    EntityManager em;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(() -> users.listAll().forEach(em::remove));
    }

    @Test
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
                .body("name", containsInAnyOrder("Speaker One", "Both Roles"))
                .body("$", everyItem(aMapWithSize(2)));
    }

    @Test
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
