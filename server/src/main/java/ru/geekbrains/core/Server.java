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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private ServerSocket serverSocket;
    private AuthHandler authHandler;
    private List<ClientHandler> clients;
    private final int port;
    private ExecutorService executorService;
    private long countCreatedThreads;

    public Server(int port) {
       this.port = port;
    }

    public AuthHandler getAuthHandler() {
        return authHandler;
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    // Добавил что-то типа счетчика, проверка на количество созданых в пуле потоков)
    public long getCountCreatedThreads() {
        return countCreatedThreads;
    }

    public void start() {
        try {
//            authHandler = new SimpleAuthHandler();
            authHandler = new DBAuthHandler();
            authHandler.start();
            serverSocket = new ServerSocket(port);
            clients = new ArrayList<>();
            // Выбрал для реализации newCachedThreadPool, думаю это будет правильно, ведь клиентов чата может быть сколько угодно
            // Ну а если упадем по ресурсам, значит пора делать апгрейд ресурсов)
            executorService = Executors.newCachedThreadPool();
            System.out.println("Сервер запущен");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
//                new ClientHandler(this, socket);
                // Поток создавался в классе ClientHandler после инициализации конструктора
                // переделал на создание потока через ExecutorService что бы ClientHandler
                // создавался и инициализировался уже в новом потоке.
                executorService.submit(() -> {
                    ClientHandler clientHandler = new ClientHandler(Server.this, socket);
                    clientHandler.startWork();
                });
                countCreatedThreads++;
                System.out.println(countCreatedThreads);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
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
        // На сколько правильно реализовал не знаю). Логика была такая. Проверяю завершены ли все задачи,
        // если нет то жду еще 5 минут, если по истечению 5 минут еще есть рабочие потоки то кидаю interrupt.
        // Но по факту, все потоки должны быть уже завершены до вызова метода stop()
        if (!executorService.isTerminated()) {
            boolean isDone = false;
            try {
                isDone = executorService.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!isDone) {
                executorService.shutdownNow();
            }
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
            sb.append(o.getNickname()).append(" ");
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