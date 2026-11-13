package com.dearrudam.cfp.boundary;

import java.time.Instant;
import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dearrudam.event.boundary.EventRequest;
import com.dearrudam.user.boundary.CoordinatorRequest;
import com.dearrudam.user.boundary.LoginRequest;
import com.dearrudam.user.boundary.SpeakerRequest;
import com.dearrudam.shared.TestDataCleaner;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class CfpResourceTest {

    @Inject
    TestDataCleaner cleaner;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(cleaner::clean);
    }

    @Test
    void ownerManagesCfpLifecycle() {
        var token = registerCoordinator("grace@example.com");
        var eventId = createEvent(token);

        given()
                .header("Authorization", "Bearer " + token)
        .when().post("/api/events/" + eventId + "/cfp")
        .then()
                .statusCode(201)
                .body("eventId", is(eventId))
                .body("status", is("CLOSED"))
                .body("openedAt", nullValue())
                .body("closedAt", nullValue())
                .body("createdAt", notNullValue());

        given().when().get("/api/events/" + eventId + "/cfp")
        .then().statusCode(200).body("status", is("CLOSED"));

        var memberId = given()
                .contentType("application/json")
                .body(new SpeakerRequest("Reviewer", "reviewer@example.com", "correct horse", "Bio"))
        .when().post("/api/speakers")
        .then().statusCode(201)
                .extract().path("id");
        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(Map.of("userId", memberId))
        .when().post("/api/events/" + eventId + "/cfp/committee")
        .then().statusCode(201);

        given()
                .header("Authorization", "Bearer " + token)
        .when().post("/api/events/" + eventId + "/cfp/open")
        .then()
                .statusCode(200)
                .body("status", is("OPEN"))
                .body("openedAt", notNullValue())
                .body("closedAt", nullValue());

        given()
                .header("Authorization", "Bearer " + token)
        .when().post("/api/events/" + eventId + "/cfp/close")
        .then()
                .statusCode(200)
                .body("status", is("CLOSED"))
                .body("closedAt", notNullValue());

        given()
                .header("Authorization", "Bearer " + token)
        .when().post("/api/events/" + eventId + "/cfp/open")
        .then().statusCode(200).body("status", is("OPEN"));
    }

    @Test
    void forbidsNonOwner() {
        var ownerToken = registerCoordinator("grace@example.com");
        var eventId = createEvent(ownerToken);
        given()
                .header("Authorization", "Bearer " + ownerToken)
        .when().post("/api/events/" + eventId + "/cfp")
        .then().statusCode(201);

        given()
        .when().post("/api/events/" + eventId + "/cfp")
        .then().statusCode(401);

        var otherToken = registerCoordinator("linus@example.com");
        var speakerToken = registerSpeaker("ada@example.com");

        for (var token : new String[]{otherToken, speakerToken}) {
            given()
                    .header("Authorization", "Bearer " + token)
            .when().post("/api/events/" + eventId + "/cfp")
            .then()
                    .statusCode(403)
                    .body("error", is("Only the event owner can manage this CFP"));

            given()
                    .header("Authorization", "Bearer " + token)
            .when().post("/api/events/" + eventId + "/cfp/open")
            .then().statusCode(403);
        }
    }

    @Test
    void rejectsInvalidUsage() {
        var token = registerCoordinator("grace@example.com");

        given()
                .header("Authorization", "Bearer " + token)
        .when().post("/api/events/999999/cfp")
        .then()
                .statusCode(404)
                .body("error", is("Event not found"));

        var eventId = createEvent(token);

        given()
                .header("Authorization", "Bearer " + token)
        .when().post("/api/events/" + eventId + "/cfp")
        .then().statusCode(201);

        given()
                .header("Authorization", "Bearer " + token)
        .when().post("/api/events/" + eventId + "/cfp")
        .then()
                .statusCode(409)
                .body("error", is("CFP already exists for this event"));

        var otherEventId = createEvent(token);
        given()
                .header("Authorization", "Bearer " + token)
        .when().post("/api/events/" + otherEventId + "/cfp/open")
        .then()
                .statusCode(404)
                .body("error", is("CFP not found"));

        given().when().get("/api/events/" + otherEventId + "/cfp")
        .then()
                .statusCode(404)
                .body("error", is("CFP not found"));
    }

    private String registerCoordinator(String email) {
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("User " + email, email, "correct horse"))
        .when().post("/api/coordinators")
        .then().statusCode(201);
        return login(email, "correct horse");
    }

    private String registerSpeaker(String email) {
        given()
                .contentType("application/json")
                .body(new SpeakerRequest("Speaker " + email, email, "correct horse", "Bio"))
        .when().post("/api/speakers")
        .then().statusCode(201);
        return login(email, "correct horse");
    }

    private Object createEvent(String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new EventRequest("Devoxx", "Java conference",
                        Instant.parse("2026-06-01T10:00:00Z"),
                        Instant.parse("2026-06-03T18:00:00Z")))
        .when().post("/api/events")
        .then().statusCode(201)
                .extract().path("id");
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
