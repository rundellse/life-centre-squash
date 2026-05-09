package org.rundellse.squashleague.service;

import jakarta.servlet.http.HttpServletRequest;
import org.rundellse.squashleague.api.player.dto.DivisionDTO;
import org.rundellse.squashleague.api.player.dto.TablePlayerDTO;
import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.model.Season;
import org.rundellse.squashleague.model.SquashMatch;
import org.rundellse.squashleague.model.user.Role;
import org.rundellse.squashleague.model.user.User;
import org.rundellse.squashleague.persistence.PlayerRepository;
import org.rundellse.squashleague.persistence.SquashMatchRepository;
import org.rundellse.squashleague.persistence.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.springframework.util.ObjectUtils.nullSafeEquals;

@Service
public class PlayerService {

    private static final Logger LOG = LoggerFactory.getLogger(PlayerService.class.getName());

    public static final String NO_USER_ROLE_ERROR_MESSAGE = "User with id: {} does not have Admin or User role, major error as this request should have already been authorised.";

    private final PlayerRepository playerRepository;

    private final UserRepository userRepository;

    private final SquashMatchRepository squashMatchRepository;

    private final MatchService matchService;


    @Autowired
    public PlayerService(PlayerRepository playerRepository, UserRepository userRepository, SquashMatchRepository squashMatchRepository, MatchService matchService) {
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
        this.squashMatchRepository = squashMatchRepository;
        this.matchService = matchService;
    }


    public Iterable<TablePlayerDTO> retrieveAllPlayers(HttpServletRequest httpServletRequest) {
        return getAllPlayers(httpServletRequest);
    }

    public Iterable<DivisionDTO> retrieveAllPlayersInDivisions(HttpServletRequest httpServletRequest) {
        List<TablePlayerDTO> allPlayersDTOs =  getAllPlayers(httpServletRequest);
        Map<Integer, DivisionDTO> divisions = new HashMap<>();

        for (TablePlayerDTO playerDTO : allPlayersDTOs) {
            DivisionDTO divisionDTO = divisions.computeIfAbsent(playerDTO.division(), division -> new DivisionDTO(division, new ArrayList<>()));
            divisionDTO.players().add(playerDTO);
        }

        return divisions.values().stream()
                .sorted(Comparator.comparingInt(DivisionDTO::divisionRank))
                .toList();
    }

    private List<TablePlayerDTO> getAllPlayers(HttpServletRequest httpServletRequest) {
        String userEmail = httpServletRequest.getRemoteUser();
        User user = userRepository.findUserByEmail(userEmail);
        LOG.debug("Retrieving all players for User: {}", user.getId());

        if (httpServletRequest.isUserInRole(Role.ROLE_ADMIN.toString())) {
            return retrieveAllPlayersNoAnonymisation();
        } else if (httpServletRequest.isUserInRole(Role.ROLE_USER.toString())) {
            return retrieveAllPlayersWithAnonymisation();
        } else {
            LOG.error(NO_USER_ROLE_ERROR_MESSAGE, user.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private List<String> getDivisionMatchPointsForPlayer(Player player, List<Player> players) {
        List<String> matchPoints = new ArrayList<>();
        Season currentSeason = matchService.getCurrentSeason();

        for (Player opponent : players) {
            if (nullSafeEquals(player.getId(), opponent.getId())) {
                //Self-game cell, no value.
                matchPoints.add("");

            } else if (nullSafeEquals(player.getDivision(), opponent.getDivision())) {
                // Our player is the 'home' player.
                SquashMatch match = squashMatchRepository.findSquashMatchBySeasonAndHomePlayerAndAwayPlayer(currentSeason, player, opponent);
                if (match != null && match.getHomePlayerPoints() != null) {
                    matchPoints.add(match.getHomePlayerPoints().toString());
                    continue;
                }

                // Our player is the 'away' player.
                if (match == null) {
                    match = squashMatchRepository.findSquashMatchBySeasonAndHomePlayerAndAwayPlayer(currentSeason, opponent, player);

                    if (match != null && match.getAwayPlayerPoints() != null) {
                        matchPoints.add(match.getAwayPlayerPoints().toString());
                        continue;
                    }
                }

                // Same division, but no game, no value
                matchPoints.add("");
            }
        }

        return matchPoints;
    }

    private List<TablePlayerDTO> retrieveAllPlayersNoAnonymisation() {
        List<TablePlayerDTO> allTablePlayers = new ArrayList<>();
        List<Player> players = playerRepository.findAll();

        for (Player player : players) {
            List<String> divisionMatchPoints = getDivisionMatchPointsForPlayer(player, players);
            allTablePlayers.add(convertPlayerToTablePlayerDTO(player, player.isAnonymised(), divisionMatchPoints));
        }
        return allTablePlayers;
    }

    private List<TablePlayerDTO> retrieveAllPlayersWithAnonymisation() {
        List<TablePlayerDTO> allTablePlayers = new ArrayList<>();
        List<Player> players = playerRepository.findAll();

        for (Player player : players) {
            if (player.isAnonymised()) {
                allTablePlayers.add(convertPlayerToAnonymousTablePlayerDTO(player, Collections.nCopies(16, "")));
            } else {
                List<String> divisionMatchPoints = getDivisionMatchPointsForPlayer(player, players);
                allTablePlayers.add(convertPlayerToTablePlayerDTO(player, false, divisionMatchPoints));
            }
        }
        return allTablePlayers;
    }

    private static TablePlayerDTO convertPlayerToTablePlayerDTO(Player player, boolean noteAnonymised, List<String> divisionPoints) {
        return new TablePlayerDTO(
                player.getId(),
                noteAnonymised ? player.getName() + " - ANONYMISED" : player.getName(),
                player.getEmail(),
                player.getPhoneNumber(),
                player.getAvailabilityNotes(),
                player.getDivision(),
                player.isRedFlagged(),
                divisionPoints
        );
    }

    private static TablePlayerDTO convertPlayerToAnonymousTablePlayerDTO(Player player, List<String> divisionPoints) {
        return new TablePlayerDTO(
                player.getId(),
                "Anonymous Player",
                "See printed sheet",
                "See printed sheet",
                "",
                player.getDivision(),
                false,
                divisionPoints
        );
    }
}
