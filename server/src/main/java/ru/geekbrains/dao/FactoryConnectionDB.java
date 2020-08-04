package ru.geekbrains.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class FactoryConnectionDB {

    private static final String DRIVER_CLASS = "org.sqlite.JDBC";
    private static final String URL_DATABASE = "jdbc:sqlite:chat_users.db";
    private Connection connection;
    private static FactoryConnectionDB instance;

    private FactoryConnectionDB() throws ClassNotFoundException, SQLException {
        Class.forName(DRIVER_CLASS);
        connection = DriverManager.getConnection(URL_DATABASE);
    }

    public synchronized Connection getConnection() {
        try {
            if (connection != null && connection.isClosed()) {
                connection = DriverManager.getConnection(URL_DATABASE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public synchronized static FactoryConnectionDB getFactoryConnectionDB() throws SQLException, ClassNotFoundException {
        if (instance != null) {
            return instance;
        }
        instance = new FactoryConnectionDB();
        return instance;
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
