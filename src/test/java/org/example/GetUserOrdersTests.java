package org.example;

import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.User;
import org.Order;
import org.junit.*;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.example.ApiConstants.*;

public class GetUserOrdersTests {
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
                .jsonPath()
                .getList("data._id", String.class);

        if (validIngredients == null || validIngredients.size() < 2) {
            throw new IllegalStateException("Недостаточно ингредиентов для теста.");
        }

        // Создание хотя бы одного заказа для пользователя
        Order order = new Order(validIngredients.subList(0, 2));
        given()
                .header("Authorization", accessToken)
                .header("Content-type", "application/json")
                .body(order)
                .post(ORDERS_PATH)
                .then()
                .statusCode(SC_OK);
    }

    @Test
    @Step("Получение заказов авторизованного пользователя")
    public void getUserOrdersWithAuthorization() {
        given()
                .header("Authorization", accessToken)
                .get(ORDERS_PATH)
                .then()
                .statusCode(SC_OK)
                .body("success", is(true))
                .body("orders", notNullValue())
                .body("orders[0]._id", notNullValue())
                .body("orders[0].status", notNullValue());
    }

    @Test
    @Step("Получение заказов без авторизации")
    public void getUserOrdersWithoutAuthorization() {
        given()
                .get(ORDERS_PATH)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", equalTo("You should be authorised"));
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            helper.deleteUser(accessToken, BASE_URL).statusCode(SC_ACCEPTED);
        }
    }

}

