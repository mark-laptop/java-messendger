package ru.geekbrains.controller;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class HistoryController implements HistoryControllerManager {

    private File file;
    private BufferedReader br;
    private BufferedWriter bw;

    @Override
    public boolean openOrCreateFileHistory(String path) throws IOException {
        boolean result = true;
        file = new File(path);
        if (file.isDirectory()) {
            throw new RuntimeException("Переданный путь является директорией! " + path);
        }
        if (!file.exists()) {
            result = file.createNewFile();
        }
        br = new BufferedReader(new FileReader(file));
        bw = new BufferedWriter(new FileWriter(file, true));
        return result;
    }

    @Override
    public void writeMessage(String message) {
        try {
            bw.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> read100LastMessageOnList() {
        List<String> listMessages = new ArrayList<>();
        try {
            while (br.ready()) {
                listMessages.add(br.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listMessages;
    }

    @Override
    public void close() {
        if (bw != null) {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        file = null;
    }
}
