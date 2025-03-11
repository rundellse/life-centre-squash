package org.rundellse.squashleague.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Helpful command for accessing h2 db through console, run in m2 h2\2.3.232 folder
// java -cp h2-2.3.232.jar org.h2.tools.Console
@Component
public class H2DatabaseConnection {

    private static final Logger LOG = LoggerFactory.getLogger(H2DatabaseConnection.class.getName());

    private static final String jdbcURL = "jdbc:h2:~/dbs/squash/test";
    private static final String jdbcUsername = "sa";
    private static final String jdbcPassword = "";

    public Connection getH2Connection() {
        Connection connection = null;
        try {
            LOG.debug("Getting connection to h2 database: " + jdbcURL);
            connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return connection;
    }

    // Handy little sql exception outputter
    // From https://www.javaguides.net/2019/08/jdbc-h2-database-create-read-update-and-delete-example-tutorial.html
    public static void logSQLException(SQLException ex) {
        for (Throwable e: ex) {
            if (e instanceof SQLException) {
                e.printStackTrace(System.err);
                LOG.error("SQLState: " + ((SQLException) e).getSQLState());
                LOG.error("Error Code: " + ((SQLException) e).getErrorCode());
                LOG.error("Message: " + e.getMessage());
                Throwable t = ex.getCause();
                while (t != null) {
                    LOG.error("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }

}
