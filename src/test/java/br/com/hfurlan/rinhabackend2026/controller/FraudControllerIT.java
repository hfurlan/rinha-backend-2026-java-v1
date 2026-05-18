package br.com.hfurlan.rinhabackend2026.controller;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class FraudControllerIT {

    @Value("${server.port}")
    private int serverPort;

    @Test
    void getReady_ReferencesAlreadyLoaded_ShouldReturn200() {
        given().
                port(serverPort).
        when().
            get("/ready").
        then().
            statusCode(HttpStatus.OK.value());
    }

    @Test
    void postPayments_ValidData_ShouldReturn200() {
        given().
                port(serverPort).contentType(ContentType.JSON).body(FraudControllerIT.class.getResourceAsStream("/json/request.json")).
                when().
                post("/fraud-score").
                then().
                statusCode(HttpStatus.OK.value());
    }
}