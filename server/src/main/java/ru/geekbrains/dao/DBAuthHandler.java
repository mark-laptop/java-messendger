package ru.geekbrains.dao;

import java.sql.*;

public class DBAuthHandler implements AuthHandler {

    private static final String ADD_NEW_USER = "INSERT INTO users (login, password, nickname) VALUES (?, ?, ?);";
    private static final String CHANGE_NICKNAME_USER = "UPDATE users SET nickname = ? WHERE login = ?;";
    private static final String GET_LOGIN_USER = "SELECT login FROM users WHERE login = ?;";
    private static final String GET_NICKNAME_USER = "SELECT nickname FROM users WHERE login = ? AND password = ?;";

    private final FactoryConnectionDB factoryConnectionDB;
    private final PreparedStatement addNeUser;
    private final PreparedStatement changeNicknameUser;
    private final PreparedStatement getLoginUser;
    private final PreparedStatement getNicknameUser;

    public DBAuthHandler() throws ClassNotFoundException, SQLException {
        this.factoryConnectionDB = FactoryConnectionDB.getFactoryConnectionDB();
        this.addNeUser = factoryConnectionDB.getConnection().prepareStatement(ADD_NEW_USER);
        this.changeNicknameUser = factoryConnectionDB.getConnection().prepareStatement(CHANGE_NICKNAME_USER);
        this.getLoginUser = factoryConnectionDB.getConnection().prepareStatement(GET_LOGIN_USER);
        this.getNicknameUser = factoryConnectionDB.getConnection().prepareStatement(GET_NICKNAME_USER);
    }

    @Override
    public void start() {
        System.out.println("SimpleAuthHandler started...");
    }

    @Override
    public void stop() {
        closeConnection();
        System.out.println("SimpleAuthHandler stopped...");
    }

    @Override
    public synchronized boolean addUser(String login, String password, String nickname) {
        int result = 0;
        try {
            PreparedStatement statement = addNeUser;
            statement.setString(1, login);
            statement.setString(2, password);
            statement.setString(3, nickname);
            result = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result != 0;
    }

    @Override
    public synchronized boolean changeNickName(String login, String newNickname) {
        int result = 0;
        try {
            PreparedStatement statement = changeNicknameUser;
            statement.setString(1, newNickname);
            statement.setString(2, login);
            result = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result != 0;
    }

    @Override
    public synchronized boolean isLoginBusy(String login) {
        try {
            PreparedStatement statement = getLoginUser;
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public synchronized String getNickByLoginPass(String login, String password) {
        try {
            PreparedStatement statement = getNicknameUser;
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void closeConnection() {
        try {
            if (addNeUser != null)
                addNeUser.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (changeNicknameUser != null)
                changeNicknameUser.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (getLoginUser != null)
                getLoginUser.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (getNicknameUser != null)
                getNicknameUser.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (factoryConnectionDB != null)
                factoryConnectionDB.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
