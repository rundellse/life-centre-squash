package org.rundellse.squashleague.api.player;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.persistence.PlayerH2DAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping
@CrossOrigin
public class PlayerRestController {

//    private static Logger LOG = LoggerFactory.getLogger(PlayerRestController.class.getName());

    private final PlayerH2DAO playerH2DAO;

    public PlayerRestController(PlayerH2DAO playerH2DAO) {
        this.playerH2DAO = playerH2DAO;
    }


    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<Object> handleOptionsRequest() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/players")
    public Map<Long, Player> newPlayer(@RequestBody Player player) {
        Map<Long, Player> playerResponse = new HashMap<>();
        playerResponse.put(playerH2DAO.persistPlayer(player), player);
        return playerResponse;
    }

    @PostMapping("/players/{id}")
    public Player updatePlayer(@PathVariable Long id, @RequestBody Player player) {
        player.setId(id);
        return playerH2DAO.updatePlayer(player);
    }

    @DeleteMapping("/players/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deletePlayer(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable Long id) {
        playerH2DAO.deletePlayer(id);
    }

    @GetMapping("/players")
    @CrossOrigin
    public List<Player> retrieveAllPlayers(HttpServletResponse httpServletResponse) {
        return playerH2DAO.getAllPlayers();
    }

    @GetMapping("/players/{id}")
    @CrossOrigin
    public Player retrievePlayer(@PathVariable Long id) {
        return playerH2DAO.getPlayer(id);
    }

}
