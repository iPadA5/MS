package com.itm.space.backendresources.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserResponse {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> roles;
    private List<String> groups;

}
