package org.rundellse.squashleague.api.game.dto;

public record MatchDTO(
        long rowPlayerId,
        long columnPlayerId,
        int points
//        int homePlayerPoints,
//        int awayPlayerPoints
) {}
