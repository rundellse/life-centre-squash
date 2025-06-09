package org.rundellse.squashleague.api.user.dto;

public record NewUserDTO(
        String name,
        String email,
        String password
){}
