/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.server_tic_tac_toe;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.derby.jdbc.ClientDriver;

/**
 *
 * @author LENOVO
 */
public class ServerClass {

    private ServerSocket serverSocket;
    private boolean isRunning;

    private final Map<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();
    private Connection con;

    public ServerClass() {
        connectDB();
    }
    public Map<String, ClientHandler> getOnlineUsers() {
        return onlineUsers;
    }
    private void connectDB() {
        try {
            DriverManager.registerDriver(new ClientDriver());
            con = DriverManager.getConnection(
                    "jdbc:derby://localhost:1527/Users",
                    "root",
                    "root"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return con;
    }


    public void startServerFunc() {
        isRunning = true;

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(5001);
                while (isRunning) {
                    Socket socket = serverSocket.accept();
                    new ClientHandler(socket, this).start();
                }
            } catch (IOException e) {
                if (isRunning) e.printStackTrace();
            }
        }).start();
    }

    public void stopServerFunc() {
        isRunning = false;

        for (ClientHandler ch : onlineUsers.values()) {
            ch.closeConnection();
        }
        onlineUsers.clear();

        try {
            if (serverSocket != null) serverSocket.close();
            if (con != null) con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
