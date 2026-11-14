package com.dearrudam.user.boundary;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dearrudam.shared.TestDataCleaner;
import com.dearrudam.user.UserRequirement;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

import static com.dearrudam.user.UserRequirement.Rn.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class PublicProfileTest {

    @Inject
    TestDataCleaner cleaner;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(cleaner::clean);
    }

    @Test
    @UserRequirement({R8_1, R8_4})
    void returnsRequestedProfile() {
        var id = given()
                .contentType("application/json")
                .body(new SpeakerRequest("Alan Turing", "alan@example.com", "correct horse", "Father of CS"))
        .when().post("/api/speakers")
        .then().statusCode(201)
                .extract().path("id");

        given().when().get("/api/users/" + id)
        .then()
                .statusCode(200)
                .body("id", is(id))
                .body("name", is("Alan Turing"))
                .body("bio", is("Father of CS"))
                .body("roles", containsInAnyOrder("SPEAKER"))
                .body("email", nullValue())
                .body("$", aMapWithSize(4));
    }

    @Test
    @UserRequirement(R8_2)
    void omitsBioWhenAbsent() {
        var id = given()
                .contentType("application/json")
                .body(new CoordinatorRequest("Grace Hopper", "grace@example.com", "correct horse"))
        .when().post("/api/coordinators")
        .then().statusCode(201)
                .extract().path("id");

        given().when().get("/api/users/" + id)
        .then()
                .statusCode(200)
                .body("id", is(id))
                .body("name", is("Grace Hopper"))
                .body("roles", containsInAnyOrder("COORDINATOR"))
                .body("bio", nullValue())
                .body("$", aMapWithSize(3));
    }

    @Test
    @UserRequirement(R8_3)
    void returns404ForUnknownUser() {
        given().when().get("/api/users/999999")
        .then()
                .statusCode(404)
                .body("error", is("User not found"))
                .body("field", nullValue());
    }
}
