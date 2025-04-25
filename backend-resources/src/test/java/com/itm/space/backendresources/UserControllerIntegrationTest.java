package com.itm.space.backendresources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.exception.BackendResourcesException;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = {"MODERATOR"})
    public void testCreateUserSuccess() throws Exception {
        UserRequest userRequest = new UserRequest(
                "userName",
                "email@em.com",
                "1234",
                "firstName",
                "lastName"
        );
        mockMvc.perform(
                post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk());
        verify(userService, times(1)).createUser(any(UserRequest.class));
    }

    @Test
    @WithMockUser(roles = {"MODERATOR"})
    public void testCreateUserBadRequest() throws Exception {
        UserRequest userRequest = new UserRequest(
                "",
                "email@em.com",
                "1234",
                "firstName",
                "lastName"
        );
        mockMvc.perform(
                post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"MODERATOR"})
    public void testGetUserByIdSuccess() throws Exception {
        UUID id = UUID.randomUUID();
        String stringId = id.toString();
        List<String> roles = Arrays.asList("MODERATOR, ADMIN");
        List<String> groups = Arrays.asList("MODERATORS");
        UserResponse userResponse = new UserResponse(
                id,
                "fname",
                "lname",
                "email@a",
                roles,
                groups
        );
        when(userService.getUserById(id)).thenReturn(userResponse);
        mockMvc.perform(
                get("/api/users/{id}", id).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(stringId))
                .andExpect(jsonPath("$.firstName").value("fname"))
                .andExpect(jsonPath("$.lastName").value("lname"))
                .andExpect(jsonPath("$.email").value("email@a"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles").isNotEmpty())
                .andExpect(jsonPath("$.groups").isArray());
        verify(userService, times(1)).getUserById(id);
    }

    @Test
    @WithMockUser(roles = {"MODERATOR"})
    public void testGetUserNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getUserById(id)).thenThrow(new BackendResourcesException("Not found", HttpStatus.NOT_FOUND));
        mockMvc.perform(
                get("/api/users/{id}", id).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }
}

