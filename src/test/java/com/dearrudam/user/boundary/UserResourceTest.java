package com.dearrudam.user.boundary;

import java.util.Optional;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dearrudam.shared.TestDataCleaner;
import com.dearrudam.user.UserRequirement;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

import static com.dearrudam.user.UserRequirement.Rn.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class UserResourceTest {

    @Inject
    TestDataCleaner cleaner;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(cleaner::clean);
    }

    @Test
    @UserRequirement(R6_1)
    void returnsOwnProfile() {
        given()
                .contentType("application/json")
                .body(new SpeakerRequest("Alan Turing", "alan@example.com", "correct horse", "Father of CS"))
        .when().post("/api/speakers")
        .then().statusCode(201);

        var token = login("alan@example.com", "correct horse");

        given()
                .header("Authorization", "Bearer " + token)
        .when().get("/api/users/me")
        .then()
                .statusCode(200)
                .body("email", is("alan@example.com"))
                .body("name", is("Alan Turing"))
                .body("bio", is("Father of CS"))
                .body("roles", containsInAnyOrder("SPEAKER"))
                .body("createdAt", notNullValue());
    }

    @Test
    @UserRequirement(R7_1)
    void updatesNameAndBio() {
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("Grace Hopper", "grace@example.com", "correct horse"))
        .when().post("/api/coordinators")
        .then().statusCode(201);

        var token = login("grace@example.com", "correct horse");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new UpdateProfileRequest(Optional.of("Grace M. Hopper"), Optional.empty()))
        .when().put("/api/users/me")
        .then()
                .statusCode(200)
                .body("name", is("Grace M. Hopper"))
                .body("email", is("grace@example.com"))
                .body("roles", containsInAnyOrder("COORDINATOR"));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new UpdateProfileRequest(Optional.empty(), Optional.of("COBOL pioneer")))
        .when().put("/api/users/me")
        .then()
                .statusCode(200)
                .body("name", is("Grace M. Hopper"))
                .body("bio", is("COBOL pioneer"));

        given()
                .header("Authorization", "Bearer " + token)
        .when().get("/api/users/me")
        .then()
                .statusCode(200)
                .body("name", is("Grace M. Hopper"))
                .body("bio", is("COBOL pioneer"));
    }

    @Test
    @UserRequirement({R6_2, R7_3})
    void requiresAuthentication() {
        given().when().get("/api/users/me").then().statusCode(401);

        given()
                .header("Authorization", "Bearer not.a.token")
        .when().get("/api/users/me")
        .then().statusCode(401);

        given()
                .contentType("application/json")
                .body(new UpdateProfileRequest(Optional.of("New Name"), Optional.empty()))
        .when().put("/api/users/me")
        .then().statusCode(401);
    }

    @Test
    @UserRequirement(R7_2)
    void rejectsBlankName() {
        given()
                .contentType("application/json")
                .body(new SpeakerRequest("Alan Turing", "alan@example.com", "correct horse", "Bio"))
        .when().post("/api/speakers")
        .then().statusCode(201);

        var token = login("alan@example.com", "correct horse");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new UpdateProfileRequest(Optional.of(" "), Optional.empty()))
        .when().put("/api/users/me")
        .then()
                .statusCode(400)
                .body("error", notNullValue())
                .body("field", is("name"));
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
