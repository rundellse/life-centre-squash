package org.rundellse.squashleague.model;

import java.util.List;

public record Player (
        Long id,
        String name,
        String email,
        String phoneNumber,
        String availabilityNotes,
        Integer division,
        List<Integer> results
) {
    public Player withID(Long id) {
        return new Player(id, this.name(), this.email(), this.phoneNumber(), this.availabilityNotes(), this.division(), this.results());
    }
}
