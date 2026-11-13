package com.dearrudam.user.boundary;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dearrudam.user.entity.UserRepository;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class AuthResourceTest {

    @Inject
    UserRepository users;

    @Inject
    EntityManager em;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(() -> users.listAll().forEach(em::remove));
    }

    @Test
    void issuesTokenForValidCredentials() {
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("Grace Hopper", "grace@example.com", "correct horse"))
        .when().post("/api/coordinators")
        .then().statusCode(201);

        String token = given()
                .contentType("application/json")
                .body(new LoginRequest("grace@example.com", "correct horse"))
        .when().post("/api/auth/login")
        .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("tokenType", is("Bearer"))
                .body("expiresIn", greaterThan(0))
        .extract().path("token");

        var payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
        assertThat(payload, containsString("grace@example.com"));
        assertThat(payload, containsString("COORDINATOR"));
        assertThat(payload, containsString("\"sub\""));
    }

    @Test
    void rejectsInvalidCredentials() {
        given()
                .contentType("application/json")
                .body(new CoordinatorRequest("Grace Hopper", "grace@example.com", "correct horse"))
        .when().post("/api/coordinators")
        .then().statusCode(201);

        given()
                .contentType("application/json")
                .body(new LoginRequest("grace@example.com", "wrong password"))
        .when().post("/api/auth/login")
        .then()
                .statusCode(401)
                .body("field", is("password"));

        given()
                .contentType("application/json")
                .body(new LoginRequest("ghost@example.com", "whatever pass"))
        .when().post("/api/auth/login")
        .then().statusCode(401);
    }

    @Test
    void rejectsInvalidInput() {
        given()
                .contentType("application/json")
                .body(new LoginRequest("not-an-email", ""))
        .when().post("/api/auth/login")
        .then()
                .statusCode(400)
                .body("error", notNullValue())
                .body("field", notNullValue());
    }
}
