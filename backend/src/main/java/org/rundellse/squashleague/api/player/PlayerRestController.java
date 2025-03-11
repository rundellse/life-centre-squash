package org.rundellse.squashleague.api.player;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.persistence.PlayerH2DAO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping
public class PlayerRestController {

//    private static Logger LOG = LoggerFactory.getLogger(PlayerRestController.class.getName());

    private final PlayerH2DAO playerH2DAO;

    public PlayerRestController(PlayerH2DAO playerH2DAO) {
        this.playerH2DAO = playerH2DAO;
    }


    @PostMapping("/players")
    @CrossOrigin
    public Map<Long, Player> newPlayer(@RequestBody Player player) {
        Map<Long, Player> playerResponse = new HashMap<>();
        playerResponse.put(playerH2DAO.persistPlayer(player), player);
        return playerResponse;
    }

    @PostMapping("/players/{id}")
    @CrossOrigin
    public Player updatePlayer(@PathVariable Long id, @RequestBody Player player) {
        Player playerToUpdate = player.withID(id);
        return playerH2DAO.updatePlayer(playerToUpdate);
    }

    @DeleteMapping("/players/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @CrossOrigin
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
