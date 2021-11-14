package core;

import io.restassured.http.ContentType;

public interface Constantes {

     String URL_BASE = "https://barrigarest.wcaquino.me";
     String BASE_PATH = "";
     int PORT = 443;
     String EMAIL = "guilherme.dourado95@gmail.com";
     String SENHA = "123456";

     ContentType APP_CONTENT_TYPE = ContentType.JSON;
     Long MAX_TIMEOUT = 20000L;
}
