package org.rundellse.squashleague.api.table.dto;

public record BulkPlayerUpdateDTO(
        Long id,
        int division,
        boolean redFlag
) {}
