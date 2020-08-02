package ru.geekbrains.core;

import ru.geekbrains.clienthandler.MessageManager;
import ru.geekbrains.dao.AuthHandler;
import ru.geekbrains.clienthandler.ClientHandler;
import ru.geekbrains.dao.DBAuthHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    private AuthHandler authHandler;
    private List<ClientHandler> clients;
    private final int port;

    public Server(int port) {
       this.port = port;
    }

    public AuthHandler getAuthHandler() {
        return authHandler;
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    public void start() {
        try {
//            authHandler = new SimpleAuthHandler();
            authHandler = new DBAuthHandler();
            authHandler.start();
            serverSocket = new ServerSocket(port);
            clients = new ArrayList<>();
            System.out.println("Сервер запущен");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            if(serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (authHandler != null) {
            authHandler.stop();
        }
    }

    public void sendPrivateMsg(ClientHandler from, String to, String msg) {
        for (ClientHandler o : clients) {
            if (o.getNickname().equals(to)) {
                o.sendMessage("from " + from.getNickname() + ": " + msg);
                from.sendMessage("to " + to + ": " + msg);
                return;
            }
        }
        from.sendMessage("Клиент " + to + " не найден!\n");
    }

    public void broadcastMsg(ClientHandler client, String msg) {
        String outMsg = client.getNickname() + ": " + msg;
        for (ClientHandler o : clients) {
            o.sendMessage(outMsg);
        }
    }

    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder();
        sb.append(MessageManager.CLIENT_LIST.getText());
        for (ClientHandler o : clients) {
            sb.append(o.getNickname() + " ");
        }
        sb.setLength(sb.length() - 1);
        String out = sb.toString();
        for (ClientHandler o : clients) {
            o.sendMessage(out);
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientsList();
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getNickname().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public boolean isLoginBusy(String login) {
        return authHandler.isLoginBusy(login);
    }
}