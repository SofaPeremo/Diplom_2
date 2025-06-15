package org.example;

public class ApiConstants {
    public static final String BASE_URL = "https://stellarburgers.nomoreparties.site";
    public static final String ORDERS_PATH = "/api/orders";
    public static final String AUTH_PATH = "/api/auth";
    public static final String LOGIN_PATH = AUTH_PATH + "/login";
    public static final String USER_PATH = AUTH_PATH + "/user";

    public static final int SC_OK = 200;
    public static final int SC_ACCEPTED = 202;
    public static final int SC_BAD_REQUEST = 400;
    public static final int SC_UNAUTHORIZED = 401;
    public static final int SC_FORBIDDEN = 403;
    public static final int SC_INTERNAL_SERVER_ERROR = 500;


}