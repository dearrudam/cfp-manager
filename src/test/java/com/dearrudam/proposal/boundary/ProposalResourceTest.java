package com.dearrudam.proposal.boundary;

import java.time.Instant;
import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dearrudam.event.boundary.EventRequest;
import com.dearrudam.shared.TestDataCleaner;
import com.dearrudam.user.boundary.CoordinatorRequest;
import com.dearrudam.user.boundary.LoginRequest;
import com.dearrudam.user.boundary.SpeakerRequest;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class ProposalResourceTest {

    @Inject
    TestDataCleaner cleaner;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(cleaner::clean);
    }

    @Test
    void speakerSubmitsAndReviewsOwnProposals() {
        var coordinatorToken = registerCoordinator("grace@example.com");
        var eventId = openCfp(coordinatorToken);
        var speakerToken = registerSpeaker("ada@example.com");

        var firstId = given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest("My first talk", "All about Java"))
        .when().post("/api/events/" + eventId + "/cfp/proposals")
        .then()
                .statusCode(201)
                .body("title", is("My first talk"))
                .body("description", is("All about Java"))
                .body("cfpId", notNullValue())
                .body("eventId", is(eventId))
                .body("eventName", is("Devoxx"))
                .body("speakerId", notNullValue())
                .body("speakerName", is("Speaker ada@example.com"))
                .body("submittedAt", notNullValue())
                .extract().path("id");

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest("My second talk", "Even more Java"))
        .when().post("/api/events/" + eventId + "/cfp/proposals")
        .then().statusCode(201);

        given()
                .header("Authorization", "Bearer " + speakerToken)
        .when().get("/api/proposals")
        .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("title", contains("My second talk", "My first talk"));

        given()
                .header("Authorization", "Bearer " + speakerToken)
        .when().get("/api/proposals/" + firstId)
        .then()
                .statusCode(200)
                .body("id", is(firstId))
                .body("title", is("My first talk"))
                .body("eventName", is("Devoxx"))
                .body("speakerName", is("Speaker ada@example.com"));

        var otherSpeakerToken = registerSpeaker("bob@example.com");

        given()
                .header("Authorization", "Bearer " + otherSpeakerToken)
        .when().get("/api/proposals")
        .then().statusCode(200).body("size()", is(0));

        given()
                .header("Authorization", "Bearer " + otherSpeakerToken)
        .when().get("/api/proposals/" + firstId)
        .then()
                .statusCode(403)
                .body("error", is("Only the proposal owner can view this proposal"));
    }

    @Test
    void rejectsSubmissionWhenCfpIsNotOpen() {
        var coordinatorToken = registerCoordinator("grace@example.com");
        var eventId = createEvent(coordinatorToken);
        var speakerToken = registerSpeaker("ada@example.com");

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest("Talk", "Description"))
        .when().post("/api/events/999999/cfp/proposals")
        .then()
                .statusCode(404)
                .body("error", is("Event not found"));

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest("Talk", "Description"))
        .when().post("/api/events/" + eventId + "/cfp/proposals")
        .then()
                .statusCode(404)
                .body("error", is("CFP not found"));

        given()
                .header("Authorization", "Bearer " + coordinatorToken)
        .when().post("/api/events/" + eventId + "/cfp")
        .then().statusCode(201);

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest("Talk", "Description"))
        .when().post("/api/events/" + eventId + "/cfp/proposals")
        .then()
                .statusCode(409)
                .body("error", is("CFP is not open"));
    }

    @Test
    void rejectsInvalidUsage() {
        given()
                .contentType("application/json")
                .body(new ProposalRequest("Talk", "Description"))
        .when().post("/api/events/1/cfp/proposals")
        .then().statusCode(401);

        given().when().get("/api/proposals").then().statusCode(401);
        given().when().get("/api/proposals/1").then().statusCode(401);

        var coordinatorToken = registerCoordinator("linus@example.com");

        given()
                .header("Authorization", "Bearer " + coordinatorToken)
                .contentType("application/json")
                .body(new ProposalRequest("Talk", "Description"))
        .when().post("/api/events/1/cfp/proposals")
        .then().statusCode(403);

        given()
                .header("Authorization", "Bearer " + coordinatorToken)
        .when().get("/api/proposals")
        .then().statusCode(403);

        given()
                .header("Authorization", "Bearer " + coordinatorToken)
        .when().get("/api/proposals/1")
        .then().statusCode(403);

        var speakerToken = registerSpeaker("ada@example.com");
        var eventId = openCfp(registerCoordinator("grace@example.com"));

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(Map.of("description", "Missing title"))
        .when().post("/api/events/" + eventId + "/cfp/proposals")
        .then()
                .statusCode(400)
                .body("field", is("title"));

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest(" ", "Blank title"))
        .when().post("/api/events/" + eventId + "/cfp/proposals")
        .then()
                .statusCode(400)
                .body("field", is("title"));

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest("No description", " "))
        .when().post("/api/events/" + eventId + "/cfp/proposals")
        .then()
                .statusCode(400)
                .body("field", is("description"));

        given()
                .header("Authorization", "Bearer " + speakerToken)
        .when().get("/api/proposals/999999")
        .then()
                .statusCode(404)
                .body("error", is("Proposal not found"));
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

    private Object openCfp(String coordinatorToken) {
        var eventId = createEvent(coordinatorToken);
        given()
                .header("Authorization", "Bearer " + coordinatorToken)
        .when().post("/api/events/" + eventId + "/cfp")
        .then().statusCode(201);

        var reviewerId = given()
                .contentType("application/json")
                .body(new SpeakerRequest("Reviewer", "reviewer@example.com", "correct horse", "Bio"))
        .when().post("/api/speakers")
        .then().statusCode(201)
                .extract().path("id");

        given()
                .header("Authorization", "Bearer " + coordinatorToken)
                .contentType("application/json")
                .body(Map.of("userId", reviewerId))
        .when().post("/api/events/" + eventId + "/cfp/committee")
        .then().statusCode(201);

        given()
                .header("Authorization", "Bearer " + coordinatorToken)
        .when().post("/api/events/" + eventId + "/cfp/open")
        .then()
                .statusCode(200)
                .body("status", is("OPEN"));

        return eventId;
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

    private String login(String email, String password) {
        return given()
                .contentType("application/json")
                .body(new LoginRequest(email, password))
        .when().post("/api/auth/login")
        .then().statusCode(200)
                .extract().path("token");
    }
}
