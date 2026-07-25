package org.rundellse.squashleague.service;

import jakarta.transaction.Transactional;
import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.model.Season;
import org.rundellse.squashleague.persistence.PlayerRepository;
import org.rundellse.squashleague.persistence.SeasonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SeasonService {

    private static final Logger LOG = LoggerFactory.getLogger(SeasonService.class.getName());

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private SeasonRepository seasonRepository;


    @Value("${squash.league.base.division.size:5}")
    private int baseDivisionSize;


    public Map<Integer, List<Player>> endSeasonNewSeason(LocalDate newSeasonEndDate) {
        LOG.info("Ending Season for current date");

        LocalDateTime now = LocalDateTime.now();
        Season endingSeason = seasonRepository.findSeasonForDate(now);
        if (endingSeason == null) {
            LOG.info("No Season found for current date, getting latest season");
            endingSeason = seasonRepository.findFirstByOrderByEndDateDesc();

            if (endingSeason == null) {
                LOG.warn("No Season found in DB. Creating new Season, no end-season done");
                createNewSeason(now, newSeasonEndDate);
                return null;
            }
        }

        Map<Integer, List<Player>> newDivisions = createNewDivisions();
        createNewSeason(now, newSeasonEndDate);

        endingSeason.setEndDate(now);
        seasonRepository.save(endingSeason);

        return newDivisions;
    }

    private Map<Integer, List<Player>> createNewDivisions() {
        Map<Integer, List<Player>> currentDivisions = getCurrentDivisions();
        List<Player> orderedPlayersList = doPromotionsRelegationsAdditions(currentDivisions);
        return createAndPopulateNewDivisions(orderedPlayersList);
    }

    public Map<Integer, List<Player>> getCurrentDivisions() {
        Map<Integer, List<Player>> endingSeasonDivisions = new HashMap<>();
        Iterable<Player> originalPlayerList = playerRepository.findAll();

        for (Player player : originalPlayerList) {
            List<Player> division = endingSeasonDivisions.get(player.getDivision());

            if (division == null) {
                ArrayList<Player> players = new ArrayList<>();
                players.add(player);
                endingSeasonDivisions.put(player.getDivision(), players);
            } else {
                division.add(player);
            }
        }

        for (Integer divisionNum : endingSeasonDivisions.keySet()) {
            endingSeasonDivisions.get(divisionNum).sort(Player.PLAYER_POINTS_COMPARATOR);
        }
        return endingSeasonDivisions;
    }

    private List<Player> doPromotionsRelegationsAdditions(Map<Integer, List<Player>> currentDivisions) {
        // TODO addNewPlayersForSeason(orderedPlayersList);
        // TODO Red-flag players with no games.
        return createEndOfSeasonOrderedListOfPlayers(currentDivisions);
    }

    private List<Player> createEndOfSeasonOrderedListOfPlayers(Map<Integer, List<Player>> currentDivisions) {
        LOG.debug("Creating ordered list of all players, applying promotions, relegations, //and additions// for reallocating to divisions");
        int numberOfPlayers = currentDivisions.values().stream().map(List::size).reduce(0, Integer::sum);
        List<Player> orderedPlayersList = new ArrayList<>(numberOfPlayers);

        for (Integer divisionNum : currentDivisions.keySet()) {
            // Establish the results of this Season for each division. Order the
            List<Player> division = currentDivisions.get(divisionNum);
            division.sort(Player.PLAYER_POINTS_COMPARATOR);

            // Maybe division should just be a class. But for now I've committed.
            LOG.trace("Division {} result: {}}", divisionNum, division);

            orderedPlayersList.addAll(division);

            // Cannot promote above the top league, otherwise do promotion. This does the relegations at the same time.
            if (!divisionNum.equals(0)) {
                movePlayerInList(orderedPlayersList, division.get(0), -2);
                movePlayerInList(orderedPlayersList, division.get(1), -2);
            }
        }
        //The null filter should be unnecessary. But we'll keep it just in case.
        return orderedPlayersList.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Map<Integer, List<Player>> createAndPopulateNewDivisions(List<Player> orderedPlayersList) {
        int totalNumberOfPlayerDivisions = orderedPlayersList.size() / baseDivisionSize;

        int[] divisionSizes = calculateDivisionSizes(orderedPlayersList, totalNumberOfPlayerDivisions);

        Map<Integer, List<Player>> newDivisions = new HashMap<>(totalNumberOfPlayerDivisions);
        int runningPlayerTotal = 0;

        for (int i = 0; i < totalNumberOfPlayerDivisions; i++) {
            ArrayList<Player> newDivision = new ArrayList<>();
            newDivisions.put(i, newDivision);

            for (int k = 0; k < divisionSizes[i]; k++) {
                Player player = orderedPlayersList.get(runningPlayerTotal);
                newDivision.add(player);
                player.setDivision(i);
                playerRepository.save(player);
                runningPlayerTotal++;
            }
        }

        return newDivisions;
    }

    private int[] calculateDivisionSizes(List<Player> orderedPlayersList, int totalNumberOfPlayerDivisions) {
        int[] divisionSizes = new int[totalNumberOfPlayerDivisions];
        Arrays.fill(divisionSizes, baseDivisionSize);
        for (int i = 0; i < orderedPlayersList.size() % baseDivisionSize; i++) {
            divisionSizes[i % baseDivisionSize]++;
        }
        return divisionSizes;
    }

    void movePlayerInList(List<Player> orderedPlayerList, Player player, int indexChange) {
        if (indexChange == 0) {
            return;
        }

        int previousPlayerIndex = orderedPlayerList.indexOf(player);
        int destinationIndex = previousPlayerIndex + indexChange;
        if (orderedPlayerList.size() < destinationIndex + 1) {
            LOG.trace("While building ordered Player list, demotion index stretches beyond current list size, expanding. Current list size: {}. Required index: {}", orderedPlayerList.size(), previousPlayerIndex);
            for (int i = 0; i < destinationIndex - orderedPlayerList.size() + 1; i++) {
                orderedPlayerList.add(null);
            }
        } else if (destinationIndex < 0) {
            LOG.warn("Promotion placed a player above the top of the table, setting to top. This implies the top table was very small for some reason");
            destinationIndex = 0;
        }

        orderedPlayerList.add(destinationIndex, player);
        // If object is added above previous index then original object is bumped up one. If added below original is unmoved.
        orderedPlayerList.remove(indexChange > 0 ? previousPlayerIndex : previousPlayerIndex + 1);
    }

    private void createNewSeason(LocalDateTime now, LocalDate newSeasonEndDate) {
        Season newSeason = new Season(now, LocalDateTime.of(newSeasonEndDate, LocalTime.of(23, 59)));
        seasonRepository.save(newSeason);
    }


    public Season getCurrentSeason() {
        Season season = seasonRepository.findSeasonForDate(LocalDateTime.now());

        if (season == null) {
            LOG.error("No Season found for current date. Fetching latest as a backup.");
            season = seasonRepository.findFirstByOrderByEndDateDesc();
        }

        return season;
    }


    void setPlayerH2DAO(PlayerRepository playerH2DAO) {
        this.playerRepository = playerH2DAO;
    }

    void setSeasonH2DAO(SeasonRepository seasonH2DAO) {
        this.seasonRepository = seasonH2DAO;
    }

    void setBaseDivisionSize(int baseDivisionSize) {
        this.baseDivisionSize = baseDivisionSize;
    }
}
