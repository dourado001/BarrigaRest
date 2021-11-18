package cenarios;

import core.BaseTest;
import io.restassured.RestAssured;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import utils.BarrigaUtils;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class ContasTest extends BaseTest {

    @Test
    public void deveIncluirConta() {

        given()
                .body("{\"nome\" : \"Conta inserida\"}")
                .when()
                .post("/contas")
                .then()
                .statusCode(201)
        ;
    }

    @Test
    public void deveAlterarConta() {
        Integer CONTA_ID = BarrigaUtils.getIdContaPeloNome("Conta para alterar");

        given()
                .body("{\"nome\" : \"Conta alterada\"}")
                .pathParam("id", CONTA_ID)
                .when()
                .put("/contas/{id}")
                .then()
                .statusCode(200)
                .body("nome", is("Conta alterada"))
        ;
    }

    @Test
    public void naoDeveIncluirContaNomeRepetido() {

        given()
                .body("{\"nome\" : \"Conta mesmo nome\"}")
                .when()
                .post("/contas")
                .then()
                .log().all()
                .statusCode(400)
                .body("error", is("Já existe uma conta com esse nome!"))
        ;
    }

}


