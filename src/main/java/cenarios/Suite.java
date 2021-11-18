package cenarios;

import core.BaseTest;
import org.junit.runners.Suite.SuiteClasses;
import io.restassured.RestAssured;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static io.restassured.RestAssured.given;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
        ContasTest.class,
        MovimentacoesTest.class,
        SaldoTest.class,
        AuthTest.class,
})
public class
Suite extends BaseTest {

    @BeforeClass
    public static void login() {

        HashMap<String, String> login = new HashMap<String, String>();
        login.put("email", EMAIL);
        login.put("senha", SENHA);

        String TOKEN = given()
                .body(login)
                .when()
                .post("/signin")
                .then()
                .statusCode(200)
                .extract().path("token");

        RestAssured.requestSpecification.header("Authorization", "JWT " + TOKEN);
        RestAssured.get("/reset").then().statusCode(200);
    }
}
