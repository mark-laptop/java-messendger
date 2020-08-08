package ru.geekbrains.controller;

import java.io.IOException;
import java.util.List;

public interface HistoryControllerManager {
    boolean openOrCreateFileHistory(String path) throws IOException;
    void writeMessage(String massage);
    List<String> read100LastMessageOnList();
    void close();
}
