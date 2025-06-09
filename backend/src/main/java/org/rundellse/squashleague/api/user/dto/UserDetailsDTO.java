package org.rundellse.squashleague.api.user.dto;

public record UserDetailsDTO(
        long id,
        String name,
        String email,
        String phoneNumber,
        String availabilityNotes,
        boolean anonymise
) {}
