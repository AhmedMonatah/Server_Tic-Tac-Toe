/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.server_tic_tac_toe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;

/**
 *
 * @author LENOVO
 */
public class ServerClass {

    ServerSocket serversocket;
    private boolean isRunning = false;
    private Thread serverThread;

    public static Map<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();
    public ServerClass() {

    }

    public void startServerFunc() {
        //Socket socket;
        serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    serversocket = new ServerSocket(5005);
                    System.out.println("Server started on port 5005");
                    while(true){
                       Socket socket=serversocket.accept();
                        new ClientHandler(socket).start();
                        
                    }
                } catch (IOException ex) {
                    System.getLogger(ServerClass.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }

            }
        });
        serverThread.start();

        
    }

}
