package org.example;

import com.github.javafaker.Faker;
import io.restassured.response.ValidatableResponse;
import org.User;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

public class HelperMethods {
    private static final String REGISTER_PATH = "/api/auth/register";
    private static final String USER_PATH = "/api/auth/user";

    public User generateRandomUser(Faker faker) {
        return new User(
                faker.internet().emailAddress(),
                faker.internet().password(),
                faker.name().username()
        );
    }

    public ValidatableResponse registerUser(User user, String baseUri) {
        return given()
                .baseUri(baseUri)
                .header("Content-type", "application/json")
                .body(user)
                .post(REGISTER_PATH)
                .then();
    }

    public ValidatableResponse deleteUser(String token, String baseUri) {
        return given()
                .baseUri(baseUri)
                .header("Authorization", token)
                .delete(USER_PATH)
                .then();
    }

    public ValidatableResponse updateAndVerifyUser(User updates, String token, String baseUri, User originalUser) {
        return given()
                .baseUri(baseUri)
                .header("Authorization", token)
                .header("Content-type", "application/json")
                .body(updates)
                .patch(USER_PATH)
                .then()
                .body("success", is(true))
                .body("user.email", anyOf(
                        equalTo(updates.getEmail() != null ? updates.getEmail().toLowerCase() : originalUser.getEmail()),
                        nullValue()))
                .body("user.name", anyOf(
                        equalTo(updates.getName() != null ? updates.getName() : originalUser.getName()),
                        nullValue()));
    }

    public ValidatableResponse getIngredientIds(String baseUri) {
        return given()
                .baseUri(baseUri)
                .header("Content-type", "application/json")
                .get("/api/ingredients")
                .then()
                .statusCode(200);
    }

}