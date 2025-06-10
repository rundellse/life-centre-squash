package org.rundellse.squashleague.api.table;

import org.rundellse.squashleague.api.player.dto.BulkPlayerUpdateDTO;
import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.persistence.PlayerRepository;
import org.rundellse.squashleague.service.PdfService;
import org.rundellse.squashleague.service.TableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@RestController
public class TableRestController {

    private static final Logger LOG = LoggerFactory.getLogger(TableRestController.class.getName());

    @Autowired
    private TableService tableService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PdfService pdfService;


    @PostMapping("/table/new-season")
    public Map<Integer, List<Player>> newSeason(@RequestBody LocalDate newSeasonEndDate) {
        return tableService.endSeasonNewSeason(newSeasonEndDate);
    }

    @PostMapping("/table/update-table")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateTable(@RequestBody BulkPlayerUpdateDTO[] updates) {
        LOG.debug("Updating divisions across the tables");
        Set<Player> updatedPlayers = new HashSet<>();

        for (BulkPlayerUpdateDTO update : updates) {
            LOG.trace("Updating player with ID: {}, to Division: {}", update.id(), update.division());
            Optional<Player> playerOptional = playerRepository.findById(update.id());
            if (playerOptional.isEmpty()) {
                LOG.error("Attempting to update Division to {} on Player with ID: {}, but no Player found for ID. Skipping.", update.division(), update.id());
                continue;
            }

            Player player = playerOptional.get();
            player.setDivision(update.division());
            updatedPlayers.add(player);
            playerRepository.saveAll(updatedPlayers);
        }
    }

    @GetMapping("/table/generate-pdf")
    public ResponseEntity<Resource> getCurrentSeasonPdf() throws IOException {
        Resource pdfResource = pdfService.getCurrentSeasonPdfResource();
        LOG.debug("PDF generated. Resource to be returned: {}", pdfResource);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdfResource.getFilename() + "\"")
                .body(pdfResource);
    }
}
