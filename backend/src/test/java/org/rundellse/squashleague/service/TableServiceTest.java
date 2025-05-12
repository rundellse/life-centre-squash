package org.rundellse.squashleague.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.model.Season;
import org.rundellse.squashleague.model.SquashMatch;
import org.rundellse.squashleague.persistence.PlayerRepository;
import org.rundellse.squashleague.persistence.SeasonRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class TableServiceTest {

    public static final String GENERIC_EMAIL = "email@email.com";
    public static final String GENERIC_PHONE_NUMBER = "0777777777";
    public static final String GENERIC_AVAILABILITY_NOTES = "Availability Notes";
    private final Season PREVIOUS_SEASON = new Season(2L, LocalDate.now().minusDays(14), LocalDate.now());

    private TableService tableService;

    private SeasonRepository seasonRepository;

    private PlayerRepository playerRepository;

    @BeforeEach
    public void beforeEach() {
        tableService = new TableService();
        seasonRepository = mock(SeasonRepository.class);
        playerRepository = mock(PlayerRepository.class);
        tableService.setSeasonH2DAO(seasonRepository);
        tableService.setPlayerH2DAO(playerRepository);
    }


    @Test
    public void endCurrentSeasonTest_happyPath() {
        // Probably should have just been an Integration test.
        when(seasonRepository.findSeasonForDate(LocalDate.now())).thenReturn(PREVIOUS_SEASON);
        List<Player> endSeasonPlayerList = populateEndSeasonPlayerList();
        when(playerRepository.findAll()).thenReturn(endSeasonPlayerList);
        when(seasonRepository.save(any(Season.class))).thenReturn(new Season(3L, LocalDate.now(), LocalDate.now().plusDays(7)));

        Map<Integer, List<Player>> result = tableService.endSeasonNewSeason(LocalDate.now().plusDays(28));

        ArgumentCaptor<Player> playerArgumentCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository, times(22)).save(playerArgumentCaptor.capture());
        // A bit of a silly list, but it represents proper working: The order of each division reversed, then the top two moved into division above,
        // bottom two moved into division below, with divisions of 5 as a starting point this results in order looking like (previous division of player shown)
        // { 0, 0, 0, 1, 1, 0, 0, 1, 2, 2, 1, 1, 2, 3, 3, 2, 2 etc... }, essentially one player per mid-table division stays in the same spot.
        List<Long> expectedResultOrder = Arrays.asList(4L, 3L, 2L, 9L, 8L, 1L, 0L, 7L, 14L, 13L, 6L, 5L, 12L, 19L, 18L, 11L, 10L, 17L, 20L, 21L, 16L, 15L);
        List<Player> capturedPlayers = playerArgumentCaptor.getAllValues();
        for (int i = 0; i < capturedPlayers.size(); i++) {
            assertEquals(capturedPlayers.get(i).getId(), expectedResultOrder.get(i), "Persisted players order should match expected result.");
        }
        assertEquals(expectedResultOrder, result.values().stream().flatMap(List::stream).map(Player::getId).collect(Collectors.toList()), "Returned divisions player orders should match expected result.");

        ArgumentCaptor<Season> seasonArgumentCaptor = ArgumentCaptor.forClass(Season.class);
        verify(seasonRepository, times(2)).save(seasonArgumentCaptor.capture());
        Season capturedSeason = seasonArgumentCaptor.getAllValues().get(0);
        assertNull(capturedSeason.getId()); // When passed to save the ID is null, save provides ID.
        assertEquals(LocalDate.now(), capturedSeason.getStartDate());
        assertEquals(LocalDate.now().plusDays(28), capturedSeason.getEndDate());

        capturedSeason = seasonArgumentCaptor.getAllValues().get(1);
        assertEquals(2L, capturedSeason.getId());
        assertEquals(LocalDate.now().minusDays(14), capturedSeason.getStartDate());
        assertEquals(LocalDate.now(), capturedSeason.getEndDate());

    }

    @Test
    public void moveObjectInListTest() {
        Player firstPlayer = new Player(0L, "Player 0", GENERIC_EMAIL, GENERIC_PHONE_NUMBER, GENERIC_AVAILABILITY_NOTES, 0);
        Player demotePlayer1 = new Player(2L, "Player 2", GENERIC_EMAIL, GENERIC_PHONE_NUMBER, GENERIC_AVAILABILITY_NOTES, 0);
        Player demotePlayer2 = new Player(3L, "Player 3", GENERIC_EMAIL, GENERIC_PHONE_NUMBER, GENERIC_AVAILABILITY_NOTES, 0);
        Player promotePlayer1 = new Player(4L, "Player 4", GENERIC_EMAIL, GENERIC_PHONE_NUMBER, GENERIC_AVAILABILITY_NOTES, 1);
        Player unmovedPlayer = new Player(5L, "Player 5", GENERIC_EMAIL, GENERIC_PHONE_NUMBER, GENERIC_AVAILABILITY_NOTES, 1);

        List<Player> testPlayerList = new ArrayList<>(7);
        testPlayerList.add(firstPlayer);
        testPlayerList.add(new Player(1L, "Player 1", GENERIC_EMAIL, GENERIC_PHONE_NUMBER, GENERIC_AVAILABILITY_NOTES, 0));
        testPlayerList.add(demotePlayer1);
        testPlayerList.add(demotePlayer2);
        testPlayerList.add(promotePlayer1);
        testPlayerList.add(unmovedPlayer);
        testPlayerList.add(new Player(6L, "Player 6", GENERIC_EMAIL, GENERIC_PHONE_NUMBER, GENERIC_AVAILABILITY_NOTES, 1));
        testPlayerList.add(new Player(7L, "Player 7", GENERIC_EMAIL, GENERIC_PHONE_NUMBER, GENERIC_AVAILABILITY_NOTES, 1));

        tableService.movePlayerInList(testPlayerList, demotePlayer1, 2);
        tableService.movePlayerInList(testPlayerList, demotePlayer2, 2);
        tableService.movePlayerInList(testPlayerList, promotePlayer1, -2);

        assertEquals(testPlayerList.get(0).getId(), firstPlayer.getId(), "First player should be unmoved. Actual index: " + testPlayerList.indexOf(firstPlayer));
        assertEquals(testPlayerList.get(2).getId(), promotePlayer1.getId(), "Promoted player should be moved to third-place, above demoted player(s). Actual index: " + testPlayerList.indexOf(promotePlayer1));
        assertEquals(testPlayerList.get(3).getId(), demotePlayer1.getId(), "First demoted player should be moved to fourth-place, below promoted player(s). Actual index: " + testPlayerList.indexOf(demotePlayer1));
        assertEquals(testPlayerList.get(4).getId(), demotePlayer2.getId(), "Second demoted player should be moved to fifth-place, below promoted player(s). Actual index: " + testPlayerList.indexOf(demotePlayer2));
        assertEquals(testPlayerList.get(5).getId(), unmovedPlayer.getId(), "Player after promoted/demoted players should be unmoved. Actual index: " + testPlayerList.indexOf(unmovedPlayer));
    }

    @Test
    public void moveObjectInListTest_noIndex_noChange() {
        Player firstPlayer = new Player(0L, "Player 0", GENERIC_EMAIL, GENERIC_PHONE_NUMBER, GENERIC_AVAILABILITY_NOTES, 0);

        List<Player> testPlayerList = new ArrayList<>(3);
        testPlayerList.add(firstPlayer);
        testPlayerList.add(new Player(1L, "Player 1", GENERIC_EMAIL, GENERIC_PHONE_NUMBER, GENERIC_AVAILABILITY_NOTES, 0));
        testPlayerList.add(new Player(6L, "Player 6", GENERIC_EMAIL, GENERIC_PHONE_NUMBER, GENERIC_AVAILABILITY_NOTES, 1));

        List<Player> testPlayerListCopy = new ArrayList<>(testPlayerList);
        tableService.movePlayerInList(testPlayerList, firstPlayer, 0);

        assertEquals(testPlayerListCopy, testPlayerList);
    }

    private long MATCH_ID = 0;

    private List<Player> populateEndSeasonPlayerList() {
        MATCH_ID = 0;
        List<Player> playerListAtEndOfSeason = new ArrayList<>(15);
        for (int i = 0; i < 22; i++) {
            Player player = new Player((long) i, "Player " + i, GENERIC_EMAIL, GENERIC_PHONE_NUMBER, GENERIC_AVAILABILITY_NOTES, i / 5);
            player.setHomeMatches(createSquashMatchListWithScore(i, player));
            playerListAtEndOfSeason.add(player);
        }

        return playerListAtEndOfSeason;
    }

    private List<SquashMatch> createSquashMatchListWithScore(int score, Player player) {
        List<SquashMatch> matches = new ArrayList<>(5);
        int remainingScore = score;
        for (int i = 0; i < 5; i++) {
            int toScore = Math.min(remainingScore, 4);
            remainingScore -= toScore;
            matches.add(new SquashMatch(++MATCH_ID, PREVIOUS_SEASON, player, null, toScore, 1, null, null, null));
        }
        return matches;
    }
}