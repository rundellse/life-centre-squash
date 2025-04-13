package org.rundellse.squashleague.model;

import java.util.List;

public record SquashMatch(
        Long id,
        Season season,
        Player homePlayer,
        Player awayPlayer,
        Integer homePlayerPoints,
        Integer awayPlayerPoints,
        Integer homePlayerMatchScore,
        Integer awayPlayerMatchScore,
        List<SquashGame> games
) {
    public SquashMatch withId(Long id) {
        return new SquashMatch(id, this.season, this.homePlayer, this.awayPlayer, this.homePlayerPoints, this.awayPlayerPoints, this.homePlayerMatchScore, this.awayPlayerMatchScore, this.games);
    }
}
