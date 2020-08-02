package ru.geekbrains.servergui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ru.geekbrains.clienthandler.ClientHandler;
import ru.geekbrains.clienthandler.MessageManager;
import ru.geekbrains.core.Server;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerController implements Initializable {

    private Server server;

    @FXML
    private TextField portField;
    @FXML
    private Label textInfoStatusServer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textInfoStatusServer.setText("Сервере остановлен!");
    }

    public void startServer(ActionEvent actionEvent) {
        if (server != null) return;
        String port = portField.getText();
        if (port.isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Необходимо ввести номер порта", ButtonType.OK);
                alert.showAndWait();
            });
            return;
        }
        if (!isNumber(port)) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Номер порта состоит только из цифр", ButtonType.OK);
                alert.showAndWait();
            });
            return;
        }
        this.server = new Server(Integer.parseInt(portField.getText()));
        new Thread(() -> server.start()).start();
        textInfoStatusServer.setText("Сервер запущен!");
    }

    public void stopServer(ActionEvent actionEvent) {
        if (server == null) return;
        List<ClientHandler> clients = server.getClients();
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(MessageManager.SERVER_DOWN.getText());
        }
        clients.clear();
        if (server != null) {
            server.stop();
            server = null;
        }
        textInfoStatusServer.setText("Сервере остановлен!");
    }

    private boolean isNumber(String string) {
        Pattern pattern = Pattern.compile("[0-9]+$");
        Matcher matcher = pattern.matcher(string);
        return matcher.matches();
    }
}
