package org.rundellse.squashleague.api.player.dto;

public record PlayerDetailsDTO(
        Long id,
        String name,
        String email,
        String phoneNumber,
        String availabilityNotes,
        Integer division,
        boolean anonymise,
        boolean redFlagged
) {}
