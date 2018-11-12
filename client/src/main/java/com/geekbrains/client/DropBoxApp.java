package com.geekbrains.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class DropBoxApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/auth.fxml"));
        primaryStage.setTitle("GeekDropBox");
        primaryStage.getIcons().add(new Image("/dropbox.jpg"));
        primaryStage.setScene(new Scene(root, 250, 150));
        primaryStage.show();
    }


    public static void main(String[] args) throws IOException {launch(args);}
}


