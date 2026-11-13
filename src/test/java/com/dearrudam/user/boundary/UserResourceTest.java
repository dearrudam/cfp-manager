package com.dearrudam.user.boundary;

import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dearrudam.user.entity.UserRepository;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class UserResourceTest {

    @Inject
    UserRepository users;

    @Inject
    EntityManager em;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(() -> users.listAll().forEach(em::remove));
    }

    @Test
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
    void requiresAuthentication() {
        given().when().get("/api/users/me").then().statusCode(401);

        given()
                .header("Authorization", "Bearer not.a.token")
        .when().get("/api/users/me")
        .then().statusCode(401);
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
