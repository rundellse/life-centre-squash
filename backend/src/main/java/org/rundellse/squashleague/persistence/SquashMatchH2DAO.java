package org.rundellse.squashleague.persistence;

import org.rundellse.squashleague.model.SquashMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.sql.*;

public class SquashMatchH2DAO {

    private static final Logger LOG = LoggerFactory.getLogger(SquashMatchH2DAO.class.getName());

    @Autowired
    private H2DatabaseConnection h2DatabaseConnection;

    private final static String createSquashMatchTable = """
            CREATE TABLE IF NOT EXISTS
            SQUASH_MATCH (ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                SEASON BIGINT, HOME_PLAYER BIGINT, AWAY_PLAYER BIGINT,
                HOME_PLAYER_POINTS INT, AWAY_PLAYER_POINTS INT, HOME_PLAYER_MATCH_SCORE INT, AWAY_PLAYER_MATCH_SCORE INT,
                FOREIGN KEY (SEASON) REFERENCES SEASON(ID), FOREIGN KEY (HOME_PLAYER) REFERENCES PLAYER(ID), FOREIGN KEY (AWAY_PLAYER) REFERENCES PLAYER(ID));
            """;

    private final static String insertSquashMatch = """
            INSERT INTO SQUASH_MATCH (SEASON, HOME_PLAYER, AWAY_PLAYER, HOME_PLAYER_POINTS, AWAY_PLAYER_POINTS, HOME_PLAYER_SCORE, AWAY_PLAYER_SCORE)
            VALUES (?,?,?,?,?,?,?);
            """;

    public void createTable() {
        try (Connection connection = h2DatabaseConnection.getH2Connection();
             Statement statement = connection.createStatement();
        ) {
            statement.execute(createSquashMatchTable);
            LOG.info("Matches table created successfully.");
        } catch (SQLException e) {
            H2DatabaseConnection.logSQLException(e);
        }
    }

    public long persistSquashMatch(SquashMatch squashMatch) {
        LOG.info("Saving a new Season to db");

        long newId;
        try (Connection connection = h2DatabaseConnection.getH2Connection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertSquashMatch, Statement.RETURN_GENERATED_KEYS);
        ) {
//            preparedStatement.setLong(1, squashMatch.season().getId());
//            preparedStatement.setLong(2, squashMatch.homePlayer().getId());
//            preparedStatement.setLong(3, squashMatch.awayPlayer().getId());
//            preparedStatement.setInt(4, squashMatch.homePlayerPoints());
//            preparedStatement.setInt(5, squashMatch.awayPlayerPoints());
//            preparedStatement.setInt(6, squashMatch.homePlayerMatchScore());
//            preparedStatement.setInt(7, squashMatch.awayPlayerMatchScore());

            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                newId = generatedKeys.getLong("id");
            } else {
                throw new SQLDataException("Not able to get ID for saved SquashMatch");
            }
        } catch (SQLException e) {
//            LOG.error("SQL Exception while attempting to save SquashMatch to database with ID: {}", squashMatch.id());
            H2DatabaseConnection.logSQLException(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while persisting SquashMatch", e);
        }

        return newId;
    }

}
