package com.itm.space.backendresources;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.exception.BackendResourcesException;
import com.itm.space.backendresources.mapper.UserMapper;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import javax.ws.rs.core.Response;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @MockBean
    private Keycloak keycloakClient;

    @MockBean
    UserMapper userMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUserSuccess() {
        UserRequest userRequest = new UserRequest(
                "username",
                "email@am",
                "password",
                "sss",
                "sss"
        );
        RealmResource realmResource = Mockito.mock(RealmResource.class);
        UsersResource usersResource = Mockito.mock(UsersResource.class);
        Response response = Mockito.mock(Response.class);

        when(keycloakClient.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);
        when(response.getLocation()).thenReturn(java.net.URI.create("/users/123"));

        userService.createUser(userRequest);
        verify(usersResource, times(1)).create(any(UserRepresentation.class));
    }

    @Test
    void testCreateUser_BadRequest() {
        // Arrange
        UserRequest userRequest = new UserRequest(
                "username",
                "email@em.com",
                "password",
                "firstName",
                "lastName");
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);

        when(keycloakClient.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenThrow(new RuntimeException("Keycloak error"));

        // Act & Assert
        BackendResourcesException exception = assertThrows(BackendResourcesException.class,
                () -> userService.createUser(userRequest)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("Keycloak error", exception.getMessage());
    }
    @Test
    void testGetUserById_Success() {
        UUID userId = UUID.randomUUID();
        String userIdString = userId.toString();

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(userIdString);
        userRepresentation.setUsername("username");
        userRepresentation.setEmail("email@em.com");
        userRepresentation.setFirstName("firstName");
        userRepresentation.setLastName("lastName");

        RoleRepresentation role = new RoleRepresentation();
        role.setName("ROLE_MODERATOR");

        GroupRepresentation group = new GroupRepresentation();
        group.setName("MODERATORS");

        UserResource userResource = mock(UserResource.class);
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);

        when(keycloakClient.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userIdString)).thenReturn(userResource); // Используем строковое представление userId
        when(userResource.toRepresentation()).thenReturn(userRepresentation);

        RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
        MappingsRepresentation mappingsRepresentation = mock(MappingsRepresentation.class);

        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.getAll()).thenReturn(mappingsRepresentation);
        when(mappingsRepresentation.getRealmMappings()).thenReturn(Collections.singletonList(role));
        when(userResource.groups()).thenReturn(Collections.singletonList(group));

        UserResponse expectedResponse = new UserResponse(
                userId,
                userRepresentation.getFirstName(),
                userRepresentation.getLastName(),
                userRepresentation.getEmail(),
                userRepresentation.getRealmRoles(),
                userRepresentation.getGroups()
        );
        when(userMapper.userRepresentationToUserResponse(
                any(UserRepresentation.class),
                anyList(),
                anyList()
        )).thenReturn(expectedResponse);

        UserResponse actualResponse = userService.getUserById(userId);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getUserId(), actualResponse.getUserId());
        assertEquals(expectedResponse.getFirstName(), actualResponse.getFirstName());
        assertEquals(expectedResponse.getLastName(), actualResponse.getLastName());
        assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());
        assertEquals(expectedResponse.getRoles(), actualResponse.getRoles());
        assertEquals(expectedResponse.getGroups(), actualResponse.getGroups());
    }


}
