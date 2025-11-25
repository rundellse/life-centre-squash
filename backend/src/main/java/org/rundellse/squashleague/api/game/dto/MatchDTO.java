package org.rundellse.squashleague.api.game.dto;

public record MatchDTO(
        long homePlayerId,
        long awayPlayerId,
        int points
//        int homePlayerPoints,
//        int awayPlayerPoints
) {}
