package org.rundellse.squashleague.persistence;

import org.rundellse.squashleague.model.user.Role;
import org.rundellse.squashleague.model.user.User;
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
public class UserH2Dao {

    private static final Logger LOG = LoggerFactory.getLogger(UserH2Dao.class);

    @Autowired
    private H2DatabaseConnection h2DatabaseConnection;

    private static final String CREATE_SITE_USER_TABLE = """
            CREATE TABLE IF NOT EXISTS
            SITE_USER (ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                NAME VARCHAR(255) NOT NULL, EMAIL VARCHAR(255) NOT NULL,
                PASSWORD VARCHAR(255) NOT NULL);
            """;

    private static final String CREATE_ROLE_TABLE = """
            CREATE TABLE IF NOT EXISTS
            ROLE (ID BIGINT AUTO_INCREMENT PRIMARY KEY, NAME VARCHAR(255) NOT NULL)
            """;

    private static final String CREATE_SITE_USER_ROLE_TABLE = """
            CREATE TABLE IF NOT EXISTS
            USER_ROLE (ID BIGINT AUTO_INCREMENT PRIMARY KEY, USER_ID BIGINT, ROLE_ID BIGINT);
            """;

    private static final String CREATE_USER_ROLE_USER_FK_CONSTRAINT = """
            ALTER TABLE USER_ROLE
            ADD FOREIGN KEY (USER_ID)
            REFERENCES SITE_USER(ID);
            """;

    private static final String CREATE_USER_ROLE_ROLE_FK_CONSTRAINT = """
            ALTER TABLE USER_ROLE
            ADD FOREIGN KEY (ROLE_ID)
            REFERENCES ROLE(ID);
            """;

    private final static String SELECT_USER = """
            SELECT ID, NAME, EMAIL
            FROM SITE_USER
            WHERE ID = ?;
            """;

    private final static String SELECT_PASSWORD = """
            SELECT ID, PASSWORD
            FROM SITE_USER
            WHERE ID = ?;
            """;

    private final static String SELECT_ROLES_FOR_SITE_USER = """
            SELECT ID, NAME
            FROM ROLE r
            INNER JOIN USER_ROLE ur on r.ID = ur.ROLE_ID
            WHERE ur.USER_ID = ?;
            """;

    public void createTables() {
        LOG.info("Creating User & Role tables");
        try (Connection connection = h2DatabaseConnection.getH2Connection();
             Statement statement = connection.createStatement();
        ) {
            statement.execute(CREATE_SITE_USER_TABLE);
            statement.execute(CREATE_ROLE_TABLE);
            statement.execute(CREATE_SITE_USER_ROLE_TABLE);
            statement.execute(CREATE_USER_ROLE_USER_FK_CONSTRAINT);
            statement.execute(CREATE_USER_ROLE_ROLE_FK_CONSTRAINT);
            LOG.info("User & Role tables created");
        } catch (SQLException e) {
            H2DatabaseConnection.logSQLException(e);
        }
    }

    public User getSiteUserById(Long id) {
        LOG.debug("Getting User from database");

        User siteUser;
        try (Connection connection = h2DatabaseConnection.getH2Connection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER);
        ) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.first()) {
                throw new NullPointerException("User not found in db");
            }

            siteUser = new User(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("email")
            );
        } catch (SQLException e) {
            LOG.error("Error while fetching Player with ID: " + id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found", e);
        }

        return siteUser;
    }

    public String getPasswordForUser(Long id) {
        LOG.debug("Getting password from database");

        String password;
        try (Connection connection = h2DatabaseConnection.getH2Connection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PASSWORD);
        ) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.first()) {
                throw new NullPointerException("User not found in db");
            }

            password = resultSet.getString("password");
        } catch (SQLException e) {
            LOG.error("Error while fetching password for User with ID: " + id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found", e);
        }

        return password;
    }

    public List<Role> getRolesForSiteUser(Long userId) {
        LOG.debug("Getting User from database");

        List<Role> userRoles = new ArrayList<>();
        try (Connection connection = h2DatabaseConnection.getH2Connection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ROLES_FOR_SITE_USER);
        ) {
            preparedStatement.setLong(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.first()) {
                throw new NullPointerException("No roles found for User in db");
            }

            while (resultSet.next()) {
                userRoles.add(new Role(
                        resultSet.getLong("id"),
                        resultSet.getString("name")
                ));
            }
        } catch (SQLException e) {
            LOG.error("Error while fetching Roles for User with ID: " + userId, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Roles not found for user", e);
        }

        return userRoles;
    }



}
