package com.geekbrains.server;

import java.sql.*;

public class SQLHandler {
    private static Connection connection;
    private static Statement stmt;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:cloudbox.db");
            stmt = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean tryToLogIn(String login, String password) {
        try {
            ResultSet rs = stmt.executeQuery("SELECT login FROM users WHERE login = '" + login + "' AND password = '" + password + "';");
            if(rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean tryToRegister(String login, String password) {
        try {
            stmt.executeUpdate(String.format("INSERT INTO users (login, password) VALUES ('%s', '%s');", login, password));
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
