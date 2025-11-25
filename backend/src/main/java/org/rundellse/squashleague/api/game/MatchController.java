package org.rundellse.squashleague.api.game;

import jakarta.servlet.http.HttpServletRequest;
import org.rundellse.squashleague.api.game.dto.MatchDTO;
import org.rundellse.squashleague.service.MatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MatchController {
    private static final Logger LOG = LoggerFactory.getLogger(MatchController.class);

    private final MatchService matchService;

    @Autowired
    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }


    @PostMapping("/match")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void createOrUpdateGameForCurrentSeason(HttpServletRequest request, @RequestBody MatchDTO matchDTO) {
        matchService.createOrUpdateGameForCurrentSeason(request, matchDTO);
    }
}
