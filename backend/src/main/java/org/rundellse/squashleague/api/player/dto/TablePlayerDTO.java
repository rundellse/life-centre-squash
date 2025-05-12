package org.rundellse.squashleague.api.player.dto;

public record TablePlayerDTO (
    Long id,
    String name,
    String email,
    String phoneNumber,
    String availabilityNotes,
    Integer division,
    boolean isRedFlagged
){}
