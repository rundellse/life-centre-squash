package org.rundellse.squashleague.api.player;

import jakarta.servlet.http.HttpServletRequest;
import org.rundellse.squashleague.api.player.dto.DivisionDTO;
import org.rundellse.squashleague.api.player.dto.NewPlayerDetailsDTO;
import org.rundellse.squashleague.api.player.dto.PlayerDetailsDTO;
import org.rundellse.squashleague.api.player.dto.TablePlayerDTO;
import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.model.user.User;
import org.rundellse.squashleague.persistence.PlayerRepository;
import org.rundellse.squashleague.persistence.UserRepository;
import org.rundellse.squashleague.service.PlayerService;
import org.rundellse.squashleague.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;


@RestController
@RequestMapping
@CrossOrigin
public class PlayerController {

    private static final Logger LOG = LoggerFactory.getLogger(PlayerController.class.getName());


    private final PlayerRepository playerRepository;

    private final PlayerService playerService;

    private final UserRepository userRepository;

    private final UserService userService;


    @Autowired
    public PlayerController(PlayerRepository playerRepository, PlayerService playerService, UserRepository userRepository, UserService userService) {
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
        this.playerService = playerService;
        this.userService = userService;
    }


    @PostMapping("/players")
    @ResponseStatus(HttpStatus.CREATED)
    public void newPlayer(@RequestBody NewPlayerDetailsDTO newPlayerDetailsDTO) {
        PlayerService.validateNewPlayerDetails(newPlayerDetailsDTO);

        LOG.trace("Creating Player and User object for persistence");
        Player newPlayer = PlayerService.createPlayerFromNewPlayerDetailsDTO(newPlayerDetailsDTO);
        User newUser = userService.createUserFromNewPlayerDetailsDTO(newPlayerDetailsDTO, newPlayer);

        LOG.debug("Attempting to persist new Player and User");
        playerRepository.save(newPlayer);
        userRepository.save(newUser);
        newPlayer.setUser(newUser);
        userRepository.save(newUser);
        LOG.info("Persisted new Player, ID: {}. Persisted new User, ID: {}", newPlayer.getId(), newUser.getId());
    }

    @PostMapping("/players/{playerId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Player updatePlayer(@PathVariable Long playerId, @RequestBody PlayerDetailsDTO playerDetailsDTO) {
        LOG.debug("Updating player with ID: {}", playerId);

        Optional<Player> playerOptional = playerRepository.findById(playerId);
        if (playerOptional.isEmpty()) {
            LOG.error("Player with ID: {} not found. Cannot be updated", playerId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Player updatedPlayer = playerOptional.get();
        PlayerService.updatePlayerFromPlayerDetailsDTO(updatedPlayer, playerDetailsDTO);
        playerRepository.save(updatedPlayer);

        if (updatedPlayer.getUser() != null) {
            userService.updateUserFromPlayerDetailsDTO(updatedPlayer.getUser(), playerDetailsDTO);
        }

        return updatedPlayer;
    }

    @DeleteMapping("/players/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deletePlayer(@PathVariable long id) {
        LOG.info("Deleting Player and User for playerId: {}", id);

        Optional<Player> playerOptional = playerRepository.findById(id);
        if (playerOptional.isEmpty()) {
            String message = "Cannot find Player for ID: " + id;
            LOG.error(message);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        Player player = playerOptional.get();
        User user = player.getUser();

        player.setUser(null);

        if (user != null) {
            user.setPlayer(null);
            userRepository.delete(user);
        }

        playerRepository.delete(player);
        LOG.debug("Player and User deleted for playerId: {}", id);
    }

    @GetMapping("/players")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<TablePlayerDTO> retrieveAllPlayers(HttpServletRequest httpServletRequest) {
        LOG.trace("Getting all Players");
        return playerService.retrieveAllPlayers(httpServletRequest);
    }

    @GetMapping("/players/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PlayerDetailsDTO retrievePlayer(@PathVariable Long id) {
        LOG.trace("Getting player details with ID: {}", id);
        Optional<Player> player = playerRepository.findById(id);
        if (player.isEmpty()) {
            LOG.error("Attempted to fetch Player with ID: {}, but no Player found in repository.", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return convertPlayerToPlayerDetailsDTO(player.get());
    }

    private PlayerDetailsDTO convertPlayerToPlayerDetailsDTO(Player player) {
        return new PlayerDetailsDTO(
                player.getId(),
                player.getName(),
                player.getEmail(),
                player.getPhoneNumber(),
                player.getAvailabilityNotes(),
                player.getDivision(),
                player.isAnonymised(),
                player.isRedFlagged(),
                userService.isUserAdmin(player)
        );
    }

    @GetMapping("/players/divisions")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<DivisionDTO> retrievePlayersInDivisions(HttpServletRequest httpServletRequest) {
        LOG.trace("Getting all players in Divisions");
        return playerService.retrieveAllPlayersInDivisions(httpServletRequest);
    }
}
