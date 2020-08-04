package ru.geekbrains.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import ru.geekbrains.clienthandler.MessageManager;
import ru.geekbrains.network.Network;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller implements Initializable {
    private boolean authorized;
    private Network network;
    private String nickname;
    private ObservableList<String> clientsList;

    @FXML
    TextField msgField, loginField, addressField, portField;

    @FXML
    TextArea mainTextArea;

    @FXML
    PasswordField passField;

    @FXML
    HBox authPanel, msgPanel, connectPanel;

    @FXML
    ListView<String> clientsView;


    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
        if (this.authorized) {
            authPanel.setVisible(false);
            authPanel.setManaged(false);
            connectPanel.setVisible(false);
            connectPanel.setManaged(false);
            /*Доработать регистрацию через всплывающее окно ввода данных*/
//            msgPanel.setVisible(true);
//            msgPanel.setManaged(true);
            clientsView.setVisible(true);
            clientsView.setManaged(true);
        } else {
            authPanel.setVisible(true);
            authPanel.setManaged(true);
            connectPanel.setVisible(true);
            connectPanel.setManaged(true);
            /*Доработать регистрацию через всплывающее окно ввода данных*/
//            msgPanel.setVisible(false);
//            msgPanel.setManaged(false);
            clientsView.setVisible(false);
            clientsView.setManaged(false);
            nickname = "";
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorized(false);
        clientsList = FXCollections.observableArrayList();
        clientsView.setItems(clientsList);
        network = new Network(addressField.getText(), portField.getText());
    }

    public void sendMsg() {
        if (network.isNotConnected()) {
            showAlert("Авторизируйтесь для отправки сообщений");
            msgField.clear();
            msgField.requestFocus();
            return;
        }
        network.sendMessage(msgField.getText());
        msgField.clear();
        msgField.requestFocus();
    }

    public void sendAuth(ActionEvent actionEvent) {
        if(!connectDataIsCorrect()) return;
        network.setAddress(addressField.getText());
        network.setPort(portField.getText());
        if (network.isNotConnected()) {
            try {
                connected();
            } catch (IOException e) {
                showAlert("Невозможно подключиться к серверу, проверьте сетевое соединение...");
            }
        }
        if (!network.isNotConnected()) {
            if (network.sendMessage(MessageManager.AUTH.getText() + loginField.getText() + " " + passField.getText())) {
                loginField.clear();
                passField.clear();
            } else {
                showAlert("Невозможно подключиться к серверу, проверьте сетевое соединение...");
            }
        }
    }

    public void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
            alert.showAndWait();
        });
    }

    public void clickClientsList(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            String str = clientsView.getSelectionModel().getSelectedItem();
            msgField.setText(MessageManager.WISP.getText() + str + " ");
            msgField.requestFocus();
            msgField.selectEnd();
        }
    }

    public void sendReg(ActionEvent actionEvent) {
        if(!connectDataIsCorrect()) return;
        network.setAddress(addressField.getText());
        network.setPort(portField.getText());
        if (network.isNotConnected()) {
            try {
                connected();
            } catch (IOException e) {
                showAlert("Невозможно подключиться к серверу, проверьте сетевое соединение...");
            }
        }
        if (!network.isNotConnected()) {
            if (network.sendMessage(MessageManager.REG.getText() + loginField.getText() + " " + passField.getText() + " " + msgField.getText())) {
                loginField.clear();
                passField.clear();
                msgField.clear();
            } else {
                showAlert("Невозможно подключиться к серверу, проверьте сетевое соединение...");
            }
        }
    }

    private boolean connectDataIsCorrect() {
        if (addressField.getText().isEmpty()) {
            showAlert("Не заполен адресс сервера!");
            return false;
        }
        if (!isNumber(portField.getText())) {
            showAlert("Порт должен содержать только цифры!");
            return false;
        }
        return true;
    }

    private boolean isNumber(String string) {
        Pattern pattern = Pattern.compile("[0-9]+$");
        Matcher matcher = pattern.matcher(string);
        return matcher.matches();
    }

    private void connected() throws IOException {
        network.connect(
                argsGetMessage -> mainTextArea.appendText(argsGetMessage[0]),
                argsAuthOk -> {
                    nickname = argsAuthOk[0];
                    setAuthorized(true);
                },
                argsGetClientsList -> Platform.runLater(() -> {
                    clientsList.clear();
                    for (int i = 1; i < argsGetClientsList.length; i++) {
                        if (argsGetClientsList[i].equals(nickname)) continue;
                        clientsList.add(argsGetClientsList[i]);
                    }
                }),
                argsDisconnect -> {
                    showAlert("Произошло отключение от сервера");
                    setAuthorized(false);
                }
        );
    }
}
