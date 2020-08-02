package ru.geekbrains.network;

import ru.geekbrains.clienthandler.MessageManager;
import ru.geekbrains.gui.CallbackArguments;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String address;
    private String port;

    public Network(String address, String port) {
        this.address = address;
        this.port = port;
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
                            break;
                        }
                        if (str.startsWith(MessageManager.REG_OK.getText())) {
                            String nickname = str.split(" ")[1];
                            callAuthOk.callback(nickname);
                            callMessageToTextArea.callback("Вы зарегистрированы под ником " + nickname + "\n");
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
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    callDisconnect.callback();
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
