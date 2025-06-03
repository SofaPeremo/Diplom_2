package org.example;

import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.User;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.example.ApiConstants.*;

public class UserLoginTests {
    private final Faker faker = new Faker();
    private final HelperMethods helper = new HelperMethods();
    private User registeredUser;
    private String accessToken;

    @Before
    @Step("Регистрация тестового пользователя")
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        registeredUser = helper.generateRandomUser(faker);

        Response response = helper.registerUser(registeredUser, BASE_URL)
                .statusCode(SC_OK)
                .extract()
                .response();

        this.accessToken = response.path("accessToken");
    }

    @Test
    @Step("Успешная авторизация существующего пользователя")
    public void successfulLoginWithValidCredentials() {
        given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .body(registeredUser)
                .post(LOGIN_PATH)
                .then()
                .statusCode(SC_OK)
                .body("success", is(true))
                .body("accessToken", startsWith("Bearer "))
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(registeredUser.getEmail().toLowerCase()))
                .body("user.name", equalTo(registeredUser.getName()));
    }

    @Test
    @Step("Попытка авторизации с неверным логином")
    public void loginWithInvalidEmailShouldFail() {
        User invalidUser = new User(
                "wrong_email_123",
                registeredUser.getPassword(),
                registeredUser.getName()
        );

        given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .body(invalidUser)
                .post(LOGIN_PATH)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @Step("Попытка авторизации с неверным паролем")
    public void loginWithInvalidPasswordShouldFail() {
        User invalidUser = new User(
                registeredUser.getEmail(),
                "wrong_password_123",
                registeredUser.getName()
        );

        given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .body(invalidUser)
                .post(LOGIN_PATH)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @Step("Попытка авторизации с пустыми полями")
    public void loginWithEmptyFieldsFail() {
        User invalidUser = new User(
                "",
                "",
                ""
        );

        given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .body(invalidUser)
                .post(LOGIN_PATH)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @After
    @Step("Удаление тестового пользователя")
    public void tearDown() {
        if (accessToken != null) {
            helper.deleteUser(accessToken, BASE_URL)
                    .statusCode(SC_ACCEPTED);
        }
    }
}
