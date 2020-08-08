package ru.geekbrains.network;

import ru.geekbrains.clienthandler.MessageManager;
import ru.geekbrains.gui.CallbackArguments;
import ru.geekbrains.controller.HistoryController;
import ru.geekbrains.controller.HistoryControllerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Network {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String address;
    private String port;
    private ExecutorService executorService;
    private HistoryControllerManager historyControllerManager;
    private String suffixHistoryNameFile = "-history.txt";

    public Network(String address, String port) {
        this.address = address;
        this.port = port;
        this.historyControllerManager = new HistoryController();
        this.executorService = Executors.newFixedThreadPool(3);
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public boolean sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isNotConnected() {
        return socket == null || socket.isClosed();
    }

    public void connect(CallbackArguments<String> callMessageToTextArea,
                        CallbackArguments<String> callAuthOk,
                        CallbackArguments<String> callClientsList,
                        CallbackArguments<?> callDisconnect) throws IOException {
        try {
            socket = new Socket(address, Integer.parseInt(port));
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            Thread t = new Thread(() -> {

                try {
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith(MessageManager.AUTH_OK.getText())) {
                            String nickname = str.split(" ")[1];
                            callAuthOk.callback(nickname);
                            callMessageToTextArea.callback("Вы авторизировались под ником " + nickname + "\n");
                            readHistoryMessage(callMessageToTextArea, nickname);
                            break;
                        }
                        if (str.startsWith(MessageManager.REG_OK.getText())) {
                            String nickname = str.split(" ")[1];
                            callAuthOk.callback(nickname);
                            callMessageToTextArea.callback("Вы зарегистрированы под ником " + nickname + "\n");
                            readHistoryMessage(callMessageToTextArea, nickname);
                            break;
                        }
                        callMessageToTextArea.callback(str + "\n");

                    }
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith(MessageManager.SERVICE_SYMBOL.getText())) {
                            if (str.startsWith(MessageManager.CLIENT_LIST.getText())) {
                                String[] tokens = str.split(" ");
                                callClientsList.callback(tokens);
                            }
                            if (str.startsWith(MessageManager.END_CONFIRM.getText())) {
                                callMessageToTextArea.callback("Сервер закрыл соединение!\n");
                                break;
                            }
                            if (str.startsWith(MessageManager.SERVER_DOWN.getText())) {
                                callMessageToTextArea.callback("Сервер закрыл соединение!\n");
                                sendMessage(MessageManager.SERVER_DOWN_OK.getText());
                                break;
                            }
                            if (str.startsWith(MessageManager.CHANGE_NICKNAME_CONFIRM.getText())) {
                                String nickname = str.split(" ")[1];
                                callAuthOk.callback(nickname);
                                callMessageToTextArea.callback("Ваш новый ник " + nickname + "\n");

                            }
                        } else {
                            callMessageToTextArea.callback(str + "\n");
                            executorService.submit(() -> {
                                historyControllerManager.writeMessage(str + "\n");
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    callDisconnect.callback();
                    historyControllerManager.close();
                    closeConnection();
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (Exception e) {
//            e.printStackTrace();
            throw e;
        }
    }

    private void readHistoryMessage(CallbackArguments<String> callMessageToTextArea, String nickname) {
        executorService.submit(() -> {
            try {
                historyControllerManager.openOrCreateFileHistory(nickname + suffixHistoryNameFile);
                List<String> last100Message = historyControllerManager.read100LastMessageOnList();
                callMessageToTextArea.callback("----------История чата----------" + "\n");
                for (int i = 0; i < last100Message.size(); i++) {
                    callMessageToTextArea.callback(last100Message.get(i) + "\n");
                }
                callMessageToTextArea.callback("----------История чата----------" + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void closeConnection() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
