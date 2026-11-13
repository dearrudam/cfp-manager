package com.dearrudam.event.boundary;

import java.time.Instant;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dearrudam.shared.TestDataCleaner;
import com.dearrudam.user.boundary.CoordinatorRequest;
import com.dearrudam.user.boundary.LoginRequest;
import com.dearrudam.user.boundary.SpeakerRequest;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class EventResourceTest {

    @Inject
    TestDataCleaner cleaner;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(cleaner::clean);
    }

    @Test
    void createsEventForCoordinator() {
        var ownerId = given()
                .contentType("application/json")
                .body(new CoordinatorRequest("Grace Hopper", "grace@example.com", "correct horse"))
        .when().post("/api/coordinators")
        .then().statusCode(201)
                .extract().path("id");

        var token = login("grace@example.com", "correct horse");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new EventRequest(
                        "Devoxx",
                        "Java conference",
                        Instant.parse("2026-06-01T10:00:00Z"),
                        Instant.parse("2026-06-03T18:00:00Z")))
        .when().post("/api/events")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("Devoxx"))
                .body("description", is("Java conference"))
                .body("startAt", is("2026-06-01T10:00:00Z"))
                .body("endAt", is("2026-06-03T18:00:00Z"))
                .body("ownerId", is(ownerId))
                .body("ownerName", is("Grace Hopper"))
                .body("createdAt", notNullValue());
    }

    @Test
    void listsEventsForVisitors() {
        var eventId = createEvent();

        given()
        .when().get("/api/events")
        .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", is(eventId))
                .body("[0].name", is("Devoxx"))
                .body("[0].ownerName", is("Grace Hopper"));
    }

    @Test
    void getsEventForVisitors() {
        var eventId = createEvent();

        given()
        .when().get("/api/events/" + eventId)
        .then()
                .statusCode(200)
                .body("id", is(eventId))
                .body("name", is("Devoxx"))
                .body("description", is("Java conference"))
                .body("startAt", is("2026-06-01T10:00:00Z"))
                .body("endAt", is("2026-06-03T18:00:00Z"))
                .body("ownerName", is("Grace Hopper"))
                .body("createdAt", notNullValue());

        given()
        .when().get("/api/events/999999")
        .then()
                .statusCode(404)
                .body("error", is("Event not found"));
    }

    @Test
    void rejectsNonCoordinators() {
        given()
                .contentType("application/json")
                .body(validEvent())
        .when().post("/api/events")
        .then().statusCode(401);

        given()
                .contentType("application/json")
                .body(new SpeakerRequest("Alan Turing", "alan@example.com", "correct horse", "Bio"))
        .when().post("/api/speakers")
        .then().statusCode(201);

        var token = login("alan@example.com", "correct horse");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(validEvent())
        .when().post("/api/events")
        .then().statusCode(403);
    }

    @Test
    void rejectsInvalidInput() {
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("Grace Hopper", "grace@example.com", "correct horse"))
        .when().post("/api/coordinators")
        .then().statusCode(201);

        var token = login("grace@example.com", "correct horse");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new EventRequest("", "desc",
                        Instant.parse("2026-06-01T10:00:00Z"),
                        Instant.parse("2026-06-03T18:00:00Z")))
        .when().post("/api/events")
        .then().statusCode(400);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new EventRequest("Devoxx", "desc",
                        Instant.parse("2026-06-03T18:00:00Z"),
                        Instant.parse("2026-06-01T10:00:00Z")))
        .when().post("/api/events")
        .then()
                .statusCode(400)
                .body("error", is("endAt must be after startAt"));
    }

    private Integer createEvent() {
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("Grace Hopper", "grace@example.com", "correct horse"))
        .when().post("/api/coordinators")
        .then().statusCode(201);

        var token = login("grace@example.com", "correct horse");

        return given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(validEvent())
        .when().post("/api/events")
        .then().statusCode(201)
                .extract().path("id");
    }

    private EventRequest validEvent() {
        return new EventRequest(
                "Devoxx",
                "Java conference",
                Instant.parse("2026-06-01T10:00:00Z"),
                Instant.parse("2026-06-03T18:00:00Z"));
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
