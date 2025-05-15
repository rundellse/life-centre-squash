package org.rundellse.squashleague.api.player;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.rundellse.squashleague.api.player.dto.PlayerDetailsDTO;
import org.rundellse.squashleague.api.player.dto.TablePlayerDTO;
import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.persistence.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping
@CrossOrigin
public class PlayerRestController {

    private static final Logger LOG = LoggerFactory.getLogger(PlayerRestController.class.getName());

    @Autowired
    private PlayerRepository playerRepository;


    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<Object> handleOptionsRequest() {
        return ResponseEntity.ok().build();
    }

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
    public void deletePlayer(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable Long id) {
        LOG.info("Deleting Player with ID: {}", id);
        playerRepository.deleteById(id);
    }

    @GetMapping("/players")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<TablePlayerDTO> retrieveAllPlayers(HttpServletResponse httpServletResponse) {
        LOG.trace("Getting all Players");

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

    private TablePlayerDTO convertPlayerToTablePlayerDTO(Player player) {
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

    private TablePlayerDTO convertPlayerToAnonymousTablePlayerDTO(Player player) {
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
