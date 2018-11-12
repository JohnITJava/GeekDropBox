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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    VBox mainVBox;

    @FXML
    TextField tfFileName;

    @FXML
    ListView<String> localFilesList, serverFilesList;

    private AuthObject authObject;

    public void setAuthObject(AuthObject authObject) {
        this.authObject = authObject;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Platform.runLater(() -> ((Stage) mainVBox.getScene().getWindow()).setOnCloseRequest(t -> {
            Platform.exit();
        }));

        Platform.runLater(() -> {
                ((Stage) mainVBox.getScene().getWindow()).setTitle("Welcome to DropBox " + authObject.getLogin());
        });

        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractObject am = Network.readObject();
                    if (am instanceof FileObject) {
                        FileObject fm = (FileObject) am;
                        Files.write(Paths.get("client_storage"
                                + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
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
        localFilesList.setItems(FXCollections.observableArrayList());
        serverFilesList.setItems(FXCollections.observableArrayList());
        refreshLocalFilesList();
        refreshServerFilesList();
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        Path path = Paths.get(authObject.getLogin());
        if (tfFileName.getLength() > 0) {
            Network.sendObject(new FileRequest(path, tfFileName.getText()));
            tfFileName.clear();
        }
    }

    public void refreshLocalFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                localFilesList.getItems().clear();
                Files.list(Paths.get("client_storage")).map(p ->
                        p.getFileName().toString()).forEach(o ->
                        localFilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    localFilesList.getItems().clear();
                    Files.list(Paths.get("client_storage")).map(p ->
                            p.getFileName().toString()).forEach(o ->
                            localFilesList.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void refreshServerFilesList(){
        Path path = Paths.get(authObject.getLogin());
        Network.sendObject(new FilesListRequest(path));
    }

    public void logOffSystem(){
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth.fxml"));
            root = (Pane) loader.load();
            Scene scene = new Scene(root, 250, 150);
            Stage stage = ((Stage) mainVBox.getScene().getWindow());
            stage.setScene(scene);
            stage.setTitle("GeekDropBox");

        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
