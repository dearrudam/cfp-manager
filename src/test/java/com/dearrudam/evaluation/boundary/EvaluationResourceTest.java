package com.dearrudam.evaluation.boundary;

import java.time.Instant;
import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dearrudam.event.boundary.EventRequest;
import com.dearrudam.proposal.boundary.ProposalRequest;
import com.dearrudam.proposal.boundary.ProposalSpeakerRequest;
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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class EvaluationResourceTest {

    @Inject
    TestDataCleaner cleaner;

    @Inject
    ProposalRepository proposals;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(cleaner::clean);
    }

    @Test
    void committeeMemberEvaluatesAndLists() {
        var ctx = evalContext();
        var secondId = registerSpeakerId("katherine@example.com");
        addCommitteeMember(ctx.coordinatorToken(), ctx.eventId(), secondId);
        var secondToken = login("katherine@example.com", "correct horse");

        given()
                .header("Authorization", "Bearer " + ctx.reviewerToken())
                .contentType("application/json")
                .body(new EvaluationRequest(4))
        .when().post("/api/proposals/" + ctx.proposalId() + "/evaluations")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("proposalId", is(ctx.proposalId()))
                .body("evaluatorId", is(ctx.reviewerId()))
                .body("evaluatorName", is("Speaker reviewer@example.com"))
                .body("score", is(4))
                .body("createdAt", notNullValue());

        given()
                .header("Authorization", "Bearer " + secondToken)
                .contentType("application/json")
                .body(new EvaluationRequest(5))
        .when().post("/api/proposals/" + ctx.proposalId() + "/evaluations")
        .then()
                .statusCode(201)
                .body("score", is(5));

        given()
                .header("Authorization", "Bearer " + ctx.reviewerToken())
        .when().get("/api/proposals/" + ctx.proposalId() + "/evaluations")
        .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("score", contains(4, 5))
                .body("evaluatorName", contains(
                        "Speaker reviewer@example.com", "Speaker katherine@example.com"));
    }

    @Test
    void rejectsNonCommitteeMembers() {
        var ctx = evalContext();
        var outsiderToken = registerSpeaker("mallory@example.com");

        given()
                .header("Authorization", "Bearer " + outsiderToken)
                .contentType("application/json")
                .body(new EvaluationRequest(3))
        .when().post("/api/proposals/" + ctx.proposalId() + "/evaluations")
        .then()
                .statusCode(403)
                .body("error", is("Only committee members can access this proposal's evaluations"));

        given()
                .header("Authorization", "Bearer " + outsiderToken)
        .when().get("/api/proposals/" + ctx.proposalId() + "/evaluations")
        .then()
                .statusCode(403)
                .body("error", is("Only committee members can access this proposal's evaluations"));
    }

    @Test
    void requiresAuthentication() {
        given()
                .contentType("application/json")
                .body(new EvaluationRequest(3))
        .when().post("/api/proposals/1/evaluations")
        .then().statusCode(401);

        given().when().get("/api/proposals/1/evaluations").then().statusCode(401);
    }

    @Test
    void proposalNotFound() {
        var ctx = evalContext();

        given()
                .header("Authorization", "Bearer " + ctx.reviewerToken())
                .contentType("application/json")
                .body(new EvaluationRequest(3))
        .when().post("/api/proposals/999999/evaluations")
        .then()
                .statusCode(404)
                .body("error", is("Proposal not found"));
    }

    @Test
    void rejectsNonEvalProposals() {
        var coordinatorToken = registerCoordinator("grace@example.com");
        var eventId = createEvent(coordinatorToken);
        createCfp(coordinatorToken, eventId);
        var reviewerId = registerSpeakerId("reviewer@example.com");
        addCommitteeMember(coordinatorToken, eventId, reviewerId);
        openCfp(coordinatorToken, eventId);

        var speakerToken = registerSpeaker("ada@example.com");
        var proposalId = submitProposal(speakerToken, eventId);
        var reviewerToken = login("reviewer@example.com", "correct horse");

        given()
                .header("Authorization", "Bearer " + reviewerToken)
                .contentType("application/json")
                .body(new EvaluationRequest(4))
        .when().post("/api/proposals/" + proposalId + "/evaluations")
        .then()
                .statusCode(409)
                .body("error", is("Proposal can only be evaluated while in EVAL status"));

        closeCfp(coordinatorToken, eventId);
        QuarkusTransaction.requiringNew().run(() ->
                proposals.update("status = ?1 where id = ?2",
                        ProposalStatus.APPROVED, asLong(proposalId)));

        given()
                .header("Authorization", "Bearer " + reviewerToken)
                .contentType("application/json")
                .body(new EvaluationRequest(4))
        .when().post("/api/proposals/" + proposalId + "/evaluations")
        .then()
                .statusCode(409)
                .body("error", is("Proposal can only be evaluated while in EVAL status"));
    }

    @Test
    void ownerCannotEvaluateOwnProposal() {
        var coordinatorToken = registerCoordinator("grace@example.com");
        var eventId = createEvent(coordinatorToken);
        createCfp(coordinatorToken, eventId);

        var ownerId = registerSpeakerId("ada@example.com");
        addCommitteeMember(coordinatorToken, eventId, ownerId);
        openCfp(coordinatorToken, eventId);

        var ownerToken = login("ada@example.com", "correct horse");
        var proposalId = submitProposal(ownerToken, eventId);
        closeCfp(coordinatorToken, eventId);

        given()
                .header("Authorization", "Bearer " + ownerToken)
                .contentType("application/json")
                .body(new EvaluationRequest(5))
        .when().post("/api/proposals/" + proposalId + "/evaluations")
        .then()
                .statusCode(403)
                .body("error", is("Authors cannot evaluate their own proposal"));
    }

    @Test
    void coAuthorCannotEvaluateProposal() {
        var coordinatorToken = registerCoordinator("grace@example.com");
        var eventId = createEvent(coordinatorToken);
        createCfp(coordinatorToken, eventId);

        var reviewerId = registerSpeakerId("reviewer@example.com");
        var coAuthorId = registerSpeakerId("katherine@example.com");
        addCommitteeMember(coordinatorToken, eventId, reviewerId);
        addCommitteeMember(coordinatorToken, eventId, coAuthorId);
        openCfp(coordinatorToken, eventId);

        var speakerToken = registerSpeaker("ada@example.com");
        var proposalId = submitProposal(speakerToken, eventId);

        given()
                .header("Authorization", "Bearer " + speakerToken)
                .contentType("application/json")
                .body(new ProposalSpeakerRequest(asLong(coAuthorId)))
        .when().post("/api/proposals/" + proposalId + "/speakers")
        .then().statusCode(201);

        closeCfp(coordinatorToken, eventId);
        var coAuthorToken = login("katherine@example.com", "correct horse");

        given()
                .header("Authorization", "Bearer " + coAuthorToken)
                .contentType("application/json")
                .body(new EvaluationRequest(2))
        .when().post("/api/proposals/" + proposalId + "/evaluations")
        .then()
                .statusCode(403)
                .body("error", is("Authors cannot evaluate their own proposal"));
    }

    @Test
    void rejectsDuplicateEvaluation() {
        var ctx = evalContext();

        given()
                .header("Authorization", "Bearer " + ctx.reviewerToken())
                .contentType("application/json")
                .body(new EvaluationRequest(4))
        .when().post("/api/proposals/" + ctx.proposalId() + "/evaluations")
        .then().statusCode(201);

        given()
                .header("Authorization", "Bearer " + ctx.reviewerToken())
                .contentType("application/json")
                .body(new EvaluationRequest(2))
        .when().post("/api/proposals/" + ctx.proposalId() + "/evaluations")
        .then()
                .statusCode(409)
                .body("error", is("Evaluator already submitted an evaluation for this proposal"));

        given()
                .header("Authorization", "Bearer " + ctx.reviewerToken())
        .when().get("/api/proposals/" + ctx.proposalId() + "/evaluations")
        .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("score", contains(4));
    }

    @Test
    void rejectsInvalidScores() {
        var ctx = evalContext();
        var url = "/api/proposals/" + ctx.proposalId() + "/evaluations";

        for (var score : new Integer[]{0, 6}) {
            given()
                    .header("Authorization", "Bearer " + ctx.reviewerToken())
                    .contentType("application/json")
                    .body(new EvaluationRequest(score))
            .when().post(url)
            .then()
                    .statusCode(400)
                    .body("field", is("score"));
        }

        given()
                .header("Authorization", "Bearer " + ctx.reviewerToken())
                .contentType("application/json")
                .body(Map.of())
        .when().post(url)
        .then()
                .statusCode(400)
                .body("field", is("score"));
    }

    @Test
    void evaluationsCannotBeModified() {
        var ctx = evalContext();
        var url = "/api/proposals/" + ctx.proposalId() + "/evaluations";

        given()
                .header("Authorization", "Bearer " + ctx.reviewerToken())
                .contentType("application/json")
                .body(new EvaluationRequest(4))
        .when().post(url)
        .then().statusCode(201);

        given()
                .header("Authorization", "Bearer " + ctx.reviewerToken())
                .contentType("application/json")
                .body(new EvaluationRequest(1))
        .when().put(url)
        .then().statusCode(404);

        given()
                .header("Authorization", "Bearer " + ctx.reviewerToken())
        .when().delete(url)
        .then().statusCode(404);

        given()
                .header("Authorization", "Bearer " + ctx.reviewerToken())
        .when().delete(url + "/1")
        .then().statusCode(404);
    }

    private EvalContext evalContext() {
        var coordinatorToken = registerCoordinator("grace@example.com");
        var eventId = createEvent(coordinatorToken);
        createCfp(coordinatorToken, eventId);

        var reviewerId = registerSpeakerId("reviewer@example.com");
        addCommitteeMember(coordinatorToken, eventId, reviewerId);
        openCfp(coordinatorToken, eventId);

        var speakerToken = registerSpeaker("ada@example.com");
        var proposalId = submitProposal(speakerToken, eventId);
        closeCfp(coordinatorToken, eventId);

        return new EvalContext(coordinatorToken, eventId, reviewerId,
                login("reviewer@example.com", "correct horse"), speakerToken, proposalId);
    }

    private record EvalContext(String coordinatorToken, Object eventId, Object reviewerId,
            String reviewerToken, String speakerToken, Object proposalId) {
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

    private String registerCoordinator(String email) {
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("User " + email, email, "correct horse"))
        .when().post("/api/coordinators")
        .then().statusCode(201);
        return login(email, "correct horse");
    }

    private String registerSpeaker(String email) {
        registerSpeakerId(email);
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
