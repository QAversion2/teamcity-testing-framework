package com.example.teamcity.api;

import org.testng.annotations.Test;
import org.apache.http.HttpStatus;
import io.restassured.RestAssured;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.spec.Specifications;

public class BuildConfigurationTest extends BaseApiTest {

    @Test
    public void buildConfigurationTest() {

        var user = User.builder()
                .username("admin2")
                .password("admin2")
                .build();

        var token = RestAssured
                .given()
                .spec(Specifications.getSpec().authSpec(user))
                .get("/authenticationTest.html?csrf")
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .extract().asString();
        System.out.println(token);
    }
}
