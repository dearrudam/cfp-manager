package com.dearrudam.evaluation.boundary;

import java.time.Instant;
import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dearrudam.evaluation.EvaluationRequirement;
import com.dearrudam.event.boundary.EventRequest;
import com.dearrudam.proposal.boundary.ProposalRequest;
import com.dearrudam.proposal.boundary.ProposalSpeakerRequest;
import com.dearrudam.proposal.entity.TalkLevel;
import com.dearrudam.shared.TestDataCleaner;
import com.dearrudam.user.boundary.CoordinatorRequest;
import com.dearrudam.user.boundary.LoginRequest;
import com.dearrudam.user.boundary.SpeakerRequest;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

import static com.dearrudam.evaluation.EvaluationRequirement.Rn.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class EvaluationResourceTest {

    @Inject
    TestDataCleaner cleaner;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(cleaner::clean);
    }

    @Test
    @EvaluationRequirement({R1_1, R1_6})
    void committeeMembersEvaluateProposal() {
        var coordinatorToken = registerCoordinator("grace@example.com");
        var eventId = createEvent(coordinatorToken);
        createCfp(coordinatorToken, eventId);
        var reviewerA = registerSpeakerId("reviewer-a@example.com");
        var reviewerB = registerSpeakerId("reviewer-b@example.com");
        addCommitteeMember(coordinatorToken, eventId, reviewerA);
        addCommitteeMember(coordinatorToken, eventId, reviewerB);
        openCfp(coordinatorToken, eventId);

        var authorToken = registerSpeaker("ada@example.com");
        var proposalId = submitProposal(authorToken, eventId);
        closeCfp(coordinatorToken, eventId);

        var reviewerAToken = login("reviewer-a@example.com", "correct horse");
        var reviewerBToken = login("reviewer-b@example.com", "correct horse");

        given()
                .header("Authorization", "Bearer " + reviewerAToken)
                .contentType("application/json")
                .body(new EvaluationRequest(4))
        .when().post(evaluationsPath(proposalId))
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("proposalId", is(proposalId))
                .body("evaluatorId", is(reviewerA))
                .body("evaluatorName", is("Speaker reviewer-a@example.com"))
                .body("score", is(4))
                .body("createdAt", notNullValue());

        given()
                .header("Authorization", "Bearer " + reviewerBToken)
                .contentType("application/json")
                .body(new EvaluationRequest(5))
        .when().post(evaluationsPath(proposalId))
        .then()
                .statusCode(201)
                .body("proposalId", is(proposalId))
                .body("evaluatorId", is(reviewerB))
                .body("score", is(5));

        given()
                .header("Authorization", "Bearer " + reviewerAToken)
                .contentType("application/json")
                .body(new EvaluationRequest(1))
        .when().post(evaluationsPath(proposalId))
        .then()
                .statusCode(409)
                .body("error", is("User already evaluated this proposal"));
    }

    @Test
    @EvaluationRequirement({R1_2, R1_3})
    void rejectsUnauthenticatedAndNonMembers() {
        var coordinatorToken = registerCoordinator("grace@example.com");
        var eventId = createEvent(coordinatorToken);
        createCfp(coordinatorToken, eventId);
        var reviewerId = registerSpeakerId("reviewer@example.com");
        addCommitteeMember(coordinatorToken, eventId, reviewerId);
        openCfp(coordinatorToken, eventId);

        var authorToken = registerSpeaker("ada@example.com");
        var proposalId = submitProposal(authorToken, eventId);
        closeCfp(coordinatorToken, eventId);

        given()
                .contentType("application/json")
                .body(new EvaluationRequest(3))
        .when().post(evaluationsPath(proposalId))
        .then().statusCode(401);

        var nonMemberToken = registerSpeaker("mallory@example.com");
        given()
                .header("Authorization", "Bearer " + nonMemberToken)
                .contentType("application/json")
                .body(new EvaluationRequest(3))
        .when().post(evaluationsPath(proposalId))
        .then()
                .statusCode(403)
                .body("error", is("Only committee members can evaluate proposals"));

        given()
                .header("Authorization", "Bearer " + coordinatorToken)
                .contentType("application/json")
                .body(new EvaluationRequest(3))
        .when().post(evaluationsPath(proposalId))
        .then()
                .statusCode(403)
                .body("error", is("Only committee members can evaluate proposals"));
    }

    @Test
    @EvaluationRequirement(R1_4)
    void rejectsAuthors() {
        var coordinatorToken = registerCoordinator("grace@example.com");
        var eventId = createEvent(coordinatorToken);
        createCfp(coordinatorToken, eventId);
        var authorId = registerSpeakerId("ada@example.com");
        var coAuthorId = registerSpeakerId("katherine@example.com");
        addCommitteeMember(coordinatorToken, eventId, authorId);
        addCommitteeMember(coordinatorToken, eventId, coAuthorId);
        openCfp(coordinatorToken, eventId);

        var authorToken = login("ada@example.com", "correct horse");
        var proposalId = submitProposal(authorToken, eventId);
        addCoSpeaker(authorToken, proposalId, coAuthorId);
        closeCfp(coordinatorToken, eventId);

        given()
                .header("Authorization", "Bearer " + authorToken)
                .contentType("application/json")
                .body(new EvaluationRequest(3))
        .when().post(evaluationsPath(proposalId))
        .then()
                .statusCode(403)
                .body("error", is("Authors cannot evaluate their own proposal"));

        var coAuthorToken = login("katherine@example.com", "correct horse");
        given()
                .header("Authorization", "Bearer " + coAuthorToken)
                .contentType("application/json")
                .body(new EvaluationRequest(3))
        .when().post(evaluationsPath(proposalId))
        .then()
                .statusCode(403)
                .body("error", is("Authors cannot evaluate their own proposal"));
    }

    @Test
    @EvaluationRequirement(R1_5)
    void rejectsProposalNotUnderEvaluation() {
        var coordinatorToken = registerCoordinator("grace@example.com");
        var eventId = createEvent(coordinatorToken);
        createCfp(coordinatorToken, eventId);
        var reviewerId = registerSpeakerId("reviewer@example.com");
        addCommitteeMember(coordinatorToken, eventId, reviewerId);
        openCfp(coordinatorToken, eventId);

        var authorToken = registerSpeaker("ada@example.com");
        var proposalId = submitProposal(authorToken, eventId);

        var reviewerToken = login("reviewer@example.com", "correct horse");
        given()
                .header("Authorization", "Bearer " + reviewerToken)
                .contentType("application/json")
                .body(new EvaluationRequest(3))
        .when().post(evaluationsPath(proposalId))
        .then()
                .statusCode(409)
                .body("error", is("Proposal is not under evaluation"));
    }

    @Test
    @EvaluationRequirement(R1_7)
    void rejectsMissingProposal() {
        var speakerToken = registerSpeaker("ada@example.com");

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new EvaluationRequest(3))
        .when().post("/api/proposals/999999/evaluations")
        .then()
                .statusCode(404)
                .body("error", is("Proposal not found"));
    }

    @Test
    @EvaluationRequirement(R1_8)
    void rejectsInvalidScore() {
        var coordinatorToken = registerCoordinator("grace@example.com");
        var eventId = createEvent(coordinatorToken);
        createCfp(coordinatorToken, eventId);
        var reviewerId = registerSpeakerId("reviewer@example.com");
        addCommitteeMember(coordinatorToken, eventId, reviewerId);
        openCfp(coordinatorToken, eventId);

        var authorToken = registerSpeaker("ada@example.com");
        var proposalId = submitProposal(authorToken, eventId);
        closeCfp(coordinatorToken, eventId);

        var reviewerToken = login("reviewer@example.com", "correct horse");

        for (var score : new Integer[]{0, 6}) {
            given()
                    .header("Authorization", "Bearer " + reviewerToken)
                    .contentType("application/json")
                    .body(new EvaluationRequest(score))
            .when().post(evaluationsPath(proposalId))
            .then()
                    .statusCode(400)
                    .body("field", is("score"));
        }

        given()
                .header("Authorization", "Bearer " + reviewerToken)
                .contentType("application/json")
                .body(Map.of())
        .when().post(evaluationsPath(proposalId))
        .then()
                .statusCode(400)
                .body("field", is("score"));
    }

    @Test
    @EvaluationRequirement(R1_9)
    void recordedEvaluationCannotBeAltered() {
        var coordinatorToken = registerCoordinator("grace@example.com");
        var eventId = createEvent(coordinatorToken);
        createCfp(coordinatorToken, eventId);
        var reviewerId = registerSpeakerId("reviewer@example.com");
        addCommitteeMember(coordinatorToken, eventId, reviewerId);
        openCfp(coordinatorToken, eventId);

        var authorToken = registerSpeaker("ada@example.com");
        var proposalId = submitProposal(authorToken, eventId);
        closeCfp(coordinatorToken, eventId);

        var reviewerToken = login("reviewer@example.com", "correct horse");
        given()
                .header("Authorization", "Bearer " + reviewerToken)
                .contentType("application/json")
                .body(new EvaluationRequest(4))
        .when().post(evaluationsPath(proposalId))
        .then().statusCode(201);

        for (var method : new String[]{"put", "delete"}) {
            given()
                    .header("Authorization", "Bearer " + reviewerToken)
                    .contentType("application/json")
                    .body(new EvaluationRequest(1))
            .when().request(method, evaluationsPath(proposalId))
            .then().statusCode(404);
        }
    }

    private String evaluationsPath(Object proposalId) {
        return "/api/proposals/" + proposalId + "/evaluations";
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

    private void addCommitteeMember(String token, Object eventId, Object userId) {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(Map.of("userId", userId))
        .when().post("/api/events/" + eventId + "/cfp/committee")
        .then().statusCode(201);
    }

    private void openCfp(String token, Object eventId) {
        given()
                .header("Authorization", "Bearer " + token)
        .when().post("/api/events/" + eventId + "/cfp/open")
        .then().statusCode(200);
    }

    private void closeCfp(String token, Object eventId) {
        given()
                .header("Authorization", "Bearer " + token)
        .when().post("/api/events/" + eventId + "/cfp/close")
        .then().statusCode(200);
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

    private void addCoSpeaker(String speakerToken, Object proposalId, Object userId) {
        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalSpeakerRequest(asLong(userId)))
        .when().post("/api/proposals/" + proposalId + "/speakers")
        .then().statusCode(201);
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
