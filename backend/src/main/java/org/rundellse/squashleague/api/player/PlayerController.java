package org.rundellse.squashleague.api.player;

import jakarta.servlet.http.HttpServletRequest;
import org.rundellse.squashleague.api.player.dto.PlayerDetailsDTO;
import org.rundellse.squashleague.api.player.dto.TablePlayerDTO;
import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.persistence.PlayerRepository;
import org.rundellse.squashleague.service.PlayerService;
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

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerService playerService;

    @PostMapping("/players")
    @ResponseStatus(HttpStatus.CREATED)
    public void newPlayer(@RequestBody Player player) {
        LOG.debug("Attempting to persist new Player");
        playerRepository.save(player);
        LOG.info("Persisted new Player, ID: {}", player.getId());
    }

    @PostMapping("/players/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Player updatePlayer(@PathVariable Long id, @RequestBody PlayerDetailsDTO playerDetailsDTO) {
        LOG.debug("Updating player with ID: {}", id);

        Optional<Player> playerOptional = playerRepository.findById(id);
        if (playerOptional.isEmpty()) {
            LOG.error("Player with ID: {} not found. Cannot be updated", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Player updatedPlayer = playerOptional.get();
        updatePlayerFromPlayerDetailsDTO(updatedPlayer, playerDetailsDTO);
        playerRepository.save(updatedPlayer);
        return updatedPlayer;
    }

    private void updatePlayerFromPlayerDetailsDTO(Player updatedPlayer, PlayerDetailsDTO playerDetailsDTO) {
        updatedPlayer.setName(playerDetailsDTO.name());
        updatedPlayer.setEmail(playerDetailsDTO.email());
        updatedPlayer.setPhoneNumber(playerDetailsDTO.phoneNumber());
        updatedPlayer.setAvailabilityNotes(playerDetailsDTO.availabilityNotes());
        updatedPlayer.setDivision(playerDetailsDTO.division());
        updatedPlayer.setAnonymised(playerDetailsDTO.anonymise());
        updatedPlayer.setRedFlagged(playerDetailsDTO.redFlagged());
    }

    @DeleteMapping("/players/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deletePlayer(@PathVariable long id) {
        LOG.info("Deleting Player with ID: {}", id);
        playerRepository.deleteById(id);
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
                player.isRedFlagged()
        );
    }
}
