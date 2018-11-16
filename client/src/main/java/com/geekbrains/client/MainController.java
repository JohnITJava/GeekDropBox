package com.geekbrains.client;

import com.geekbrains.common.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    VBox mainVBox;

    @FXML
    ListView<String> localFilesList, serverFilesList;

    private String userName;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Platform.runLater(() -> ((Stage) mainVBox.getScene().getWindow()).setOnCloseRequest(t -> {
            Platform.exit();
        }));

        Thread t = new Thread(() -> {
            try {
                while (true) {

                    AbstractObject income = Network.readObject();

                    if (income instanceof UserObject) {
                        UserObject uo = (UserObject) income;
                        this.userName = uo.getName();
                        updateTitle(userName);
                    }
                    if (income instanceof FileObject) {
                        FileObject fm = (FileObject) income;
                        Files.write(Paths.get("client_storage/"
                                + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                    }
                    if (income instanceof FilesListObject) {
                        FilesListObject flo = (FilesListObject) income;
                        serverFilesList.getItems().addAll(flo.getFileNamesList());
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });

        t.setDaemon(true);
        t.start();
        getUserObject();
        serverFilesList.setItems(FXCollections.observableArrayList());
        refreshServerFilesList();
        localFilesList.setItems(FXCollections.observableArrayList());
        refreshLocalFilesList();

    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        String choosenFile = serverFilesList.getFocusModel().getFocusedItem();
        System.out.println(choosenFile);
        Network.sendObject(new FileRequest(choosenFile));
    }

    public void pressOnUpdateBtn() {
            refreshServerFilesList();
            refreshLocalFilesList();
    }

    public void refreshLocalFilesList() {
        updateGUI(() -> {
            localFilesList.getItems().clear();
            try {
                Files.list(Paths.get("client_storage")).map(p ->
                        p.getFileName().toString()).forEach(o ->
                        localFilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void refreshServerFilesList() {
        serverFilesList.getItems().clear();
        Network.sendObject(new FilesListRequest());
    }

    public void logOffSystem() {
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth.fxml"));
            root = (Pane) loader.load();
            Scene scene = new Scene(root, 250, 150);
            Stage stage = ((Stage) mainVBox.getScene().getWindow());
            stage.setScene(scene);
            stage.setTitle("GeekDropBox");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateTitle(String name) {
        Platform.runLater(() -> ((Stage) mainVBox.getScene().getWindow()).setTitle("Welcome to DropBox " + name));
    }

    public void getUserObject() {
        Network.sendObject(new UserRequest());
    }

    public static void updateGUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
                r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public void pressOnUploadBtn(ActionEvent actionEvent) throws IOException {
        String fileName = localFilesList.getFocusModel().getFocusedItem();
        Network.sendObject(new FileObject(Paths.get("client_storage/" + fileName)));
    }


    public void pressOnDeleteBtn(ActionEvent actionEvent) {

    }
}
