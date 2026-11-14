package com.dearrudam.review.boundary;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.dearrudam.event.boundary.EventRequest;
import com.dearrudam.proposal.entity.ProposalRepository;
import com.dearrudam.proposal.entity.ProposalStatus;
import com.dearrudam.review.ReviewRequirement.Rn;
import com.dearrudam.shared.TestDataCleaner;
import com.dearrudam.user.boundary.CoordinatorRequest;
import com.dearrudam.user.boundary.LoginRequest;
import com.dearrudam.user.boundary.SpeakerRequest;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

import static com.dearrudam.review.ReviewRequirement.Rn.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@QuarkusTest
class ReviewResourceTest {

    private static final String PASSWORD = "correct horse";

    @Inject
    TestDataCleaner cleaner;

    @Inject
    ProposalRepository proposals;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(cleaner::clean);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("submitCases")
    void submitReview(Rn req, SubmitCase c) {
        var world = world(c.status());
        var response = given()
                .header("Authorization", "Bearer " + world.token(c.actor()))
                .contentType("application/json")
                .body(new ReviewRequest(c.score(), c.comment()))
        .when().put("/api/proposals/" + world.proposalId() + "/reviews");

        response.then().statusCode(c.expectedStatus());
        if (c.expectedField() != null) {
            response.then().body("field", is(c.expectedField()));
        }
        if (c.expectedStatus() != 200) {
            given()
                    .header("Authorization", "Bearer " + world.committeeToken())
            .when().get("/api/proposals/" + world.proposalId() + "/reviews")
            .then()
                    .statusCode(200)
                    .body("size()", is(0));
            return;
        }

        if (c.resubmitScore() != null) {
            given()
                    .header("Authorization", "Bearer " + world.token(c.actor()))
                    .contentType("application/json")
                    .body(new ReviewRequest(c.resubmitScore(), c.resubmitComment()))
            .when().put("/api/proposals/" + world.proposalId() + "/reviews")
            .then().statusCode(200);
        }

        var expectedScore = c.resubmitScore() != null ? c.resubmitScore() : c.score();
        var expectedComment = c.resubmitComment() != null ? c.resubmitComment() : c.comment();
        given()
                .header("Authorization", "Bearer " + world.token(c.actor()))
        .when().get("/api/proposals/" + world.proposalId() + "/reviews")
        .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("score", contains(expectedScore))
                .body("comment", contains(expectedComment))
                .body("reviewerName", contains("Speaker committee@example.com"))
                .body("proposalId", contains(world.proposalId()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("listCases")
    void listReviews(Rn req, ListCase c) {
        var world = listWorld();
        var response = given()
                .header("Authorization", "Bearer " + world.token(c.actor()))
        .when().get("/api/proposals/" + world.proposalId() + "/reviews");

        response.then().statusCode(c.expectedStatus());
        if (c.expectedStatus() == 200) {
            response.then()
                    .body("size()", is(2))
                    .body("score", containsInAnyOrder(4, 2))
                    .body("comment", containsInAnyOrder("Solid proposal", "Needs more detail"))
                    .body("reviewerName", containsInAnyOrder(
                            "Speaker committee@example.com", "Speaker committee2@example.com"));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("queueCases")
    void reviewQueue(Rn req, QueueCase c) {
        var world = queueWorld();
        var response = given()
                .header("Authorization", "Bearer " + world.token(c.actor()))
        .when().get("/api/events/" + world.eventId() + "/cfp/review-queue");

        response.then().statusCode(c.expectedStatus());
        if (c.expectedStatus() == 200) {
            response.then()
                    .body("size()", is(2))
                    .body("id", containsInAnyOrder(world.evalIds().get(0), world.evalIds().get(1)))
                    .body("title", containsInAnyOrder("Talk A", "Talk B"));
        }
    }

    static Stream<Arguments> submitCases() {
        return Stream.of(
                arguments(R1_1, new SubmitCase(Actor.COMMITTEE, ProposalStatus.EVAL,
                        4, "Solid proposal", 200, null, null, null)),
                arguments(R1_2, new SubmitCase(Actor.COMMITTEE, ProposalStatus.EVAL,
                        4, "First take", 200, null, 4, "First take")),
                arguments(R1_3, new SubmitCase(Actor.COMMITTEE, ProposalStatus.EVAL,
                        4, "First take", 200, null, 5, "Updated review")),
                arguments(R1_4, new SubmitCase(Actor.OTHER, ProposalStatus.EVAL,
                        4, "Solid proposal", 403, null, null, null)),
                arguments(R1_4, new SubmitCase(Actor.FOREIGN_COMMITTEE, ProposalStatus.EVAL,
                        4, "Solid proposal", 403, null, null, null)),
                arguments(R1_5, new SubmitCase(Actor.COMMITTEE, ProposalStatus.SUBMITTED,
                        4, "Solid proposal", 409, null, null, null)),
                arguments(R1_6, new SubmitCase(Actor.COMMITTEE, ProposalStatus.EVAL,
                        7, "Solid proposal", 400, "score", null, null)),
                arguments(R1_6, new SubmitCase(Actor.COMMITTEE, ProposalStatus.EVAL,
                        0, "Solid proposal", 400, "score", null, null)),
                arguments(R1_6, new SubmitCase(Actor.COMMITTEE, ProposalStatus.EVAL,
                        6, "Solid proposal", 200, null, null, null)),
                arguments(R1_7, new SubmitCase(Actor.COMMITTEE, ProposalStatus.EVAL,
                        4, " ", 400, "comment", null, null)));
    }

    static Stream<Arguments> listCases() {
        return Stream.of(
                arguments(R2_1, new ListCase(Actor.COMMITTEE, 200)),
                arguments(R2_2, new ListCase(Actor.OWNER, 200)),
                arguments(R2_3, new ListCase(Actor.OTHER, 403)),
                arguments(R2_3, new ListCase(Actor.FOREIGN_COMMITTEE, 403)));
    }

    static Stream<Arguments> queueCases() {
        return Stream.of(
                arguments(R3_1, new QueueCase(Actor.COMMITTEE, 200)),
                arguments(R3_2, new QueueCase(Actor.OTHER, 403)),
                arguments(R3_2, new QueueCase(Actor.FOREIGN_COMMITTEE, 403)),
                arguments(R3_2, new QueueCase(Actor.OWNER, 403)));
    }

    private enum Actor {
        COMMITTEE, OTHER, OWNER, FOREIGN_COMMITTEE
    }

    private record SubmitCase(Actor actor, ProposalStatus status, Integer score, String comment,
                              int expectedStatus, String expectedField,
                              Integer resubmitScore, String resubmitComment) {
    }

    private record ListCase(Actor actor, int expectedStatus) {
    }

    private record QueueCase(Actor actor, int expectedStatus) {
    }

    private record World(Object eventId, Object proposalId, String ownerToken,
                         String committeeToken, String otherToken, String foreignToken) {
        String token(Actor actor) {
            return switch (actor) {
                case COMMITTEE -> committeeToken;
                case OTHER -> otherToken;
                case OWNER -> ownerToken;
                case FOREIGN_COMMITTEE -> foreignToken;
            };
        }
    }

    private record ListWorld(Object proposalId, String ownerToken, String committeeToken,
                             String otherToken, String foreignToken) {
        String token(Actor actor) {
            return switch (actor) {
                case COMMITTEE -> committeeToken;
                case OTHER -> otherToken;
                case OWNER -> ownerToken;
                case FOREIGN_COMMITTEE -> foreignToken;
            };
        }
    }

    private record QueueWorld(Object eventId, List<Object> evalIds, String ownerToken,
                              String committeeToken, String otherToken, String foreignToken) {
        String token(Actor actor) {
            return switch (actor) {
                case COMMITTEE -> committeeToken;
                case OTHER -> otherToken;
                case OWNER -> ownerToken;
                case FOREIGN_COMMITTEE -> foreignToken;
            };
        }
    }

    private World world(ProposalStatus status) {
        var ownerToken = registerCoordinator("owner@example.com");
        var eventId = createEvent(ownerToken);
        createCfp(ownerToken, eventId);
        addCommitteeMember(ownerToken, eventId, registerSpeakerId("committee@example.com"));
        var committeeToken = login("committee@example.com", PASSWORD);
        openCfp(ownerToken, eventId);
        var speakerToken = registerSpeaker("speaker@example.com");
        var proposalId = submitProposal(speakerToken, eventId, "Talk A");
        var otherToken = registerSpeaker("other@example.com");
        if (status == ProposalStatus.EVAL) {
            closeCfp(ownerToken, eventId);
        } else if (status != ProposalStatus.SUBMITTED) {
            throw new IllegalArgumentException("Unsupported proposal status: " + status);
        }
        return new World(eventId, proposalId, ownerToken, committeeToken, otherToken,
                foreignCommitteeToken());
    }

    private ListWorld listWorld() {
        var ownerToken = registerCoordinator("owner@example.com");
        var eventId = createEvent(ownerToken);
        createCfp(ownerToken, eventId);
        addCommitteeMember(ownerToken, eventId, registerSpeakerId("committee@example.com"));
        addCommitteeMember(ownerToken, eventId, registerSpeakerId("committee2@example.com"));
        var committeeToken = login("committee@example.com", PASSWORD);
        openCfp(ownerToken, eventId);
        var speakerToken = registerSpeaker("speaker@example.com");
        var proposalId = submitProposal(speakerToken, eventId, "Talk A");
        var otherToken = registerSpeaker("other@example.com");
        closeCfp(ownerToken, eventId);
        putReview(committeeToken, proposalId, 4, "Solid proposal");
        putReview(login("committee2@example.com", PASSWORD), proposalId, 2, "Needs more detail");
        return new ListWorld(proposalId, ownerToken, committeeToken, otherToken,
                foreignCommitteeToken());
    }

    private QueueWorld queueWorld() {
        var ownerToken = registerCoordinator("owner@example.com");
        var eventId = createEvent(ownerToken);
        createCfp(ownerToken, eventId);
        addCommitteeMember(ownerToken, eventId, registerSpeakerId("committee@example.com"));
        var committeeToken = login("committee@example.com", PASSWORD);
        openCfp(ownerToken, eventId);
        var speakerToken = registerSpeaker("speaker@example.com");
        var evalA = submitProposal(speakerToken, eventId, "Talk A");
        var evalB = submitProposal(speakerToken, eventId, "Talk B");
        var approved = submitProposal(speakerToken, eventId, "Talk C");
        var otherToken = registerSpeaker("other@example.com");
        closeCfp(ownerToken, eventId);
        forceStatus(approved, ProposalStatus.APPROVED);
        putReview(committeeToken, evalA, 5, "Excellent");

        var foreignOwnerToken = registerCoordinator("foreign-owner@example.com");
        var foreignEventId = createEvent(foreignOwnerToken);
        createCfp(foreignOwnerToken, foreignEventId);
        addCommitteeMember(foreignOwnerToken, foreignEventId, registerSpeakerId("foreign@example.com"));
        var foreignToken = login("foreign@example.com", PASSWORD);
        openCfp(foreignOwnerToken, foreignEventId);
        submitProposal(speakerToken, foreignEventId, "Foreign talk");
        closeCfp(foreignOwnerToken, foreignEventId);

        return new QueueWorld(eventId, List.of(evalA, evalB), ownerToken, committeeToken,
                otherToken, foreignToken);
    }

    private String foreignCommitteeToken() {
        var ownerToken = registerCoordinator("foreign-owner@example.com");
        var eventId = createEvent(ownerToken);
        createCfp(ownerToken, eventId);
        addCommitteeMember(ownerToken, eventId, registerSpeakerId("foreign@example.com"));
        return login("foreign@example.com", PASSWORD);
    }

    private void putReview(String token, Object proposalId, int score, String comment) {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new ReviewRequest(score, comment))
        .when().put("/api/proposals/" + proposalId + "/reviews")
        .then().statusCode(200);
    }

    private void forceStatus(Object proposalId, ProposalStatus status) {
        QuarkusTransaction.requiringNew().run(() ->
                proposals.update("status = ?1 where id = ?2", status, asLong(proposalId)));
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

    private Object submitProposal(String token, Object eventId, String title) {
        return given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(Map.of("eventId", asLong(eventId), "title", title, "abstract", "Abstract",
                        "level", "BEGINNER", "durationMinutes", 30, "saveAsDraft", false))
        .when().post("/api/proposals")
        .then().statusCode(201)
                .extract().path("id");
    }

    private String registerCoordinator(String email) {
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("User " + email, email, PASSWORD))
        .when().post("/api/coordinators")
        .then().statusCode(201);
        return login(email, PASSWORD);
    }

    private String registerSpeaker(String email) {
        given()
                .contentType("application/json")
                .body(new SpeakerRequest("Speaker " + email, email, PASSWORD, "Bio"))
        .when().post("/api/speakers")
        .then().statusCode(201);
        return login(email, PASSWORD);
    }

    private Object registerSpeakerId(String email) {
        return given()
                .contentType("application/json")
                .body(new SpeakerRequest("Speaker " + email, email, PASSWORD, "Bio"))
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
