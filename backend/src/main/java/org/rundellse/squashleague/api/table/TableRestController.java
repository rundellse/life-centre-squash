package org.rundellse.squashleague.api.table;

import org.rundellse.squashleague.api.player.dto.DivisionUpdateDTO;
import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.persistence.PlayerH2DAO;
import org.rundellse.squashleague.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@CrossOrigin
public class TableRestController {

    @Autowired
    private TableService tableService;

    @Autowired
    private PlayerH2DAO playerH2DAO;

    @PostMapping("/table/new-season")
    public Map<Integer, List<Player>> newSeason(@RequestBody LocalDate newSeasonEndDate) {
        return tableService.endSeasonNewSeason(newSeasonEndDate);
    }

    @PostMapping("/table/update-table")
    public ResponseEntity<Object> updateTable(@RequestBody DivisionUpdateDTO[] updates) {
        // This is very slow, pretty much the slowest way to do this, opening a new db connection each time,
        // but it's also the simplest and we don't mind waiting a couple of seconds for now.
        for (DivisionUpdateDTO update : updates) {
            playerH2DAO.updatePlayerDivision(update);
        }
        return ResponseEntity.ok().build();
    }
}
