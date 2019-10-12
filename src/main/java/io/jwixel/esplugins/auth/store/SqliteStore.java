package io.jwixel.esplugins.auth.store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class SqliteStore implements IAuthStore {
    public SqliteStore() {}

    @Override
    public Boolean authentic(String auth) {
        return true;
    }
}
