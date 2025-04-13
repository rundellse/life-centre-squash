package org.rundellse.squashleague.api.table;

import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@CrossOrigin
public class TableRestController {

    @Autowired
    private TableService tableService;

    @PostMapping("/table/new-season")
    public Map<Integer, List<Player>> newSeason(LocalDate newSeasonEndDate) {
        return tableService.endSeasonNewSeason(newSeasonEndDate);
    }
}
