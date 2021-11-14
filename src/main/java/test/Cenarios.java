package test;

import core.BaseTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class Cenarios extends BaseTest {

    private int idConta;
    private String TOKEN;

    @Before
    public void login() {
        HashMap<String, String> login = new HashMap<String, String>();
        login.put("email", EMAIL);
        login.put("senha", SENHA);

        TOKEN = given()
                .body(login)
                .when()
                .post("/signin")
                .then()
                .statusCode(200)
                .extract().path("token");
    }

    @Test
    public void naoDeveAcessarSemTOKEN() {
        given()
                .when()
                .get("/contas")
                .then()
                .statusCode(401)
        ;
    }

    //Cenario 2 - Deve incluir uma conta com sucesso
    //post/signin email, senha
    //post/contas
    @Test
    public void deveIncluirConta() {
        idConta = given()
                .header("Authorization", "JWT " + TOKEN)
                .body("{\"nome\" : \"Conta base 2\"}")
                .when()
                .post("/contas")
                .then()
                .log().all()
                .statusCode(201)
                .body("nome", is("Conta base 2"))
                .extract().path("id")
        ;
    }

    //Cenario 3 - Deve alterar uma conta com sucesso
    //PUT/contas/:id
    @Test
    public void deveAlterarConta() {
        idConta = given()
                .header("Authorization", "JWT " + TOKEN)
                .body("{\"nome\" : \"Conta base 2\"}")
                .when()
                .post("/contas")
                .then()
                .log().all()
                .statusCode(201)
                .body("nome", is("Conta base 2"))
                .extract().path("id")
        ;

        given()
                .header("Authorization", "JWT " + TOKEN)
                .body("{\"nome\" : \"Conta automação\"}")
                .when()
                .put("/contas/" + idConta)
                .then()
                .log().all()
                .statusCode(200)
                .body("nome", is("Conta automação"))
        ;
    }


    //Cenario 4 - Não deve incluir conta com nome repetido
    //POST/contas/
    @Test
    public void naoDeveIncluirContaNomeRepetido() {
        given()
                .header("Authorization", "JWT " + TOKEN)
                .body("{\"nome\" : \"Conta base 2\"}")
                .when()
                .post("/contas")
                .then()
                .log().all()
                .statusCode(400)
                .body("error", is("Já existe uma conta com esse nome!"))
        ;
    }

    //Cenario 5 - Deve inserir movimentação com sucesso
    //POST/transações
    //conta_id, descricao(descrição basica da transacao), envolvido(Nome da pessoa),
    // tipo(DESP / REC), data_transacao(dd/MM/YYYY), data_pagamento(dd/MM/YYYY),
    // valor(0.00f), status(true/false)
    @Test
    public void deveInserirMovimentacaoComSucesso() {
        Movimentacao mov = new Movimentacao();

        mov.setDescricao("Movimentação de dinheiro de bitcoin");
        mov.setEnvolvido("Mané Pelé");
        mov.setTipo("REC");
        mov.setData_transacao("05/11/2021");
        mov.setData_pagamento("04/11/2021");
        mov.setValor(2500f);
        mov.setStatus(true);

        String nomeConta = "Vermeinha";

        idConta = given()
                .header("Authorization", "JWT " + TOKEN)
                .body("{\"nome\" : \"" + nomeConta + "\"}")
                .when()
                .post("/contas")
                .then()
                .log().all()
                .statusCode(201)
                .body("nome", is(nomeConta))
                .extract().path("id")
        ;

        mov.setConta_id(idConta);

        given()
                .header("Authorization", "JWT " + TOKEN)
                .body(mov)
                .when()
                .post("/transacoes")
                .then()
                .statusCode(201)
                .body("envolvido", is("Mané Pelé"))
                .body("valor", equalTo("2500.00"))
                .body("descricao", is("Movimentação de dinheiro de bitcoin"))
        ;
    }

    //Cenario 6 - Deve validar campos obrigatorios na movimentação
    //POST/transações
    @Test
    public void deveValidarCamposObrigatoriosMovimentacao() {
        given()
                .header("Authorization", "JWT " + TOKEN)
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

    //Cenario 7 - Não deve cadastrar movimentação futura
    //POST/transações data futura a data atual
    @Test
    public void naoDeveCadastrarMovimentacaoFutura() {
        Movimentacao mov = getMovimentacaoValida();
        mov.setData_transacao("11/11/2021");

        String nomeConta = "Verdin";

        idConta = given()
                .header("Authorization", "JWT " + TOKEN)
                .body("{\"nome\" : \"" + nomeConta + "\"}")
                .when()
                .post("/contas")
                .then()
                .log().all()
                .statusCode(201)
                .body("nome", is(nomeConta))
                .extract().path("id")
        ;

        mov.setConta_id(idConta);

        given()
                .header("Authorization", "JWT " + TOKEN)
                .body(mov)
                .when()
                .post("/transacoes")
                .then()
                .statusCode(400)
                .body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"))
                .body("$", hasSize(1))
        ;
    }

    //Cenario 8 - Não deve remover conta com movimentação
    //DELETE/contas/:id
    @Test
    public void naoDeveRemoverContaMovimentacao() {
        String nomeConta = "Roxin";

        idConta = given()
                .header("Authorization", "JWT " + TOKEN)
                .body("{\"nome\" : \"" + nomeConta + "\"}")
                .when()
                .post("/contas")
                .then()
                .log().all()
                .statusCode(201)
                .body("nome", is(nomeConta))
                .extract().path("id")
        ;

        given()
                .header("Authorization", "JWT " + TOKEN)
                .body("{\n" +
                        "    \"conta_id\" : \"" + idConta + "\",\n" +
                        "    \"descricao\" : \"Movimentação de dinheiro de bitcoin\",\n" +
                        "    \"envolvido\" : \"Mané Pelé\",\n" +
                        "    \"tipo\" : \"REC\",\n" +
                        "    \"data_transacao\" : \"05/11/2021\",\n" +
                        "    \"data_pagamento\" : \"04/11/2021\",\n" +
                        "    \"valor\" : 10025.75,\n" +
                        "    \"status\" : false\n" +
                        "}")
                .when()
                .post("/transacoes")
                .then()
                .statusCode(201)
                .body("envolvido", is("Mané Pelé"))
                .body("valor", equalTo("10025.75"))
                .body("descricao", is("Movimentação de dinheiro de bitcoin"))
        ;

        given()
                .header("Authorization", "JWT " + TOKEN)
                .when()
                .delete("/contas/" + idConta)
                .then()
                .statusCode(500)
                .body("detail", hasItem("Key (id)=(" + idConta + ") is still referenced from table \"transacoes\"."))
        ;

    }

    //Cenario 9 - Deve calcular saldo contas
    //GET/saldo
    @Test
    public void deveCalcularSaldoContas() {
        String nomeConta = "Marromzinho";

        idConta = given()
                .header("Authorization", "JWT " + TOKEN)
                .body("{\"nome\" : \"" + nomeConta + "\"}")
                .when()
                .post("/contas")
                .then()
                .log().all()
                .statusCode(201)
                .body("nome", is(nomeConta))
                .extract().path("id")
        ;

        given()
                .header("Authorization", "JWT " + TOKEN)
                .body("{\n" +
                        "    \"conta_id\" : \"" + idConta + "\",\n" +
                        "    \"descricao\" : \"Movimentação do dinheiro dos cursos da hotmart\",\n" +
                        "    \"envolvido\" : \"Sané Mané Pelé\",\n" +
                        "    \"tipo\" : \"REC\",\n" +
                        "    \"data_transacao\" : \"05/11/2021\",\n" +
                        "    \"data_pagamento\" : \"04/11/2021\",\n" +
                        "    \"valor\" : 5000.00,\n" +
                        "    \"status\" : true\n" +
                        "}")
                .when()
                .post("/transacoes")
                .then()
                .statusCode(201)
                .body("envolvido", is("Sané Mané Pelé"))
                .body("valor", equalTo("5000.00"))
                .body("descricao", is("Movimentação do dinheiro dos cursos da hotmart"))
        ;

        given()
                .header("Authorization", "JWT " + TOKEN)
                .body("{\n" +
                        "    \"conta_id\" : \"" + idConta + "\",\n" +
                        "    \"descricao\" : \"Reforma do banheiro\",\n" +
                        "    \"envolvido\" : \"MSN\",\n" +
                        "    \"tipo\" : \"DESP\",\n" +
                        "    \"data_transacao\" : \"05/11/2021\",\n" +
                        "    \"data_pagamento\" : \"05/11/2021\",\n" +
                        "    \"valor\" : 2000.00,\n" +
                        "    \"status\" : true\n" +
                        "}")
                .when()
                .post("/transacoes")
                .then()
                .statusCode(201)
                .body("envolvido", is("MSN"))
                .body("valor", equalTo("-2000.00"))
                .body("descricao", is("Reforma do banheiro"))
        ;

        given()
                .header("Authorization", "JWT " + TOKEN)
                .when()
                .get("/saldo/" + idConta)
                .then()
                .log().all()
                .statusCode(200)
                .body("conta_id", is(idConta))
                .body("conta", is(nomeConta))
                .body("saldo", is("3000.00"))
        ;
    }

    //Cenario 10 - Deve remover movimentação
    //DELETE/transações/:id
    @Test
    public void deveRemoverTransacao() {
        String nomeConta = "Amarelin";

        idConta = given()
                .header("Authorization", "JWT " + TOKEN)
                .body("{\"nome\" : \"" + nomeConta + "\"}")
                .when()
                .post("/contas")
                .then()
                .log().all()
                .statusCode(201)
                .body("nome", is(nomeConta))
                .extract().path("id")
        ;

        int transferencia_id = given()
                .header("Authorization", "JWT " + TOKEN)
                .body("{\n" +
                        "    \"conta_id\" : \"" + idConta + "\",\n" +
                        "    \"descricao\" : \"Freela do casamento da Marcinha\",\n" +
                        "    \"envolvido\" : \"Sané Mané Pelé\",\n" +
                        "    \"tipo\" : \"REC\",\n" +
                        "    \"data_transacao\" : \"05/11/2021\",\n" +
                        "    \"data_pagamento\" : \"04/11/2021\",\n" +
                        "    \"valor\" : 5000.00,\n" +
                        "    \"status\" : true\n" +
                        "}")
                .when()
                .post("/transacoes")
                .then()
                .statusCode(201)
                .body("envolvido", is("Sané Mané Pelé"))
                .body("valor", equalTo("5000.00"))
                .body("descricao", is("Freela do casamento da Marcinha"))
                .extract().path("id");

        given()
                .header("Authorization", "JWT " + TOKEN)
                .when()
                .delete("/transacoes/" + transferencia_id)
                .then()
                .statusCode(204)
        ;

        given()
                .header("Authorization", "JWT " + TOKEN)
                .when()
                .get("/transacoes/" + transferencia_id)
                .then()
                .log().all()
                .statusCode(404)
                .body("error", is("Transacao com id " + transferencia_id + " não encontrada"))
        ;
    }

    public Movimentacao getMovimentacaoValida(){
        Movimentacao mov = new Movimentacao();

        mov.setDescricao("Movimentação de dinheiro de bitcoin");
        mov.setEnvolvido("Mané Pelé");
        mov.setTipo("REC");
        mov.setData_transacao("05/11/2021");
        mov.setData_pagamento("04/11/2021");
        mov.setValor(2500f);
        mov.setStatus(true);
        return mov;
    }
}


