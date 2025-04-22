package com.itm.space.backendresources.provider;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class KeyCloakTokenProvider {

    private static final String TOKEN_URL = "http://backend-keycloak-auth:8080/auth/realms/ITM/protocol/openid-connect/token";
    private static final String CLIENT_ID = "backend-gateway-client"; // Укажите ваш client ID
    private static final String CLIENT_SECRET = "j9AFHneKjXnlf7MZ51pEYXnIo1IbFENh"; // Укажите ваш client secret
    private static final String USERNAME = "jorji"; // Укажите имя пользователя
    private static final String PASSWORD = "1234"; // Укажите пароль

    public static String getAccessToken() {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", CLIENT_ID);
        body.add("client_secret", CLIENT_SECRET);
        body.add("username", USERNAME);
        body.add("password", PASSWORD);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(TOKEN_URL, request, Map.class);
        return (String) response.getBody().get("access_token");
    }
}
