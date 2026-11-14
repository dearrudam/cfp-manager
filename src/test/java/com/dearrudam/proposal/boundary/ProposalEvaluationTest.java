package com.dearrudam.proposal.boundary;

import java.time.Instant;
import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dearrudam.event.boundary.EventRequest;
import com.dearrudam.proposal.entity.ProposalRepository;
import com.dearrudam.proposal.entity.ProposalStatus;
import com.dearrudam.proposal.entity.TalkLevel;
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
class ProposalEvaluationTest {

    @Inject
    TestDataCleaner cleaner;

    @Inject
    ProposalRepository proposals;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(cleaner::clean);
    }

    @Test
    void committeeMembersEvaluateProposal() {
        var coordinatorToken = registerCoordinator("grace@example.com");
        var reviewerAToken = registerSpeaker("reviewer-a@example.com");
        var reviewerBToken = registerSpeaker("reviewer-b@example.com");
        var eventId = createEvent(coordinatorToken);
        createCfp(coordinatorToken, eventId);
        addCommitteeMember(coordinatorToken, eventId, reviewerAToken);
        addCommitteeMember(coordinatorToken, eventId, reviewerBToken);
        openCfp(coordinatorToken, eventId);

        var speakerToken = registerSpeaker("ada@example.com");
        var proposalId = submitProposal(speakerToken, eventId);
        closeCfp(coordinatorToken, eventId);

        given()
                .header("Authorization", "Bearer " + reviewerAToken)
                .contentType("application/json")
                .body(new ProposalEvaluationRequest(5))
        .when().post("/api/proposals/" + proposalId + "/evaluations")
        .then()
                .statusCode(201)
                .body("proposalId", is(proposalId))
                .body("evaluatorId", notNullValue())
                .body("evaluatorName", is("Speaker reviewer-a@example.com"))
                .body("score", is(5))
                .body("createdAt", notNullValue());

        given()
                .header("Authorization", "Bearer " + reviewerBToken)
                .contentType("application/json")
                .body(new ProposalEvaluationRequest(2))
        .when().post("/api/proposals/" + proposalId + "/evaluations")
        .then().statusCode(201);

        for (var token : new String[]{reviewerAToken, reviewerBToken, coordinatorToken}) {
            given()
                    .header("Authorization", "Bearer " + token)
            .when().get("/api/proposals/" + proposalId + "/evaluations")
            .then()
                    .statusCode(200)
                    .body("size()", is(2))
                    .body("score", containsInAnyOrder(5, 2))
                    .body("evaluatorName", containsInAnyOrder(
                            "Speaker reviewer-a@example.com", "Speaker reviewer-b@example.com"));
        }
    }

    @Test
    void rejectsUnauthorizedAndInvalidEvaluations() {
        var coordinatorToken = registerCoordinator("grace@example.com");
        var reviewerToken = registerSpeaker("reviewer@example.com");
        var eventId = createEvent(coordinatorToken);
        createCfp(coordinatorToken, eventId);
        addCommitteeMember(coordinatorToken, eventId, reviewerToken);
        openCfp(coordinatorToken, eventId);

        var speakerToken = registerSpeaker("ada@example.com");
        var proposalId = submitProposal(speakerToken, eventId);
        closeCfp(coordinatorToken, eventId);

        given()
                .contentType("application/json")
                .body(new ProposalEvaluationRequest(4))
        .when().post("/api/proposals/" + proposalId + "/evaluations")
        .then().statusCode(401);

        given().when().get("/api/proposals/" + proposalId + "/evaluations")
        .then().statusCode(401);

        given()
                .header("Authorization", "Bearer " + reviewerToken)
                .contentType("application/json")
                .body(new ProposalEvaluationRequest(4))
        .when().post("/api/proposals/999999/evaluations")
        .then()
                .statusCode(404)
                .body("error", is("Proposal not found"));

        var outsiderToken = registerSpeaker("mallory@example.com");
        for (var token : new String[]{outsiderToken, speakerToken}) {
            given()
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .body(new ProposalEvaluationRequest(4))
            .when().post("/api/proposals/" + proposalId + "/evaluations")
            .then()
                    .statusCode(403)
                    .body("error", is("Only committee members of this CFP can evaluate proposals"));

            given()
                    .header("Authorization", "Bearer " + token)
            .when().get("/api/proposals/" + proposalId + "/evaluations")
            .then().statusCode(403);
        }

        given()
                .header("Authorization", "Bearer " + coordinatorToken)
                .contentType("application/json")
                .body(new ProposalEvaluationRequest(4))
        .when().post("/api/proposals/" + proposalId + "/evaluations")
        .then()
                .statusCode(403)
                .body("error", is("Only committee members of this CFP can evaluate proposals"));

        given()
                .header("Authorization", "Bearer " + coordinatorToken)
        .when().get("/api/proposals/" + proposalId + "/evaluations")
        .then().statusCode(200).body("size()", is(0));

        for (var score : new Object[]{0, 6}) {
            given()
                    .header("Authorization", "Bearer " + reviewerToken)
                    .contentType("application/json")
                    .body(Map.of("score", score))
            .when().post("/api/proposals/" + proposalId + "/evaluations")
            .then()
                    .statusCode(400)
                    .body("field", is("score"));
        }

        given()
                .header("Authorization", "Bearer " + reviewerToken)
                .contentType("application/json")
                .body(Map.of())
        .when().post("/api/proposals/" + proposalId + "/evaluations")
        .then()
                .statusCode(400)
                .body("field", is("score"));

        given()
                .header("Authorization", "Bearer " + reviewerToken)
                .contentType("application/json")
                .body(new ProposalEvaluationRequest(4))
        .when().put("/api/proposals/" + proposalId + "/evaluations")
        .then().statusCode(405);

        given()
                .header("Authorization", "Bearer " + reviewerToken)
        .when().delete("/api/proposals/" + proposalId + "/evaluations")
        .then().statusCode(405);
    }

    @Test
    void enforcesStateAuthorAndUniquenessRules() {
        var coordinatorToken = registerCoordinator("grace@example.com");
        var reviewerAToken = registerSpeaker("reviewer-a@example.com");
        var reviewerBToken = registerSpeaker("reviewer-b@example.com");
        var eventId = createEvent(coordinatorToken);
        createCfp(coordinatorToken, eventId);
        addCommitteeMember(coordinatorToken, eventId, reviewerAToken);
        addCommitteeMember(coordinatorToken, eventId, reviewerBToken);
        openCfp(coordinatorToken, eventId);

        var ownProposalId = submitProposal(reviewerAToken, eventId);
        var speakerToken = registerSpeaker("ada@example.com");
        var coAuthoredId = submitProposal(speakerToken, eventId);
        var decidedId = submitProposal(speakerToken, eventId);

        var reviewerUserId = given()
                .header("Authorization", "Bearer " + reviewerAToken)
        .when().get("/api/users/me")
        .then().statusCode(200)
                .extract().path("id");

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(Map.of("userId", reviewerUserId))
        .when().post("/api/proposals/" + coAuthoredId + "/speakers")
        .then().statusCode(201);

        given()
                .header("Authorization", "Bearer " + reviewerBToken)
                .contentType("application/json")
                .body(new ProposalEvaluationRequest(3))
        .when().post("/api/proposals/" + ownProposalId + "/evaluations")
        .then()
                .statusCode(409)
                .body("error", is("Proposal can only be evaluated while in EVAL status"));

        closeCfp(coordinatorToken, eventId);
        QuarkusTransaction.requiringNew().run(() ->
                proposals.update("status = ?1 where id = ?2",
                        ProposalStatus.APPROVED, asLong(decidedId)));

        for (var proposalId : new Object[]{ownProposalId, coAuthoredId}) {
            given()
                    .header("Authorization", "Bearer " + reviewerAToken)
                    .contentType("application/json")
                    .body(new ProposalEvaluationRequest(4))
            .when().post("/api/proposals/" + proposalId + "/evaluations")
            .then()
                    .statusCode(403)
                    .body("error", is("Authors cannot evaluate their own proposal"));
        }

        given()
                .header("Authorization", "Bearer " + reviewerBToken)
                .contentType("application/json")
                .body(new ProposalEvaluationRequest(4))
        .when().post("/api/proposals/" + decidedId + "/evaluations")
        .then()
                .statusCode(409)
                .body("error", is("Proposal can only be evaluated while in EVAL status"));

        given()
                .header("Authorization", "Bearer " + reviewerBToken)
                .contentType("application/json")
                .body(new ProposalEvaluationRequest(4))
        .when().post("/api/proposals/" + ownProposalId + "/evaluations")
        .then().statusCode(201);

        given()
                .header("Authorization", "Bearer " + reviewerBToken)
                .contentType("application/json")
                .body(new ProposalEvaluationRequest(1))
        .when().post("/api/proposals/" + ownProposalId + "/evaluations")
        .then()
                .statusCode(409)
                .body("error", is("Evaluator has already evaluated this proposal"));
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

    private void addCommitteeMember(String coordinatorToken, Object eventId, String memberToken) {
        var memberId = given()
                .header("Authorization", "Bearer " + memberToken)
        .when().get("/api/users/me")
        .then().statusCode(200)
                .extract().path("id");
        given()
                .header("Authorization", "Bearer " + coordinatorToken)
                .contentType("application/json")
                .body(Map.of("userId", memberId))
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

    private static Long asLong(Object id) {
        return ((Number) id).longValue();
    }
}
