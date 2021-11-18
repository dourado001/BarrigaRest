package cenarios;

import core.BaseTest;
import io.restassured.RestAssured;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.BeforeClass;
import org.junit.Test;
import pojo.Movimentacao;
import utils.BarrigaUtils;
import utils.DataUtils;

import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class MovimentacoesTest extends BaseTest {

    @Test
    public void deveInserirMovimentacaoComSucesso() {

        Movimentacao mov = getMovimentacaoValida();

        given()
                .body(mov)
                .when()
                .post("/transacoes")
                .then()
                .statusCode(201)
                .extract().path("id")
        ;
    }

    @Test
    public void deveValidarCamposObrigatoriosMovimentacao() {

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
    public void naoDeveCadastrarMovimentacaoFutura() {
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
    public void naoDeveRemoverContaMovimentacao() {

        Integer CONTA_ID = given()
                .when()
                .get("/contas?nome=Conta com movimentacao")
                .then()
                .statusCode(200)
                .extract().path("id[0]");

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
    public void deveRemoverTransacao() {
        Integer MOVIMENTACAO_ID = given()
                .when()
                .get("/transacoes?descricao=Movimentacao para exclusao")
                .then()
                .statusCode(200)
                .extract().path("id[0]");

        given()
                .when()
                .delete("/transacoes/" + MOVIMENTACAO_ID)
                .then()
                .statusCode(204)
        ;
    }

    private Movimentacao getMovimentacaoValida() {
        Movimentacao mov = new Movimentacao();
        mov.setConta_id(BarrigaUtils.getIdContaPeloNome("Conta para movimentacoes"));
//		mov.setUsuario_id(usuario_id);
        mov.setDescricao("Descricao da movimentacao");
        mov.setEnvolvido("Envolvido na mov");
        mov.setTipo("REC");
        mov.setData_transacao(DataUtils.getDataDiferencaDias(-1));
        mov.setData_pagamento(DataUtils.getDataDiferencaDias(5));
        mov.setValor(100f);
        mov.setStatus(true);
        return mov;
    }
}


