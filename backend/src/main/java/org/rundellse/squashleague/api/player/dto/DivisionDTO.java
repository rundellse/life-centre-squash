package org.rundellse.squashleague.api.player.dto;

import java.util.Collection;

public record DivisionDTO(
        int divisionRank,
        Collection<TablePlayerDTO> players
){}
