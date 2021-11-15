package test;

import core.BaseTest;
import io.restassured.RestAssured;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import utils.DataUtils;

import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Cenarios extends BaseTest {

    private static String CONTA_NAME = "Conta" + System.nanoTime();
    private static Integer CONTA_ID;
    private static Integer MOVIMENTACAO_ID;

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
    }

    @Test
    public void CT02_deveIncluirConta() {

        CONTA_ID = given()
                .body("{\"nome\" : \"" + CONTA_NAME + "\"}")
                .when()
                .post("/contas")
                .then()
                .log().all()
                .statusCode(201)
                .extract().path("id")
        ;
    }

    @Test
    public void CT03_deveAlterarConta() {

        given()
                .body("{\"nome\" : \"" + CONTA_NAME + " alterada" + "\"}")
                .when()
                .put("/contas/" + CONTA_ID)
                .then()
                .log().all()
                .statusCode(200)
        ;
    }

    @Test
    public void CT04_naoDeveIncluirContaNomeRepetido() {

        given()
                .body("{\"nome\" : \"" + CONTA_NAME + " alterada" + "\"}")
                .when()
                .post("/contas")
                .then()
                .log().all()
                .statusCode(400)
                .body("error", is("Já existe uma conta com esse nome!"))
        ;
    }

    @Test
    public void CT05_deveInserirMovimentacaoComSucesso() {

        Movimentacao mov = getMovimentacaoValida();

        MOVIMENTACAO_ID = given()
                .body(mov)
                .when()
                .post("/transacoes")
                .then()
                .statusCode(201)
                .extract().path("id")
        ;
    }

    @Test
    public void CT06_deveValidarCamposObrigatoriosMovimentacao() {

        given()
                .when()
                .post("/transacoes")
                .then()
                .statusCode(400)
                .body("$", hasSize(8))
                .body("msg", hasItems(
                        "Data da Movimentação é obrigatório",
                        "Data do pagamento é obrigatório",
                        "Descrição é obrigatório",
                        "Interessado é obrigatório",
                        "Valor é obrigatório",
                        "Conta é obrigatório",
                        "Situação é obrigatório",
                        "Valor deve ser um número"
                ))
        ;
    }

    @Test
    public void CT07_naoDeveCadastrarMovimentacaoFutura() {

        Movimentacao mov = getMovimentacaoValida();
        mov.setData_transacao(DataUtils.getDataDiferencaDias(2));

        given()
                .body(mov)
                .when()
                .post("/transacoes")
                .then()
                .statusCode(400)
                .body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"))
                .body("$", hasSize(1))
        ;
    }

    @Test
    public void CT08_naoDeveRemoverContaMovimentacao() {

        given()
                .when()
                .pathParam("id", CONTA_ID)
                .delete("/contas/{id}")
                .then()
                .statusCode(500)
                .body("detail", is("Key (id)=(" + CONTA_ID + ") is still referenced from table \"transacoes\"."))
        ;
    }

    @Test
    public void CT09_deveCalcularSaldoContas() {

        Movimentacao mov1 = getMovimentacaoValida();
        mov1.setValor(200.00f);
        Movimentacao mov2 = getMovimentacaoValida();
        mov2.setValor(300.00f);

        given()
                .body(mov1)
                .when()
                .post("/transacoes")
                .then()
                .statusCode(201)
        ;

        given()
                .body(mov2)
                .when()
                .post("/transacoes")
                .then()
                .statusCode(201)
        ;

        given()
                .when()
                .get("/saldo")
                .then()
                .log().all()
                .statusCode(200)
                .body("find{it.conta_id == " + CONTA_ID + "}.saldo", is("3000.00"))
        ;
    }

    @Test
    public void CT10_deveRemoverTransacao() {

        given()
                .when()
                .delete("/transacoes/" + MOVIMENTACAO_ID)
                .then()
                .statusCode(204)
        ;
    }

    @Test
    public void CT11_naoDeveAcessarSemTOKEN() {
        FilterableRequestSpecification req = (FilterableRequestSpecification) RestAssured.requestSpecification;
        req.removeHeader("Authorization");

        given()
                .when()
                .get("/contas")
                .then()
                .statusCode(401)
        ;
    }

    public Movimentacao getMovimentacaoValida() {

        Movimentacao mov = new Movimentacao();

        mov.setConta_id(CONTA_ID);
        mov.setDescricao("Movimentação de dinheiro de bitcoin");
        mov.setEnvolvido("Mané Pelé");
        mov.setTipo("REC");
        mov.setData_transacao(DataUtils.getDataDiferencaDias(-1));
        mov.setData_pagamento(DataUtils.getDataDiferencaDias(5));
        mov.setValor(2500f);
        mov.setStatus(true);
        return mov;
    }
}


