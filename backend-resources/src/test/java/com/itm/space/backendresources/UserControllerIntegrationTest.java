package com.itm.space.backendresources;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.provider.KeyCloakTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIntegrationTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void testCreateUser() {
        String jwtToken = KeyCloakTokenProvider.getAccessToken();
        UserRequest userRequest = new UserRequest(
                "userName",
                "email@em.com",
                "1234",
                "firstName",
                "lastName"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                "http://backend-gateway-client:9191/api/users", HttpMethod.POST,
                new HttpEntity<>(userRequest, headers), Void.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testCreateExistingUser() {
        String jwtToken = KeyCloakTokenProvider.getAccessToken();
        UserRequest userRequest = new UserRequest(
                "userName",
                "email@em.com",
                "1234",
                "firstName",
                "lastName"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> restTemplate.exchange(
        "http://backend-gateway-client:9191/api/users",
        HttpMethod.POST, new HttpEntity<>(userRequest, headers), Void.class));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    public void testGetUserById(){
        String jwtToken = KeyCloakTokenProvider.getAccessToken();
        String UUID = "9faf67e1-569e-4479-843e-272529d7c53b";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                ("http://backend-gateway-client:9191/api/users/" + UUID),
                HttpMethod.GET, new HttpEntity<>(headers), Void.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetUserByIdNotFound(){
        String jwtToken = KeyCloakTokenProvider.getAccessToken();
        String uuId = UUID.randomUUID().toString();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpServerErrorException exception = assertThrows(HttpServerErrorException.class, () -> restTemplate.exchange("http://backend-gateway-client:9191/api/users/" + uuId,
                HttpMethod.GET, new HttpEntity<>(headers), Void.class));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    }

    @Test
    public void testHello() {
        String jwtToken = KeyCloakTokenProvider.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "http://backend-gateway-client:9191/api/users/hello",
                HttpMethod.GET, new HttpEntity<>(headers), String.class
        );
        assertNotNull(responseEntity.getBody());
        assertEquals("9faf67e1-569e-4479-843e-272529d7c53b", responseEntity.getBody());
    }
}
