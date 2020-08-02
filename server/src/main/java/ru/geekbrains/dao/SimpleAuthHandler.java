package ru.geekbrains.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleAuthHandler implements AuthHandler {
    private class Entry {
        private String login;
        private String password;
        private String nickname;

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        public String getNickname() {
            return nickname;
        }

        public Entry(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    private List<Entry> entries;

    public SimpleAuthHandler() {
        this.entries = Collections.synchronizedList(new ArrayList<>());
        this.entries.add(new Entry("login1", "pass1", "nick1"));
        this.entries.add(new Entry("login2", "pass2", "nick2"));
        this.entries.add(new Entry("login3", "pass3", "nick3"));
    }

    @Override
    public String getNickByLoginPass(String login, String password) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getLogin().equals(login) && entries.get(i).getPassword().equals(password)) {
                return entries.get(i).getNickname();
            }
        }
        return null;
    }

    @Override
    public boolean changeNickName(String login, String newNickname) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).login.equals(login) ) {
                entries.get(i).nickname = newNickname;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean addUser(String login, String password, String nickname) {
        this.entries.add(new Entry(login, password, nickname));
        return true;
    }

    @Override
    public boolean isLoginBusy(String login) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).login.equals(login)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        System.out.println("SimpleAuthHandler started...");
    }

    @Override
    public void stop() {
        System.out.println("SimpleAuthHandler stopped...");
    }
}
