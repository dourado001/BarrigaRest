package utils;

import io.restassured.RestAssured;

public class BarrigaUtils {

    public static int getIdContaPeloNome(String nome){
        return RestAssured.given().get("/contas?nome="+nome).then().extract().path("id[0]");
    }

    public static Integer getIdMovimentacaoPeloNome(String desc){
        return RestAssured.given().get("/transacoes?descricao="+desc).then().extract().path("id[0]");
    }
}
