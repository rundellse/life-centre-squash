package org.rundellse.squashleague.persistence;

import org.rundellse.squashleague.model.Season;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.sql.*;
import java.time.LocalDate;

@Repository
public class SeasonH2DAO {

    private static final Logger LOG = LoggerFactory.getLogger(SeasonH2DAO.class.getName());

    @Autowired
    private H2DatabaseConnection h2DatabaseConnection;

    private final static String createSeasonTable = """
            CREATE TABLE IF NOT EXISTS
            SEASON (ID BIGINT AUTO_INCREMENT PRIMARY KEY, START_DATE DATE, END_DATE DATE);
            """;

    private final static String insertSeason = """
            INSERT INTO SEASON (START_DATE, END_DATE)
            VALUES (?,?);
            """;

    private final static String updateSeason = """
            UPDATE SEASON
            SET START_DATE = ?, END_DATE = ?
            WHERE ID = ?
            """;

    private final static String getSeasonForDate = """
            SELECT ID, START_DATE, END_DATE
            FROM SEASON
            WHERE START_DATE <= ?
            AND END_DATE > ?
            """;

    private final static String getLastSeason = """
            SELECT TOP 1
            ID, START_DATE, END_DATE
            FROM SEASON
            WHERE END_DATE = (SELECT MAX(END_DATE) FROM SEASON)
            """;


    public void createTable() {
        try (Connection connection = h2DatabaseConnection.getH2Connection();
             Statement statement = connection.createStatement();
        ) {
            statement.execute(createSeasonTable);
            LOG.info("Season table created successfully.");
        } catch (SQLException e) {
            H2DatabaseConnection.logSQLException(e);
        }
    }

    public long persistSeason(Season season) {
        LOG.info("Saving a new Season to db. Start Date: {}, End Date: {}", season.getStartDate(), season.getEndDate());

        long newId;
        try (Connection connection = h2DatabaseConnection.getH2Connection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertSeason, Statement.RETURN_GENERATED_KEYS);
        ) {
            preparedStatement.setObject(1, season.getStartDate());
            preparedStatement.setObject(2, season.getEndDate());

            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                newId = generatedKeys.getLong("id");
            } else {
                throw new SQLDataException("Not able to get ID for saved Season");
            }
        } catch (SQLException e) {
            LOG.error("SQL Exception while attempting to save Season to database with ID: {}", season.getId());
            H2DatabaseConnection.logSQLException(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while persisting Season", e);
        }

        return newId;
    }

    public Season getSeasonForDate(LocalDate date) {
        LOG.info("Fetching Season for Date: {}", date);

        try (Connection connection = h2DatabaseConnection.getH2Connection();
             PreparedStatement preparedStatement = connection.prepareStatement(getSeasonForDate);
        ) {
            preparedStatement.setObject(1, date);
            preparedStatement.setObject(2, date);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet == null || !resultSet.first()) {
                return null;
            }

            return new Season(
                    resultSet.getLong("id"),
                    LocalDate.parse(resultSet.getString("start_date")),
                    LocalDate.parse(resultSet.getString("end_date"))
            );

        } catch (SQLException e) {
            LOG.error("SQL Exception while attempting to fetch Season from database for date: {}", date);
            H2DatabaseConnection.logSQLException(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while fetching Season", e);
        }
    }

    public Season getLastSeason() {
        LOG.info("Fetching the latest-ending Season.");

        try (Connection connection = h2DatabaseConnection.getH2Connection();
             Statement statement = connection.createStatement();
        ) {
            ResultSet resultSet = statement.executeQuery(getLastSeason);
            if (resultSet == null || !resultSet.first()) {
                return null;
            }

            return new Season(
                    resultSet.getLong("id"),
                    LocalDate.parse(resultSet.getString("start_date")),
                    LocalDate.parse(resultSet.getString("end_date"))
            );

        } catch (SQLException e) {
            LOG.error("SQL Exception while attempting to fetch last Season from database.");
            H2DatabaseConnection.logSQLException(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while fetching Season", e);
        }
    }

    public Season updateSeason(Season season) {
        LOG.info("Updating player with ID: {}", season.getId());

        try (Connection connection = h2DatabaseConnection.getH2Connection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateSeason)) {

            preparedStatement.setObject(1, season.getStartDate());
            preparedStatement.setObject(2, season.getEndDate());
            preparedStatement.setLong(3, season.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOG.error("SQL Exception while attempting to update Season in database with ID: " + season.getId());
            H2DatabaseConnection.logSQLException(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while updating Season", e);
        }

        return season;
    }


}
