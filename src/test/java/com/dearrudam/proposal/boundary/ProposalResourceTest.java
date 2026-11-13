package com.dearrudam.proposal.boundary;

import java.time.Instant;
import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dearrudam.event.boundary.EventRequest;
import com.dearrudam.proposal.entity.TalkLevel;
import com.dearrudam.shared.TestDataCleaner;
import com.dearrudam.user.boundary.CoordinatorRequest;
import com.dearrudam.user.boundary.LoginRequest;
import com.dearrudam.user.boundary.SpeakerRequest;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class ProposalResourceTest {

    @Inject
    TestDataCleaner cleaner;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(cleaner::clean);
    }

    @Test
    void speakerSubmitsAndManagesProposal() {
        var speakerToken = registerSpeaker("ada@example.com");
        var eventId = openCfpWithEvent();

        var proposalId = given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest(asLong(eventId), "My talk", "All about Java",
                        TalkLevel.INTERMEDIATE, 45, false))
        .when().post("/api/proposals")
        .then()
                .statusCode(201)
                .body("title", is("My talk"))
                .body("abstract", is("All about Java"))
                .body("level", is("INTERMEDIATE"))
                .body("durationMinutes", is(45))
                .body("status", is("SUBMITTED"))
                .body("eventId", is(eventId))
                .body("ownerName", is("Speaker ada@example.com"))
                .body("speakers", empty())
                .body("draftId", nullValue())
                .body("createdAt", notNullValue())
                .extract().path("id");

        given()
                .header("Authorization", "Bearer " + speakerToken)
        .when().get("/api/proposals/mine")
        .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("title", contains("My talk"))
                .body("status", contains("SUBMITTED"));

        given().when().get("/api/proposals/" + proposalId)
        .then().statusCode(401);

        given()
                .header("Authorization", "Bearer " + speakerToken)
        .when().get("/api/proposals/" + proposalId)
        .then()
                .statusCode(200)
                .body("id", is(proposalId))
                .body("title", is("My talk"));

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new UpdateProposalRequest("Renamed talk", "Better abstract",
                        TalkLevel.ADVANCED, 60))
        .when().put("/api/proposals/" + proposalId)
        .then()
                .statusCode(200)
                .body("title", is("Renamed talk"))
                .body("abstract", is("Better abstract"))
                .body("level", is("ADVANCED"))
                .body("durationMinutes", is(60))
                .body("status", is("SUBMITTED"));
    }

    @Test
    void saveAsDraftCreatesDraftCopy() {
        var speakerToken = registerSpeaker("ada@example.com");
        var eventId = openCfpWithEvent();

        var draftId = given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest(asLong(eventId), "Talk with draft", "Abstract text",
                        TalkLevel.BEGINNER, 30, true))
        .when().post("/api/proposals")
        .then()
                .statusCode(201)
                .body("status", is("SUBMITTED"))
                .body("draftId", notNullValue())
                .extract().path("draftId");

        given()
                .header("Authorization", "Bearer " + speakerToken)
        .when().get("/api/drafts/" + draftId)
        .then()
                .statusCode(200)
                .body("title", is("Talk with draft"))
                .body("abstract", is("Abstract text"))
                .body("level", is("BEGINNER"))
                .body("durationMinutes", is(30))
                .body("ownerName", is("Speaker ada@example.com"));
    }

    @Test
    void submitRequiresOpenCfp() {
        var speakerToken = registerSpeaker("ada@example.com");
        var coordinatorToken = registerCoordinator("grace@example.com");

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest(999999L, "Talk", "Abstract",
                        TalkLevel.BEGINNER, 30, false))
        .when().post("/api/proposals")
        .then()
                .statusCode(404)
                .body("error", is("Event not found"));

        var eventWithoutCfp = createEvent(coordinatorToken);
        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest(asLong(eventWithoutCfp), "Talk", "Abstract",
                        TalkLevel.BEGINNER, 30, false))
        .when().post("/api/proposals")
        .then()
                .statusCode(404)
                .body("error", is("CFP not found"));

        var eventId = createEvent(coordinatorToken);
        createCfp(coordinatorToken, eventId);

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest(asLong(eventId), "Talk", "Abstract",
                        TalkLevel.BEGINNER, 30, false))
        .when().post("/api/proposals")
        .then()
                .statusCode(409)
                .body("error", is("CFP is not open for submissions"));

        openCfpWithMember(coordinatorToken, eventId, "member-a@example.com");

        var proposalId = given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest(asLong(eventId), "Talk", "Abstract",
                        TalkLevel.BEGINNER, 30, false))
        .when().post("/api/proposals")
        .then().statusCode(201)
                .extract().path("id");

        closeCfp(coordinatorToken, eventId);

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new UpdateProposalRequest("Edited", "Abstract",
                        TalkLevel.BEGINNER, 30))
        .when().put("/api/proposals/" + proposalId)
        .then()
                .statusCode(409)
                .body("error", is("Proposal can only be modified while the CFP is open"));
    }

    @Test
    void ownerAddsCoSpeakers() {
        var speakerToken = registerSpeaker("ada@example.com");
        var eventId = openCfpWithEvent();
        var proposalId = submitProposal(speakerToken, eventId);

        var coSpeakerId = registerSpeakerId("katherine@example.com");

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalSpeakerRequest(asLong(coSpeakerId)))
        .when().post("/api/proposals/" + proposalId + "/speakers")
        .then()
                .statusCode(201)
                .body("userId", is(coSpeakerId))
                .body("name", is("Speaker katherine@example.com"));

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalSpeakerRequest(asLong(coSpeakerId)))
        .when().post("/api/proposals/" + proposalId + "/speakers")
        .then()
                .statusCode(409)
                .body("error", is("User is already a co-speaker of this proposal"));

        var ownerId = given()
                .header("Authorization", "Bearer " + speakerToken)
        .when().get("/api/users/me")
        .then().statusCode(200)
                .extract().path("id");

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalSpeakerRequest(asLong(ownerId)))
        .when().post("/api/proposals/" + proposalId + "/speakers")
        .then()
                .statusCode(409)
                .body("error", is("The owner cannot be added as a co-speaker"));

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalSpeakerRequest(999999L))
        .when().post("/api/proposals/" + proposalId + "/speakers")
        .then()
                .statusCode(404)
                .body("error", is("User not found"));

        given()
                .header("Authorization", "Bearer " + speakerToken)
        .when().get("/api/proposals/" + proposalId)
        .then()
                .statusCode(200)
                .body("speakers.size()", is(1))
                .body("speakers.userId", contains(coSpeakerId))
                .body("speakers.name", contains("Speaker katherine@example.com"));

        given()
                .header("Authorization", "Bearer " + speakerToken)
        .when().get("/api/proposals/mine")
        .then()
                .statusCode(200)
                .body("speakers.size()", is(1));
    }

    @Test
    void forbidsNonOwner() {
        var ownerToken = registerSpeaker("ada@example.com");
        var eventId = openCfpWithEvent();
        var proposalId = submitProposal(ownerToken, eventId);

        var otherSpeakerToken = registerSpeaker("mallory@example.com");
        var coordinatorToken = registerCoordinator("linus@example.com");
        var coSpeakerId = registerSpeakerId("katherine@example.com");

        for (var token : new String[]{otherSpeakerToken, coordinatorToken}) {
            given()
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .body(new UpdateProposalRequest("Hijacked", "Abstract",
                            TalkLevel.BEGINNER, 30))
            .when().put("/api/proposals/" + proposalId)
            .then().statusCode(403);

            given()
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .body(new ProposalSpeakerRequest(asLong(coSpeakerId)))
            .when().post("/api/proposals/" + proposalId + "/speakers")
            .then().statusCode(403);
        }

        given()
                .header("Authorization", "Bearer " + otherSpeakerToken)
        .when().get("/api/proposals/mine")
        .then().statusCode(200).body("size()", is(0));

        for (var token : new String[]{otherSpeakerToken, coordinatorToken}) {
            given()
                    .header("Authorization", "Bearer " + token)
            .when().get("/api/proposals/" + proposalId)
            .then()
                    .statusCode(200)
                    .body("title", is("Submitted talk"))
                    .body("status", is("SUBMITTED"));
        }
    }

    @Test
    void rejectsInvalidUsage() {
        given()
                .contentType("application/json")
                .body(new ProposalRequest(1L, "Talk", "Abstract", TalkLevel.BEGINNER, 30, false))
        .when().post("/api/proposals")
        .then().statusCode(401);

        given().when().get("/api/proposals/mine").then().statusCode(401);
        given().when().get("/api/proposals/999999").then().statusCode(401);

        var coordinatorToken = registerCoordinator("linus@example.com");
        given()
                .header("Authorization", "Bearer " + coordinatorToken)
                .contentType("application/json")
                .body(new ProposalRequest(1L, "Talk", "Abstract", TalkLevel.BEGINNER, 30, false))
        .when().post("/api/proposals")
        .then().statusCode(403);

        var speakerToken = registerSpeaker("ada@example.com");
        var eventId = openCfpWithEvent();

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest(null, "Talk", "Abstract", TalkLevel.BEGINNER, 30, false))
        .when().post("/api/proposals")
        .then()
                .statusCode(400)
                .body("field", is("eventId"));

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest(asLong(eventId), " ", "Abstract",
                        TalkLevel.BEGINNER, 30, false))
        .when().post("/api/proposals")
        .then()
                .statusCode(400)
                .body("field", is("title"));

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest(asLong(eventId), "Talk", " ",
                        TalkLevel.BEGINNER, 30, false))
        .when().post("/api/proposals")
        .then()
                .statusCode(400)
                .body("field", is("abstractText"));

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest(asLong(eventId), "Talk", "Abstract",
                        null, 30, false))
        .when().post("/api/proposals")
        .then()
                .statusCode(400)
                .body("field", is("level"));

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest(asLong(eventId), "Talk", "Abstract",
                        TalkLevel.BEGINNER, 0, false))
        .when().post("/api/proposals")
        .then()
                .statusCode(400)
                .body("field", is("durationMinutes"));

        var proposalId = submitProposal(speakerToken, eventId);

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new UpdateProposalRequest("X", "Abstract", TalkLevel.BEGINNER, 30))
        .when().put("/api/proposals/999999")
        .then()
                .statusCode(404)
                .body("error", is("Proposal not found"));

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(Map.of("userId", 1))
        .when().post("/api/proposals/999999/speakers")
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

    private void createCfp(String token, Object eventId) {
        given()
                .header("Authorization", "Bearer " + token)
        .when().post("/api/events/" + eventId + "/cfp")
        .then().statusCode(201);
    }

    private void openCfpWithMember(String coordinatorToken, Object eventId, String memberEmail) {
        var memberId = registerSpeakerId(memberEmail);
        given()
                .header("Authorization", "Bearer " + coordinatorToken)
                .contentType("application/json")
                .body(Map.of("userId", memberId))
        .when().post("/api/events/" + eventId + "/cfp/committee")
        .then().statusCode(201);
        given()
                .header("Authorization", "Bearer " + coordinatorToken)
        .when().post("/api/events/" + eventId + "/cfp/open")
        .then().statusCode(200);
    }

    private void closeCfp(String token, Object eventId) {
        given()
                .header("Authorization", "Bearer " + token)
        .when().post("/api/events/" + eventId + "/cfp/close")
        .then().statusCode(200);
    }

    private Object openCfpWithEvent() {
        var coordinatorToken = registerCoordinator("grace@example.com");
        var eventId = createEvent(coordinatorToken);
        createCfp(coordinatorToken, eventId);
        openCfpWithMember(coordinatorToken, eventId, "reviewer@example.com");
        return eventId;
    }

    private Object submitProposal(String speakerToken, Object eventId) {
        return given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalRequest(asLong(eventId), "Submitted talk", "Abstract",
                        TalkLevel.BEGINNER, 30, false))
        .when().post("/api/proposals")
        .then().statusCode(201)
                .extract().path("id");
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

    private Object registerSpeakerId(String email) {
        return given()
                .contentType("application/json")
                .body(new SpeakerRequest("Speaker " + email, email, "correct horse", "Bio"))
        .when().post("/api/speakers")
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

    private static Long asLong(Object id) {
        return ((Number) id).longValue();
    }
}
