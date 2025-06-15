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

public class UserUpdateTests {
    private final Faker faker = new Faker();
    private final HelperMethods helper = new HelperMethods();
    private User originalUser;
    private String accessToken;

    @Before
    @Step("Регистрация тестового пользователя")
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        originalUser = helper.generateRandomUser(faker);

        Response response = helper.registerUser(originalUser, BASE_URL)
                .statusCode(SC_OK)
                .extract()
                .response();

        this.accessToken = response.path("accessToken");
    }

    @Test
    @Step("Успешное изменение email авторизованным пользователем")
    public void updateEmailWithAuthorization() {
        User updatedUser = new User(
                faker.internet().emailAddress(),
                null,
                null
        );

        helper.updateAndVerifyUser(updatedUser, accessToken, BASE_URL, originalUser)
                .statusCode(SC_OK);
    }

    @Test
    @Step("Успешное изменение пароля авторизованным пользователем")
    public void updatePasswordWithAuthorization() {
        String newPassword = faker.internet().password();
        User updatedUser = new User(
                originalUser.getEmail(),
                newPassword,
                originalUser.getName()
        );

        helper.updateAndVerifyUser(updatedUser, accessToken, BASE_URL, originalUser)
                .statusCode(SC_OK);
    }

    @Test
    @Step("Успешное изменение имени авторизованным пользователем")
    public void updateNameWithAuthorization() {
        String newName = faker.name().username();
        User updatedUser = new User(
                originalUser.getEmail(),
                originalUser.getPassword(),
                newName
        );

        helper.updateAndVerifyUser(updatedUser, accessToken, BASE_URL, originalUser)
                .statusCode(SC_OK);
    }

    @Test
    @Step("Попытка изменения почты и имени без авторизации")
    public void updateWithoutAuthorizationShouldFail() {
        User updatedUser = new User(
                faker.internet().emailAddress(),
                faker.internet().password(),
                faker.name().username()
        );

        given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .body(updatedUser)
                .patch(USER_PATH)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", equalTo("You should be authorised"));
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

