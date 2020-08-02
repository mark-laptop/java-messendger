package ru.geekbrains.clienthandler;

public enum MessageManager {

    SERVER_DOWN("/server_down"),
    SERVER_DOWN_OK("/server_down_ok"),
    CLIENT_LIST("/clients_list "),
    AUTH("/auth "),
    AUTH_OK("/auth_ok "),
    REG("/reg "),
    REG_OK("/reg_ok "),
    SERVICE_SYMBOL("/"),
    WISP("/w "),
    END("/end"),
    END_CONFIRM("/end_confirm"),
    CHANGE_NICKNAME("/change_nickname "),
    CHANGE_NICKNAME_CONFIRM("/change_nickname_confirm ");

    private String text;

    public String getText() {
        return text;
    }

    MessageManager(String text) {
        this.text = text;
    }
}
