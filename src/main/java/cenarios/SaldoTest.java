package cenarios;

import core.BaseTest;
import io.restassured.RestAssured;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import pojo.Movimentacao;
import utils.BarrigaUtils;
import utils.DataUtils;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class SaldoTest extends BaseTest {

    @Test
    public void deveCalcularSaldoContas() {
        Integer CONTA_ID = given()
                .when()
                .get("/contas?nome=Conta para saldo")
                .then()
                .statusCode(200)
                .extract().path("id[0]");

        given()
                .when()
                .get("/saldo")
                .then()
                .log().all()
                .statusCode(200)
                .body("find{it.conta_id == " + CONTA_ID + "}.saldo", is("534.00"))
        ;
    }

    public Movimentacao getMovimentacaoValida() {

        Movimentacao mov = new Movimentacao();

        mov.setConta_id(BarrigaUtils.getIdContaPeloNome("Conta para movimentacoes"));
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


