package org.rundellse.squashleague.api.user.dto;

public record PasswordUpdateDTO(
        String currentPassword,
        String newPassword
) {
}
