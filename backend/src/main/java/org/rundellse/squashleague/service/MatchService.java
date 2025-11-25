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

import java.time.LocalDate;

@Service
public class MatchService {
    private static final Logger LOG = LoggerFactory.getLogger(MatchService.class);

    private final UserService userService;

    private final SquashMatchRepository squashMatchRepository;

    private final SeasonRepository seasonRepository;

    private final PlayerRepository playerRepository;

    @Autowired
    public MatchService(UserService userService, SquashMatchRepository squashMatchRepository, SeasonRepository seasonRepository, PlayerRepository playerRepository) {
        this.userService = userService;
        this.squashMatchRepository = squashMatchRepository;
        this.seasonRepository = seasonRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public void createOrUpdateGameForCurrentSeason(HttpServletRequest request, MatchDTO matchDTO) {
        LOG.debug("Attempting to create or update Match, {}", matchDTO);
        User sessionUser = userService.getSessionUser(request);
        boolean userIsAdmin = sessionUser.getUserRoles().stream().map(UserRole::getRole).toList().contains(Role.ROLE_ADMIN);
        Long userPlayerId = sessionUser.getPlayer().getId();
        long homePlayerId = matchDTO.homePlayerId();
        long awayPlayerId = matchDTO.awayPlayerId();

        if (userPlayerId != homePlayerId &&
                userPlayerId != awayPlayerId &&
                !userIsAdmin) {
            LOG.error("Non-admin User attempted to update game for another player. Potential security issue. User: {}, UserPlayer: {}, Game: {}", sessionUser.getId(), userPlayerId, matchDTO);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Season season = seasonRepository.findSeasonForDate(LocalDate.now());
        if (season == null) {
            LOG.error("No Season found for current date.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Player homePlayer = playerRepository.findById(homePlayerId).orElseThrow();
        Player awayPlayer = playerRepository.findById(awayPlayerId).orElseThrow();
        SquashMatch squashMatch = squashMatchRepository.findSquashMatchBySeasonAndHomePlayerAndAwayPlayer(season, homePlayer, awayPlayer);
        if (squashMatch == null) {
            squashMatch = squashMatchRepository.findSquashMatchBySeasonAndHomePlayerAndAwayPlayer(season, awayPlayer, homePlayer);
        }

        if (squashMatch == null) {
            LOG.debug("No squash match found, creating a new Match. {}", matchDTO);
            squashMatch = new SquashMatch(
                    season,
                    homePlayer.getDivision(),
                    homePlayer,
                    awayPlayer,
                    null,
                    null
            );
            squashMatchRepository.save(squashMatch);
        }

        if (homePlayerId == userPlayerId) {
            squashMatch.setHomePlayerPoints(matchDTO.points());
        } else {
            squashMatch.setAwayPlayerPoints(matchDTO.points());
        }
        LOG.trace("Match updated. Match: {}", squashMatch);
    }
}
