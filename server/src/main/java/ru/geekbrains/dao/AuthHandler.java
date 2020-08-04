package ru.geekbrains.dao;

public interface AuthHandler {
    void start();
    void stop();
    boolean addUser(String login, String password, String nickname);
    boolean changeNickName(String login, String newNickname);
    boolean isLoginBusy(String login);
    String getNickByLoginPass(String login, String password);
}
