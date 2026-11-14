package com.dearrudam.draft.boundary;

import java.util.Optional;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dearrudam.draft.DraftRequirement;
import com.dearrudam.proposal.entity.TalkLevel;
import com.dearrudam.shared.TestDataCleaner;
import com.dearrudam.user.boundary.CoordinatorRequest;
import com.dearrudam.user.boundary.LoginRequest;
import com.dearrudam.user.boundary.SpeakerRequest;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

import static com.dearrudam.draft.DraftRequirement.Rn.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class DraftResourceTest {

    @Inject
    TestDataCleaner cleaner;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(cleaner::clean);
    }

    @Test
    @DraftRequirement({R1_1, R1_2, R2_1, R3_1, R4_1, R5_1})
    void speakerManagesDrafts() {
        var token = registerSpeaker("ada@example.com");

        var draftA = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new DraftRequest("My first talk", "All about Java", TalkLevel.BEGINNER, 45))
        .when().post("/api/drafts")
        .then()
                .statusCode(201)
                .body("title", is("My first talk"))
                .body("abstract", is("All about Java"))
                .body("level", is("BEGINNER"))
                .body("durationMinutes", is(45))
                .body("ownerName", is("Speaker ada@example.com"))
                .body("createdAt", notNullValue())
                .extract().path("id");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new DraftRequest("My second talk", "Even more Java", TalkLevel.ADVANCED, 60))
        .when().post("/api/drafts")
        .then().statusCode(201);

        given()
                .header("Authorization", "Bearer " + token)
        .when().get("/api/drafts")
        .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("title", contains("My second talk", "My first talk"))
                .body("ownerName", everyItem(is("Speaker ada@example.com")));

        given()
                .header("Authorization", "Bearer " + token)
        .when().get("/api/drafts/" + draftA)
        .then()
                .statusCode(200)
                .body("id", is(draftA))
                .body("title", is("My first talk"))
                .body("abstract", is("All about Java"))
                .body("level", is("BEGINNER"))
                .body("durationMinutes", is(45))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new UpdateDraftRequest(Optional.of("Renamed talk"), Optional.empty(),
                        Optional.empty(), Optional.empty()))
        .when().put("/api/drafts/" + draftA)
        .then()
                .statusCode(200)
                .body("title", is("Renamed talk"))
                .body("abstract", is("All about Java"))
                .body("level", is("BEGINNER"))
                .body("durationMinutes", is(45))
                .body("updatedAt", notNullValue());

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new UpdateDraftRequest(Optional.empty(), Optional.of("New abstract"),
                        Optional.of(TalkLevel.INTERMEDIATE), Optional.of(30)))
        .when().put("/api/drafts/" + draftA)
        .then()
                .statusCode(200)
                .body("title", is("Renamed talk"))
                .body("abstract", is("New abstract"))
                .body("level", is("INTERMEDIATE"))
                .body("durationMinutes", is(30));

        given()
                .header("Authorization", "Bearer " + token)
        .when().delete("/api/drafts/" + draftA)
        .then().statusCode(204);

        given()
                .header("Authorization", "Bearer " + token)
        .when().get("/api/drafts/" + draftA)
        .then()
                .statusCode(404)
                .body("error", is("Draft not found"));
    }

    @Test
    @DraftRequirement({R1_1, R3_1, R3_3, R4_3, R5_3, R6_1})
    void forbidsNonOwner() {
        var ownerToken = registerSpeaker("ada@example.com");
        var draftId = given()
                .header("Authorization", "Bearer " + ownerToken)
                .contentType("application/json")
                .body(new DraftRequest("Private talk", "Only for me", TalkLevel.BEGINNER, 30))
        .when().post("/api/drafts")
        .then().statusCode(201)
                .extract().path("id");

        registerSpeaker("grace@example.com");
        var otherSpeakerToken = login("grace@example.com", "correct horse");
        var coordinatorToken = registerCoordinator("linus@example.com");

        given()
                .header("Authorization", "Bearer " + otherSpeakerToken)
        .when().get("/api/drafts")
        .then().statusCode(200).body("size()", is(0));

        for (var token : new String[]{otherSpeakerToken, coordinatorToken}) {
            given()
                    .header("Authorization", "Bearer " + token)
            .when().get("/api/drafts/" + draftId)
            .then().statusCode(403);

            given()
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .body(new UpdateDraftRequest(Optional.of("Hijacked"), Optional.empty(),
                            Optional.empty(), Optional.empty()))
            .when().put("/api/drafts/" + draftId)
            .then().statusCode(403);

            given()
                    .header("Authorization", "Bearer " + token)
            .when().delete("/api/drafts/" + draftId)
            .then().statusCode(403);
        }

        given()
                .header("Authorization", "Bearer " + otherSpeakerToken)
        .when().get("/api/drafts/" + draftId)
        .then()
                .statusCode(403)
                .body("error", is("Only the draft owner can manage this draft"));

        given()
                .header("Authorization", "Bearer " + ownerToken)
        .when().get("/api/drafts/" + draftId)
        .then().statusCode(200).body("title", is("Private talk"));
    }

    @Test
    @DraftRequirement({R2_2, R3_2, R4_2, R4_4, R5_2, R6_1})
    void rejectsInvalidUsage() {
        given()
                .contentType("application/json")
                .body(new DraftRequest("Anon talk", "No auth", TalkLevel.BEGINNER, 30))
        .when().post("/api/drafts")
        .then().statusCode(401);

        given().when().get("/api/drafts").then().statusCode(401);
        given().when().get("/api/drafts/1").then().statusCode(401);
        given()
                .contentType("application/json")
                .body(new UpdateDraftRequest(Optional.of("X"), Optional.empty(),
                        Optional.empty(), Optional.empty()))
        .when().put("/api/drafts/1")
        .then().statusCode(401);
        given().when().delete("/api/drafts/1").then().statusCode(401);

        var coordinatorToken = registerCoordinator("linus@example.com");

        given()
                .header("Authorization", "Bearer " + coordinatorToken)
                .contentType("application/json")
                .body(new DraftRequest("Coord talk", "Not allowed", TalkLevel.BEGINNER, 30))
        .when().post("/api/drafts")
        .then().statusCode(403);

        given()
                .header("Authorization", "Bearer " + coordinatorToken)
        .when().get("/api/drafts")
        .then().statusCode(403);

        var token = registerSpeaker("ada@example.com");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new DraftRequest(null, "Missing title", TalkLevel.BEGINNER, 30))
        .when().post("/api/drafts")
        .then()
                .statusCode(400)
                .body("field", is("title"));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new DraftRequest(" ", "Blank title", TalkLevel.BEGINNER, 30))
        .when().post("/api/drafts")
        .then()
                .statusCode(400)
                .body("field", is("title"));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new DraftRequest("No abstract", " ", TalkLevel.BEGINNER, 30))
        .when().post("/api/drafts")
        .then()
                .statusCode(400)
                .body("field", is("abstractText"));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new DraftRequest("Bad level", "Abstract", null, 30))
        .when().post("/api/drafts")
        .then()
                .statusCode(400)
                .body("field", is("level"));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new DraftRequest("Bad duration", "Abstract", TalkLevel.BEGINNER, 0))
        .when().post("/api/drafts")
        .then()
                .statusCode(400)
                .body("field", is("durationMinutes"));

        var draftId = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new DraftRequest("Valid talk", "Has abstract", TalkLevel.BEGINNER, 30))
        .when().post("/api/drafts")
        .then().statusCode(201)
                .extract().path("id");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new UpdateDraftRequest(Optional.of(" "), Optional.empty(),
                        Optional.empty(), Optional.empty()))
        .when().put("/api/drafts/" + draftId)
        .then()
                .statusCode(400)
                .body("field", is("title"));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new UpdateDraftRequest(Optional.empty(), Optional.empty(),
                        Optional.empty(), Optional.of(0)))
        .when().put("/api/drafts/" + draftId)
        .then()
                .statusCode(400)
                .body("field", is("durationMinutes"));

        given()
                .header("Authorization", "Bearer " + token)
        .when().get("/api/drafts/999999")
        .then()
                .statusCode(404)
                .body("error", is("Draft not found"));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new UpdateDraftRequest(Optional.of("X"), Optional.empty(),
                        Optional.empty(), Optional.empty()))
        .when().put("/api/drafts/999999")
        .then()
                .statusCode(404)
                .body("error", is("Draft not found"));

        given()
                .header("Authorization", "Bearer " + token)
        .when().delete("/api/drafts/999999")
        .then()
                .statusCode(404)
                .body("error", is("Draft not found"));
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
