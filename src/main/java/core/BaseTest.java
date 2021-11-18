package core;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.parsing.Parser;
import io.restassured.specification.ResponseSpecification;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class BaseTest implements Constantes {

    @BeforeClass
    public static void setup(){
        baseURI = URL_BASE;
        port = PORT;
        basePath = BASE_PATH;

        RequestSpecBuilder reqBuilder = new RequestSpecBuilder();
        reqBuilder.setContentType(APP_CONTENT_TYPE);
        requestSpecification = reqBuilder.build();

        ResponseSpecBuilder respBuilder = new ResponseSpecBuilder();
        respBuilder.expectResponseTime(lessThan(MAX_TIMEOUT));
        respBuilder.setDefaultParser(Parser.JSON);
        responseSpecification = respBuilder.build();

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
