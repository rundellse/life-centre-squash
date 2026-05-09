package org.rundellse.squashleague.api.player.dto;

import java.util.List;

public record TablePlayerDTO (
    Long id,
    String name,
    String email,
    String phoneNumber,
    String availabilityNotes,
    Integer division,
    boolean isRedFlagged,
    List<String> matchPoints
){}
