package org.rundellse.squashleague.service;

import jakarta.servlet.http.HttpServletRequest;
import org.rundellse.squashleague.api.player.dto.TablePlayerDTO;
import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.model.user.Role;
import org.rundellse.squashleague.model.user.Roles;
import org.rundellse.squashleague.model.user.User;
import org.rundellse.squashleague.persistence.PlayerRepository;
import org.rundellse.squashleague.persistence.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    private static final Logger LOG = LoggerFactory.getLogger(PlayerService.class.getName());

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private UserRepository userRepository;

    public Iterable<TablePlayerDTO> retrieveAllPlayers(HttpServletRequest httpServletRequest) {
        String userEmail = httpServletRequest.getRemoteUser();
        User user = userRepository.findUserByEmail(userEmail);
        LOG.trace("Retrieving all players for User: {}", user.getId());
        httpServletRequest.isUserInRole()

        Set<String> roles = user.getUserRoles().stream().map(Role::getName).collect(Collectors.toSet());
        if (roles.contains(Roles.ROLE_ADMIN.toString())) {
            return retrieveAllPlayersNoAnonymisation();
        } else if (roles.contains(Roles.ROLE_USER.toString())) {
            return retrieveAllPlayersWithAnonymisation();
        } else {
            LOG.error("User with id: {} does not have Admin or User role, major error as this request should have already been authorised.", user.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private List<TablePlayerDTO> retrieveAllPlayersNoAnonymisation() {
        List<TablePlayerDTO> allTablePlayers = new ArrayList<>();
        for (Player player : playerRepository.findAll()) {
            allTablePlayers.add(convertPlayerToTablePlayerDTO(player));
        }
        return allTablePlayers;
    }

    private List<TablePlayerDTO> retrieveAllPlayersWithAnonymisation() {
        List<TablePlayerDTO> allTablePlayers = new ArrayList<>();
        for (Player player : playerRepository.findAll()) {
            if (player.isAnonymised()) {
                allTablePlayers.add(convertPlayerToAnonymousTablePlayerDTO(player));
            } else {
                allTablePlayers.add(convertPlayerToTablePlayerDTO(player));
            }
        }
        return allTablePlayers;
    }

    private static TablePlayerDTO convertPlayerToTablePlayerDTO(Player player) {
        return new TablePlayerDTO(
                player.getId(),
                player.getName(),
                player.getEmail(),
                player.getPhoneNumber(),
                player.getAvailabilityNotes(),
                player.getDivision(),
                player.isRedFlagged()
        );
    }

    private static TablePlayerDTO convertPlayerToAnonymousTablePlayerDTO(Player player) {
        return new TablePlayerDTO(
                player.getId(),
                "Anonymous Player",
                "See printed sheet",
                "See printed sheet",
                "",
                player.getDivision(),
                false
        );
    }

}
