package com.geekbrains.client;

import com.geekbrains.common.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    VBox mainVBox;

    @FXML
    ListView<String> localFilesList, serverFilesList;

    @FXML
    Button uplBtn, dwnlBtn, updBtn, delBtn;

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
                    }
                    if (income instanceof FilesListObject) {
                        FilesListObject flo = (FilesListObject) income;
                        updateGUI(() -> {serverFilesList.getItems().addAll(flo.getFileNamesList());});
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

        Thread tskUpdLists = new Thread(taskUpdateLists);
        tskUpdLists.setDaemon(true);
        tskUpdLists.start();

        serverFilesList.setOnMouseClicked(event -> {
            localFilesList.getSelectionModel().clearSelection();
        });
        localFilesList.setOnMouseClicked(event -> {
            Platform.runLater(() -> {serverFilesList.getSelectionModel().clearSelection();});
        });


    }

    ////////

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        String choosenFile = serverFilesList.getFocusModel().getFocusedItem();
        Network.sendObject(new FileRequest(choosenFile));
    }

    /*public void pressOnUpdateBtn() {
            refreshServerFilesList();
            refreshLocalFilesList();
    }*/

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
        refreshServerFilesList();

    }


    public void pressOnDeleteBtn(ActionEvent actionEvent) throws IOException {
        String locName = localFilesList.getSelectionModel().getSelectedItem();
        String servName = serverFilesList.getSelectionModel().getSelectedItem();
        if (locName != null){
            Files.delete(Paths.get("client_storage/" + locName));
        }
        if (servName != null){
            Network.sendObject(new CMD("/delete", servName, null));
            refreshServerFilesList();
        }
    }


    Task taskUpdateLists = new Task() {
        @Override
        protected Object call() throws Exception {
            Path pathToLocalStorage = Paths.get("client_storage");
            WatchService watchService = null;
            try {
                watchService = pathToLocalStorage.getFileSystem().newWatchService();
                pathToLocalStorage.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (; ; ) {
                WatchKey key = null;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (WatchEvent event : key.pollEvents()) {
                    try {
                        refreshLocalList();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
                key.reset();
            }
        }
    };

    public void refreshLocalList() {
        Platform.runLater(() -> {
            try {
                localFilesList.getItems().clear();
                Files.list(Paths.get("client_storage")).map(f ->
                        f.getFileName().toString()).forEach(f -> localFilesList.getItems().add(f));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
