package org.rundellse.squashleague.persistence;

import org.rundellse.squashleague.api.player.PlayerNotFoundException;
import org.rundellse.squashleague.api.player.dto.DivisionUpdateDTO;
import org.rundellse.squashleague.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PlayerH2DAO {

    private static final Logger LOG = LoggerFactory.getLogger(PlayerH2DAO.class.getName());

    @Autowired
    private H2DatabaseConnection h2DatabaseConnection;

    private final static String createPlayerTable = """
            CREATE TABLE IF NOT EXISTS
            PLAYER (ID BIGINT AUTO_INCREMENT PRIMARY KEY, NAME VARCHAR(255) NOT NULL, EMAIL VARCHAR(255), PHONE_NUMBER VARCHAR(255), DIVISION INT, AVAILABILITY_NOTES VARCHAR(MAX), RESULTS VARCHAR(MAX));
            """;

    private final static String selectAllPlayers = """
            SELECT ID, NAME, EMAIL, PHONE_NUMBER, AVAILABILITY_NOTES, DIVISION, RESULTS
            FROM PLAYER;
            """;

    private final static String selectPlayer = """
            SELECT ID, NAME, EMAIL, PHONE_NUMBER, AVAILABILITY_NOTES, DIVISION, RESULTS
            FROM PLAYER
            WHERE ID = ?;
            """;

    private final static String insertPlayer = """
            INSERT INTO PLAYER (NAME, EMAIL, PHONE_NUMBER, DIVISION, AVAILABILITY_NOTES)
            VALUES (?,?,?,?,?);
            """;

    private final static String updatePlayer = """
            UPDATE PLAYER
            SET NAME = ?, EMAIL = ?, PHONE_NUMBER = ?, DIVISION = ?, AVAILABILITY_NOTES = ?
            WHERE ID = ?;
            """;

    private final static String updatePlayerDivision = """
            UPDATE PLAYER
            SET DIVISION = ?
            WHERE ID = ?;
            """;

    private final static String deletePlayer = """
            DELETE FROM PLAYER
            WHERE ID = ?;
            """;

    public void createTable() {
        try (Connection connection = h2DatabaseConnection.getH2Connection();
             Statement statement = connection.createStatement();
        ) {
            statement.execute(createPlayerTable);
            LOG.info("Player table created successfully.");
        } catch (SQLException e) {
            H2DatabaseConnection.logSQLException(e);
        }
    }

    public List<Player> getAllPlayers() {
        LOG.debug("Getting all players from database");

        List<Player> resultPlayerList;
        try (Connection connection = h2DatabaseConnection.getH2Connection();
             Statement statement = connection.createStatement();
        ) {
            ResultSet resultSet = statement.executeQuery(selectAllPlayers);
            resultPlayerList = convertResultSetToPlayerList(resultSet);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error while fetching Player list", e);
        }

        return resultPlayerList;
    }

    public Player getPlayer(Long id) {
        LOG.debug("Getting player from database");

        Player resultPlayer;
        try (Connection connection = h2DatabaseConnection.getH2Connection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectPlayer);
        ) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.first()) {
                throw new PlayerNotFoundException(id);
            }

            resultPlayer = populatePlayerFromResult(resultSet);
        } catch (SQLException e) {
            LOG.error("Error while fetching Player with ID: " + id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found", e);
        }

        return resultPlayer;
    }

    private List<Player> convertResultSetToPlayerList(ResultSet resultSet) {
        List<Player> playerList = new ArrayList<>();
        try {
            while (resultSet.next()) {
                Player player = populatePlayerFromResult(resultSet);
                playerList.add(player);
            }
        } catch (SQLException e) {
            LOG.error("SQL Exception while converting Result of Players fetch from database");
            H2DatabaseConnection.logSQLException(e);
        }

        return playerList;
    }

    private Player populatePlayerFromResult(ResultSet resultSet) throws SQLException {
        return new Player(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("phone_number"),
                resultSet.getString("availability_notes"),
                resultSet.getInt("division"),
                null //TODO results
        );
    }

    /**
     * Persists player to h2 db.
     *
     * @param player - Player - To persist
     * @return - Long - Generated id from the db
     */
    public long persistPlayer(Player player) {
        LOG.info("Saving a new player to db");

        long newId;
        try (Connection connection = h2DatabaseConnection.getH2Connection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertPlayer, Statement.RETURN_GENERATED_KEYS);
        ) {
            preparedStatement.setString(1, player.getName());
            preparedStatement.setString(2, player.getEmail());
            preparedStatement.setString(3, player.getPhoneNumber());
            preparedStatement.setInt(4, player.getDivision());
            preparedStatement.setString(5, player.getAvailabilityNotes());

            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                newId = generatedKeys.getLong("id");
            } else {
                throw new SQLDataException("Not able to get ID for saved Player");
            }
        } catch (SQLException e) {
            LOG.error("SQL Exception while attempting to save Player to database with ID: " + player.getId());
            H2DatabaseConnection.logSQLException(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while persisting Player", e);
        }

        return newId;
    }

    public Player updatePlayer(Player player) {
        LOG.info("Updating player with ID: {}, division: {}", player.getId(), player.getDivision());

        try (Connection connection = h2DatabaseConnection.getH2Connection();
             PreparedStatement preparedStatement = connection.prepareStatement(updatePlayer)) {

            preparedStatement.setString(1, player.getName());
            preparedStatement.setString(2, player.getEmail());
            preparedStatement.setString(3, player.getPhoneNumber());
            preparedStatement.setInt(4, player.getDivision());
            preparedStatement.setString(5, player.getAvailabilityNotes());
            preparedStatement.setLong(6, player.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOG.error("SQL Exception while attempting to save Player to database with ID: " + player.getId());
            H2DatabaseConnection.logSQLException(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while updating Player", e);
        }

        return player;
    }

    public void updatePlayerDivision(DivisionUpdateDTO divisionUpdate) {
        LOG.debug("Updating player division with ID: {}, to division: {}", divisionUpdate.id(), divisionUpdate.division());

        try (Connection connection = h2DatabaseConnection.getH2Connection();
             PreparedStatement preparedStatement = connection.prepareStatement(updatePlayerDivision)) {

            preparedStatement.setInt(1, divisionUpdate.division());
            preparedStatement.setLong(2, divisionUpdate.id());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOG.error("SQL Exception while updating player division with ID: {}, to division: {}", divisionUpdate.id(), divisionUpdate.division());
            H2DatabaseConnection.logSQLException(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while updating Player division", e);
        }
    }

    public void deletePlayer(Long id) {
        LOG.info("Deleting player with ID: " + id);
        try (Connection connection = h2DatabaseConnection.getH2Connection();
             PreparedStatement preparedStatement = connection.prepareStatement(deletePlayer);
        ) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOG.error("SQL Exception while attempting to delete Player from database with ID: " + id);
            H2DatabaseConnection.logSQLException(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while deleting Player", e);
        }
    }

}
