package cenarios;

import core.BaseTest;
import io.restassured.RestAssured;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import pojo.Movimentacao;
import utils.BarrigaUtils;
import utils.DataUtils;

import static io.restassured.RestAssured.given;

public class AuthTest extends BaseTest {

    @Test
    public void naoDeveAcessarSemTOKEN() {
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


