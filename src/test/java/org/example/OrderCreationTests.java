package org.example;

import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.Order;
import org.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.example.ApiConstants.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

public class OrderCreationTests {
    private final Faker faker = new Faker();
    private final HelperMethods helper = new HelperMethods();
    private User testUser;
    private String accessToken;
    private List<String> validIngredients;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        testUser = helper.generateRandomUser(faker);

        Response response = helper.registerUser(testUser, BASE_URL)
                .statusCode(SC_OK)
                .extract()
                .response();

        accessToken = response.path("accessToken");

        validIngredients = helper.getIngredientIds(BASE_URL)
                .extract()
                .path("data._id");

        if (validIngredients == null || validIngredients.size() < 2) {
            throw new IllegalStateException("Недостаточно ингредиентов для теста.");
        }
    }

    @Test
    @Step("Создание заказа с авторизацией и валидными ингредиентами")
    public void createOrderWithAuthAndIngredients() {
        Order order = new Order(validIngredients.subList(0, 2));

        given()
                .header("Authorization", accessToken)
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post(ORDERS_PATH)
                .then()
                .statusCode(SC_OK)
                .body("success", is(true))
                .body("order.number", notNullValue());
    }

    @Test
    @Step("Создание заказа без авторизации и с ингредиентами")
    public void createOrderWithoutAuth() {
        Order order = new Order(validIngredients.subList(0, 2));

        given()
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post(ORDERS_PATH)
                .then()
                .statusCode(SC_OK)
                .body("success", is(true))
                .body("order.number", notNullValue());
    }

    @Test
    @Step("Создание заказа с авторизацией, но без ингредиентов")
    public void createOrderWithAuthNoIngredients() {
        Order order = new Order(Collections.emptyList());

        given()
                .header("Authorization", accessToken)
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post(ORDERS_PATH)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .body("success", is(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @Step("Создание заказа без авторизации и без ингредиентов")
    public void createOrderWithoutAuthNoIngredients() {
        Order order = new Order(Collections.emptyList());

        given()
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post(ORDERS_PATH)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .body("success", is(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @Step("Создание заказа с неверным хешем ингредиента")
    public void createOrderWithInvalidIngredientHash() {
        Order order = new Order(Collections.singletonList("invalid_ingredient_id"));

        given()
                .header("Authorization", accessToken)
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post(ORDERS_PATH)
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            helper.deleteUser(accessToken, BASE_URL)
                    .statusCode(SC_ACCEPTED);
        }
    }
}