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
import org.json.JSONObject;

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
        connectDB(); // Initial connection
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
            System.out.println("Database connected successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        // Ensure connection is valid before returning
        try {
            if (con == null || con.isClosed()) {
                connectDB();
            }
        } catch (SQLException e) {
            connectDB();
        }
        return con;
    }


    public void startServerFunc() {
        // Fix: Ensure DB is connected when starting server (in case it was closed on stop)
        try {
            if (con == null || con.isClosed()) {
                connectDB();
            }
        } catch (SQLException e) {
            connectDB();
        }

        isRunning = true;

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(5001);
                System.out.println("Server started on port 5001");
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
        
        JSONObject json = new JSONObject();
        json.put("action", "server_stopped");
        String msg = json.toString();

        for (ClientHandler ch : onlineUsers.values()) {
            if (ch.isConnected()) {
                ch.sendMessage(msg);
            }
            ch.closeConnection(); 
        }
        onlineUsers.clear();

        try {
            if (serverSocket != null) serverSocket.close();
            if (con != null) con.close(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Server stopped.");
    }
}
