package org.example;

import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.example.ApiConstants.*;

public class CreateUserTests {
    private final Faker faker = new Faker();
    private final HelperMethods helper = new HelperMethods();
    private User existingUser;
    private String accessToken;

    @Before
    @Step("Регистрация пользователя для тестов")
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        existingUser = helper.generateRandomUser(faker);
        accessToken = helper.registerUser(existingUser, BASE_URL)
                .statusCode(SC_OK)
                .extract()
                .path("accessToken");
    }

    @Test
    @Step("Успешная регистрация уникального пользователя")
    public void registerNewUserSuccessfully() {
        User newUser = helper.generateRandomUser(faker);

        helper.registerUser(newUser, BASE_URL)
                .statusCode(SC_OK)
                .body("success", is(true))
                .body("user.email", equalTo(newUser.getEmail().toLowerCase()))
                .body("accessToken", startsWith("Bearer "));
    }

    @Test
    @Step("Попытка регистрации уже существующего пользователя")
    public void registerExistingUserShouldFail() {
        helper.registerUser(existingUser, BASE_URL)
                .statusCode(SC_FORBIDDEN)
                .body("success", is(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @Step("Попытка регистрации без обязательных полей")
    public void registerUserWithoutRequiredField() {
        User invalidUser = new User("", "password", "name");

        helper.registerUser(invalidUser, BASE_URL)
                .statusCode(SC_FORBIDDEN)
                .body("success", is(false))
                .body("message", equalTo("Email, password and name are required fields"));
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
