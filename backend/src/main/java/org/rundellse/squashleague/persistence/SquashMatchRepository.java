package org.rundellse.squashleague.persistence;

import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.model.Season;
import org.rundellse.squashleague.model.SquashMatch;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SquashMatchRepository extends CrudRepository<SquashMatch, Long> {
    SquashMatch findSquashMatchBySeasonAndHomePlayerAndAwayPlayer(Season season, Player homePlayer, Player awayPlayer);

    List<SquashMatch> findSquashMatchesBySeasonAndHomePlayerAndAwayPlayerIn(Season season, Long homePlayerId, List<Long> awayPlayerIds);
}
