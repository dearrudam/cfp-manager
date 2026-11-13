package com.dearrudam.cfp.boundary;

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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class CfpCommitteeTest {

    @Inject
    TestDataCleaner cleaner;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(cleaner::clean);
    }

    @Test
    void ownerAddsListsAndRemovesMembers() {
        var ownerToken = registerCoordinator("grace@example.com");
        var eventId = createEvent(ownerToken);
        createCfp(ownerToken, eventId);

        given().when().get("/api/events/" + eventId + "/cfp/committee")
        .then().statusCode(200).body("size()", is(0));

        var memberA = registerSpeaker("ada@example.com");
        var memberB = registerSpeaker("katherine@example.com");

        given()
                .header("Authorization", "Bearer " + ownerToken)
                .contentType("application/json")
                .body(Map.of("userId", memberA))
        .when().post("/api/events/" + eventId + "/cfp/committee")
        .then()
                .statusCode(201)
                .body("userId", is(memberA))
                .body("name", is("Speaker ada@example.com"))
                .body("createdAt", notNullValue());

        given()
                .header("Authorization", "Bearer " + ownerToken)
                .contentType("application/json")
                .body(Map.of("userId", memberA))
        .when().post("/api/events/" + eventId + "/cfp/committee")
        .then()
                .statusCode(409)
                .body("error", is("User is already a committee member"));

        given()
                .header("Authorization", "Bearer " + ownerToken)
                .contentType("application/json")
                .body(Map.of("userId", 999999))
        .when().post("/api/events/" + eventId + "/cfp/committee")
        .then()
                .statusCode(404)
                .body("error", is("User not found"));

        given()
                .header("Authorization", "Bearer " + ownerToken)
                .contentType("application/json")
                .body(Map.of("userId", memberB))
        .when().post("/api/events/" + eventId + "/cfp/committee")
        .then().statusCode(201);

        given().when().get("/api/events/" + eventId + "/cfp/committee")
        .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("userId", containsInAnyOrder(memberA, memberB));

        given()
                .header("Authorization", "Bearer " + ownerToken)
        .when().delete("/api/events/" + eventId + "/cfp/committee/" + memberA)
        .then().statusCode(204);

        given()
                .header("Authorization", "Bearer " + ownerToken)
        .when().delete("/api/events/" + eventId + "/cfp/committee/" + memberB)
        .then()
                .statusCode(409)
                .body("error", is("CFP must have at least one committee member"));

        given().when().get("/api/events/" + eventId + "/cfp/committee")
        .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("userId", containsInAnyOrder(memberB));
    }

    @Test
    void openRequiresAtLeastOneMember() {
        var ownerToken = registerCoordinator("grace@example.com");
        var eventId = createEvent(ownerToken);
        createCfp(ownerToken, eventId);

        given()
                .header("Authorization", "Bearer " + ownerToken)
        .when().post("/api/events/" + eventId + "/cfp/open")
        .then()
                .statusCode(409)
                .body("error", is("CFP requires at least one committee member to open"));

        var memberId = registerSpeaker("ada@example.com");
        given()
                .header("Authorization", "Bearer " + ownerToken)
                .contentType("application/json")
                .body(Map.of("userId", memberId))
        .when().post("/api/events/" + eventId + "/cfp/committee")
        .then().statusCode(201);

        given()
                .header("Authorization", "Bearer " + ownerToken)
        .when().post("/api/events/" + eventId + "/cfp/open")
        .then().statusCode(200).body("status", is("OPEN"));
    }

    @Test
    void forbidsNonOwnerCommitteeChanges() {
        var ownerToken = registerCoordinator("grace@example.com");
        var eventId = createEvent(ownerToken);
        createCfp(ownerToken, eventId);

        var memberId = registerSpeaker("ada@example.com");
        var speakerToken = login("ada@example.com", "correct horse");
        var otherOwnerToken = registerCoordinator("linus@example.com");

        given()
                .contentType("application/json")
                .body(Map.of("userId", memberId))
        .when().post("/api/events/" + eventId + "/cfp/committee")
        .then().statusCode(401);

        for (var token : new String[]{otherOwnerToken, speakerToken}) {
            given()
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .body(Map.of("userId", memberId))
            .when().post("/api/events/" + eventId + "/cfp/committee")
            .then()
                    .statusCode(403)
                    .body("error", is("Only the event owner can manage this CFP"));

            given()
                    .header("Authorization", "Bearer " + token)
            .when().delete("/api/events/" + eventId + "/cfp/committee/" + memberId)
            .then().statusCode(403);
        }

        given()
                .header("Authorization", "Bearer " + ownerToken)
        .when().delete("/api/events/" + eventId + "/cfp/committee/999999")
        .then()
                .statusCode(404)
                .body("error", is("Committee member not found"));
    }

    private String registerCoordinator(String email) {
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("User " + email, email, "correct horse"))
        .when().post("/api/coordinators")
        .then().statusCode(201);
        return login(email, "correct horse");
    }

    private Object registerSpeaker(String email) {
        var id = given()
                .contentType("application/json")
                .body(new SpeakerRequest("Speaker " + email, email, "correct horse", "Bio"))
        .when().post("/api/speakers")
        .then().statusCode(201)
                .extract().path("id");
        return id;
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

    private void createCfp(String token, Object eventId) {
        given()
                .header("Authorization", "Bearer " + token)
        .when().post("/api/events/" + eventId + "/cfp")
        .then().statusCode(201);
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
