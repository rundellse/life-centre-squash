package org.rundellse.squashleague.api.player.dto;

public record BulkPlayerUpdateDTO(
        Long id,
        int division,
        boolean redFlagged
) {}
