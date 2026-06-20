package org.rundellse.squashleague.api.user.dto;

public record PasswordUpdateAdminDTO(
        long playerId,
        String newPassword
) {}
