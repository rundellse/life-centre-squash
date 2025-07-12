package org.rundellse.squashleague.api.game;

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
import org.rundellse.squashleague.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
public class MatchController {
    private static final Logger LOG = LoggerFactory.getLogger(MatchController.class);

    private UserService userService;

    private SquashMatchRepository squashMatchRepository;

    private SeasonRepository seasonRepository;

    private PlayerRepository playerRepository;

    @Autowired
    public MatchController(UserService userService, SquashMatchRepository squashMatchRepository, SeasonRepository seasonRepository) {
        this.userService = userService;
        this.squashMatchRepository = squashMatchRepository;
        this.seasonRepository = seasonRepository;
    }


    @PostMapping("/game")
    @Transactional
    public ResponseEntity<Void> createOrUpdateGameForCurrentSeason(HttpServletRequest request, @RequestBody MatchDTO matchDTO) {
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

        Player homePlayer = playerRepository.getReferenceById(homePlayerId);
        Player awayPlayer = playerRepository.getReferenceById(awayPlayerId);
        SquashMatch squashMatch = squashMatchRepository.findSquashMatchBySeasonAndHomePlayerAndAwayPlayer(season, homePlayer, awayPlayer);
        if (squashMatch == null) {
            LOG.debug("No squash match found, creating a new Match. {}", matchDTO);
            squashMatch = new SquashMatch(
                    season,
                    0/*TODO GET DIVISION*/,
                    homePlayer,
                    awayPlayer,
                    matchDTO.homePlayerPoints(),
                    matchDTO.awayPlayerPoints()
            );
        }

        squashMatch.setHomePlayerPoints(matchDTO.homePlayerPoints());
        squashMatch.setAwayPlayerPoints(matchDTO.awayPlayerPoints());
        LOG.trace("Match updated. Match {}, home points: {}, away points: {}", squashMatch.getId(), matchDTO.homePlayerPoints(), matchDTO.awayPlayerPoints());

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
