package io.jwixel.esplugins.auth.store;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class SqliteStore implements IAuthStore {
    protected final Logger log = LogManager.getLogger(this.getClass());
    private static String CONNECTION_STRING = "jdbc:sqlite:/home/elasticsearch/auth.db";

    /*
     * NOTE: Committing this with auth store PR even though it isn't working atm.
     * There are significant challenges to getting this store to sync across shardcs
     * which may or may not be a part of the eventual implementation.  Also need to
     * finalize permissions work.
     * */
    public SqliteStore() {
        Connection connection = null;

        try {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch(ClassNotFoundException e) {
                this.log.error(e);
            }

            connection = DriverManager.getConnection(CONNECTION_STRING);
            // TODO: The following code goes away with an install/setup strategy
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(12);

            statement.executeUpdate("drop table if exists users");
            statement.executeUpdate("create table person (id integer, username string, password string)");
            statement.executeUpdate("insert into person values(1, 'jwixel', 'pass!')");
        } catch(SQLException e) {
            this.log.error(e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch(SQLException e) {
                this.log.error(e);
            }
        }

    }

    @Override
    public Boolean authentic(String auth) throws Exception {
        String[] parts = auth.split(":");
        if (parts.length != 2) {
            this.log.error("Unable to parse username and password from auth string: ", auth);
            return false;
        }
        String user = parts[0];
        String pass = parts[1];
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(CONNECTION_STRING);

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(12);

            ResultSet rs = statement.executeQuery(
                String.format("select * from users where username = '%s' and password = '%s'", user, pass)
            );
            if (rs.first()) {
                this.log.info("Matched user/pw pair. Authentication succeeded.");
                connection.close();
                return true;
            }
        } catch(SQLException e) {
            this.log.error(e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch(SQLException e) {
                this.log.error(e);
            }
        }

        return false;
    }
}
