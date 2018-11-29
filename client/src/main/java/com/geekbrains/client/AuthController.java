package com.geekbrains.client;

import com.geekbrains.common.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;


public class AuthController implements Initializable{
    @FXML
    VBox mainVBox;

    @FXML
    TextField logF, passF;

    @FXML
    Label hint;

    @Override
    public void initialize(URL location, ResourceBundle resources){
        hint.setText("Log In system CloudDropBox or register yourself");

        Network.start();

        Platform.runLater(() -> ((Stage) mainVBox.getScene().getWindow()).setOnCloseRequest(t -> {
            Platform.exit();
        }));

        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractObject income = Network.readObject();
                    if (income instanceof AuthObject) {
                        AuthObject ao = (AuthObject) income;
                        if (ao.isAuthorized()) {
                            textInGUI("SUCCESS. Loading the system, please, wait...");
                            Platform.runLater(() -> switchSceneToBox());
                            break;
                        } else {
                            textInGUI("Wrong combination of login and pass");
                        }
                    }
                    if (income instanceof RegObject){
                        RegObject ro = (RegObject) income;
                        if (ro.isRegistered()) {
                            textInGUI("Registration is SUCCESS");
                            logF.clear();
                            passF.clear();
                        } else {
                            textInGUI("Registration aborted, such login has already exists");
                        }
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void logIn(){
        if (logF.getLength() > 0 && passF.getLength() > 0){
            Network.sendObject(new AuthRequest(logF.getText(), passF.getText()));
            logF.clear();
            passF.clear();
        }
    }

    public void register(){
        if (logF.getLength() > 0 && passF.getLength() > 0){
            Network.sendObject(new RegRequest(logF.getText(), passF.getText()));
        }
    }

    public void textInGUI(String text){
        Platform.runLater(() -> {
            hint.setText(text);
        });
    }

    public void switchSceneToBox() {
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            root = (Pane) loader.load();
            Scene scene = new Scene(root, 500, 400);
            ((Stage) mainVBox.getScene().getWindow()).setScene(scene);

        } catch (IOException e){
            e.printStackTrace();
        }
    }


}
