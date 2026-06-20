package org.rundellse.squashleague.api.player.dto;

public record NewPlayerDetailsDTO(
        String name,
        String email,
        String password,
        String phoneNumber,
        String availabilityNotes,
        Integer division,
        boolean anonymise,
        boolean adminUser
) {}
