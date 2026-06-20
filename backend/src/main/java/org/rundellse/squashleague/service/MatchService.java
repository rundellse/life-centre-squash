package org.rundellse.squashleague.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.rundellse.squashleague.api.game.dto.MatchDTO;
import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.model.Season;
import org.rundellse.squashleague.model.SquashMatch;
import org.rundellse.squashleague.model.user.Role;
import org.rundellse.squashleague.model.user.User;
import org.rundellse.squashleague.model.user.UserRole;
import org.rundellse.squashleague.persistence.PlayerRepository;
import org.rundellse.squashleague.persistence.SeasonRepository;
import org.rundellse.squashleague.persistence.SquashMatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MatchService {
    private static final Logger LOG = LoggerFactory.getLogger(MatchService.class);

    private final UserService userService;

    private final SeasonService seasonService;

    private final SquashMatchRepository squashMatchRepository;

    private final SeasonRepository seasonRepository;

    private final PlayerRepository playerRepository;

    @Autowired
    public MatchService(UserService userService, SeasonService seasonService, SquashMatchRepository squashMatchRepository, SeasonRepository seasonRepository, PlayerRepository playerRepository) {
        this.userService = userService;
        this.seasonService = seasonService;
        this.squashMatchRepository = squashMatchRepository;
        this.seasonRepository = seasonRepository;
        this.playerRepository = playerRepository;
    }

    public void createOrUpdateGameForCurrentSeason(HttpServletRequest request, MatchDTO matchDTO) {
        LOG.debug("Attempting to create or update Match, {}", matchDTO);
        User sessionUser = userService.getSessionUser(request);
        Long userPlayerId = sessionUser.getPlayer().getId();
        long rowPlayerId = matchDTO.rowPlayerId();
        long columnPlayerId = matchDTO.columnPlayerId();

        boolean userIsAdmin = sessionUser.getUserRoles().stream().map(UserRole::getRole).toList().contains(Role.ROLE_ADMIN);
        boolean userPlayerIsPlayerInMatch = (userPlayerId == rowPlayerId) || (userPlayerId == columnPlayerId);
        if (!userPlayerIsPlayerInMatch && !userIsAdmin) {
            LOG.error("Non-admin User attempted to update game for another player. Potential security issue. User: {}, UserPlayer: {}, Game: {}", sessionUser.getId(), userPlayerId, matchDTO);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Season season = seasonService.getCurrentSeason();

        Player rowPlayer = playerRepository.findById(rowPlayerId).orElseThrow();
        Player columnPlayer = playerRepository.findById(columnPlayerId).orElseThrow();
        SquashMatch squashMatch = squashMatchRepository.findSquashMatchBySeasonAndHomePlayerAndAwayPlayer(season, rowPlayer, columnPlayer);
        if (squashMatch == null) {
            squashMatch = squashMatchRepository.findSquashMatchBySeasonAndHomePlayerAndAwayPlayer(season, columnPlayer, rowPlayer);
        }

        if (squashMatch == null) {
            LOG.debug("No squash match found, creating a new Match. {}", matchDTO);
            squashMatch = new SquashMatch(
                    season,
                    rowPlayer.getDivision(),
                    rowPlayer,
                    columnPlayer,
                    null,
                    null
            );
            squashMatchRepository.save(squashMatch);
        }

        if (rowPlayerId == squashMatch.getHomePlayer().getId()) {
            squashMatch.setHomePlayerPoints(matchDTO.points());
        } else {
            squashMatch.setAwayPlayerPoints(matchDTO.points());
        }
        LOG.trace("Match updated. Match: {}", squashMatch);
    }

//    public Map<Long, List<Integer>> getMatchesForDivision(List<Long> playerIds) {
//        // Format: player(id): game1(points), game2(points) ....
//        Map<Long, List<Integer>> pointsGrid = new HashMap<>();
//
//        Season season = getCurrentSeason();
//
//        for (Long playerId : playerIds) {
//            List<Integer> matchPoints = new ArrayList<>();
//
//            List<SquashMatch> matches = squashMatchRepository.findSquashMatchesBySeasonAndHomePlayerAndAwayPlayerIn(season, playerId, playerIds);
//            for (SquashMatch match : matches) {
//                matchPoints.add(match.getHomePlayerPoints());
//            }
//
//            pointsGrid.put(playerId, matchPoints);
//        }
//
//        return pointsGrid;
//    }
}
