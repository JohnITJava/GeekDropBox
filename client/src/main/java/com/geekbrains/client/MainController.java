package com.geekbrains.client;

import com.geekbrains.common.*;
import com.geekbrains.common.File;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static final int MAX_OBJ_SIZE = 1024 * 1024 * 5;

    @FXML
    VBox mainVBox;

    @FXML
    TableView<File> localFilesTable, serverFilesTable;

    @FXML
    Label filesDragAndDrop, progressLabel;

    private String userName;
    private List<File> serverFiles;
    ObservableList<File> serverFilesList = FXCollections.observableArrayList();
    ObservableList<File> localFilesList = FXCollections.observableArrayList();


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
                            serverFiles = flo.getServerFiles();
                            updateGUI(() -> {
                                serverFilesList.addAll(serverFiles);
                            });
                        }
                    if (income instanceof BigDataInfo) {
                        BigDataInfo info = (BigDataInfo) income;
                        if (ExtController.bigDataHandler(info)){
                            refreshServerFilesList();
                            }
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

        initializeDragAndDropLabel();
        refreshServerFilesList();

        try {
            initializeServerFilesTable();
            initializeLocalFilesTable();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Thread tskUpdLists = new Thread(taskUpdateLists);
        tskUpdLists.setDaemon(true);
        tskUpdLists.start();

        serverFilesTable.setOnMouseClicked(event -> {
            localFilesTable.getSelectionModel().clearSelection();
        });
        localFilesTable.setOnMouseClicked(event -> {
            Platform.runLater(() -> {
                serverFilesTable.getSelectionModel().clearSelection();
            });
        });


    }

    ////////

    public void initializeDragAndDropLabel() {
        filesDragAndDrop.setOnDragOver(event -> {
            filesDragAndDrop.setStyle("-fx-background-color: rgba(93, 204, 255, 0.51)");
            if (event.getGestureSource() != filesDragAndDrop && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        filesDragAndDrop.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                for (java.io.File o : db.getFiles()) {
                    progressLabel.setText(o.getName());
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        filesDragAndDrop.setOnDragExited(event -> {
            filesDragAndDrop.setStyle("-fx-background-color: rgba(93, 204, 255, 0.30);");
            event.consume();
        });
    }

    public void initializeLocalFilesTable() throws IOException {
        Files.list(Paths.get("client_storage")).forEach(o ->
                localFilesList.add(new File(o.getFileName().toString(), o.toFile().length())));

        TableColumn<File, String> tcName = new TableColumn<>("Name");
        tcName.setCellValueFactory(new PropertyValueFactory<File, String>("fileName"));

        TableColumn<File, String> tcSize = new TableColumn<>("Size, kb");
        tcSize.setCellValueFactory(new PropertyValueFactory<File, String>("size"));

        localFilesTable.getColumns().addAll(tcName, tcSize);
        localFilesTable.setItems(localFilesList);
    }

    public void initializeServerFilesTable() throws IOException {
        TableColumn<File, String> tcName = new TableColumn<>("Name");
        tcName.setCellValueFactory(new PropertyValueFactory<File, String>("fileName"));

        TableColumn<File, String> tcSize = new TableColumn<>("Size, kb");
        tcSize.setCellValueFactory(new PropertyValueFactory<File, String>("size"));

        serverFilesTable.getColumns().addAll(tcName, tcSize);
        serverFilesTable.setItems(serverFilesList);
    }

    /////

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        String choosenFile = null;
        if (serverFilesTable.getSelectionModel().getSelectedItem() != null) {
            choosenFile = serverFilesTable.getFocusModel().getFocusedItem().getFileName();
        }
        if (choosenFile != null) {
            Network.sendObject(new FileRequest(choosenFile));
        }
    }

    public void pressOnUploadBtn(ActionEvent actionEvent) throws IOException {
        String fileName = null;
        if (localFilesTable.getSelectionModel().getSelectedItem() != null) {
            fileName = localFilesTable.getFocusModel().getFocusedItem().getFileName();
            if (Paths.get("client_storage/" + fileName).toFile().length() > MAX_OBJ_SIZE) {
                ExtController.sendBigData(Paths.get("client_storage/" + fileName));
                return;
            }
        }
        if (fileName != null) {
            Network.sendObject(new FileObject(Paths.get("client_storage/" + fileName)));
            refreshServerFilesList();
        }
    }

    public void pressOnDeleteBtn(ActionEvent actionEvent) throws IOException {
        String filename = null;
        if (localFilesTable.getFocusModel().getFocusedItem() != null) {
            filename = localFilesTable.getSelectionModel().getSelectedItem().getFileName();
            Files.delete(Paths.get("client_storage/" + filename));
        }
        if (serverFilesTable.getSelectionModel().getSelectedItem() != null) {
            filename = serverFilesTable.getSelectionModel().getSelectedItem().getFileName();
            Network.sendObject(new CMD("/delete", filename, null));
            refreshServerFilesList();
        }
    }

    public void refreshLocalFilesList() {
        updateGUI(() -> {
            localFilesTable.getItems().clear();
            try {
                Files.list(Paths.get("client_storage")).forEach(o ->
                        localFilesList.add(new File(o.getFileName().toString(), o.toFile().length())));
                localFilesTable.setItems(localFilesList);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void refreshServerFilesList() {
        updateGUI(() -> {
            serverFilesTable.getItems().clear();
        });
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

    //Watcher on localList
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
                        refreshLocalFilesList();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
                key.reset();
            }
        }
    };

}
