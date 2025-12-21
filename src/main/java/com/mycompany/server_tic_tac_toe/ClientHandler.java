package com.mycompany.server_tic_tac_toe;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.gson.Gson; 
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ClientHandler extends Thread {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private static Connection con;
    public static Map<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();
    private String username;

    public ClientHandler(Socket clientSocket) {
        this.socket = clientSocket;
    private BufferedReader  input;
    private BufferedWriter  output;
    private static Connection con;
    private Gson gson;

    public ClientHandler(Socket clientSocket) {
        this.socket = clientSocket;
        this.gson = new Gson();

    }
    static {
        try {
            DriverManager.registerDriver(new ClientDriver());
            con = DriverManager.getConnection("jdbc:derby://localhost:1527/Users", "root", "root");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

@Override
public void run() {
    try {
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        String jsonReq;
        while ((jsonReq = reader.readLine()) != null) {
            System.out.println("Received: " + jsonReq);
            String response = processJsonRequest(jsonReq);
            if (response != null) {
                sendMessage(response);
            }
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            while (true) {
                String jsonReq = (String) input.readLine();
                System.out.println("Received: " + jsonReq);

                String response = processJsonRequest(jsonReq);
                output.write(response);
                output.newLine();
                output.flush();
            }
        } catch (IOException ex) {
            System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    } catch (IOException ex) {
        System.err.println("Connection lost with user: " + username);
    } finally {
        handleUserDisconnection();
    }
}

private void handleUserDisconnection() {
    if (username != null) {
        onlineUsers.remove(username);
        System.out.println(username + " has disconnected.");
        broadcastUsersList();
    }
    cleanup();
}
    private String processJsonRequest(String jsonReq) {
        try {
            JSONObject json = new JSONObject(jsonReq);
            String action = json.getString("action");

            switch (action) {
                case "login":
                    return handleLogin(json);
                case "register":
                    return handleRegister(json);
                case "get_users":
                    return handleGetUsers();
                case "game_request":
                    return handleGameRequest(json);
                case "logout":
                    handleLogout(json);
                    return null;
                default:
                    return createErrorResponse("Unknown action", action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("Invalid JSON format", "error");
        }
    }

        private void handleLogout(JSONObject json) {
            String user = json.optString("username");
            if (user != null && !user.isEmpty()) {
                System.out.println(user + " logged out.");

                onlineUsers.remove(user);

                broadcastUsersList();

                if (username != null && username.equals(user)) {
                    cleanup();
                }
            }
        }
        private void cleanup() {
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    private String handleLogin(JSONObject json) {
        try {
            String user = json.getString("username");
            String pass = json.getString("password");

            String query = "SELECT * FROM USERS WHERE NAME=? AND PASSWORD=?";
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setString(1, user);
                pst.setString(2, pass);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {

                        if (onlineUsers.containsKey(user)) {
                            ClientHandler oldClient = onlineUsers.get(user);
                            oldClient.disconnect();
                            onlineUsers.remove(user);
                        }

                        this.username = user;
                        onlineUsers.put(user, this);

                        JSONObject response = new JSONObject();
                        response.put("action", "login_response");
                        response.put("success", true);
                        response.put("message", "Welcome " + user);
                        broadcastUsersList();
                        return response.toString();
                    } else {
                        return createErrorResponse("Invalid username or password", "login");
                    }
                }
            }
        } catch (SQLException ex) {
            return createErrorResponse(ex.getMessage(), "login");
        }
    }

    public void disconnect() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastUsersList() {
        JSONArray usersArray;
        for (Map.Entry<String, ClientHandler> entry : onlineUsers.entrySet()) {
            ClientHandler ch = entry.getValue();
            if (!ch.isConnected()) continue;

            usersArray = new JSONArray();
            for (String user : onlineUsers.keySet()) {
                if (!user.equals(ch.username)) usersArray.put(user);
            }

            JSONObject update = new JSONObject();
            update.put("action", "users_list");
            update.put("users", usersArray);

            ch.sendMessage(update.toString());
        }
    }



public boolean isConnected() {
    return socket != null && !socket.isClosed() && socket.isConnected();
}

    private String handleRegister(JSONObject json) {
        try {
            String user = json.getString("username");
            String pass = json.getString("password");

            String checkQuery = "SELECT * FROM USERS WHERE NAME=?";
            try (PreparedStatement pst = con.prepareStatement(checkQuery)) {
                pst.setString(1, user);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return createErrorResponse("User already exists", "register");
                    }
                }
            }

            String insertQuery = "INSERT INTO USERS(NAME,PASSWORD) VALUES(?,?)";
            try (PreparedStatement insertStmt = con.prepareStatement(insertQuery)) {
                insertStmt.setString(1, user);
                insertStmt.setString(2, pass);
                int r = insertStmt.executeUpdate();
                if (r > 0) {
                    JSONObject response = new JSONObject();
                    response.put("action", "register_response");
                    response.put("success", true);
                    response.put("message", "Registered successfully");
                    return response.toString();
                } else {
                    return createErrorResponse("Registration failed", "register");
                }
            }

        } catch (SQLException ex) {
            return createErrorResponse(ex.getMessage(), "register");
        }
    }

    private String handleGetUsers() {
        try {
            JSONArray usersArray = new JSONArray();

            for (String user : onlineUsers.keySet()) {
                if (!user.equals(this.username)) {
                    usersArray.put(user);
                }
            }

            JSONObject response = new JSONObject();
            response.put("action", "users_list");
            response.put("users", usersArray);
            return response.toString();

        } catch (Exception ex) {
            return createErrorResponse(ex.getMessage(), "get_users");
        }
    }

    private String handleGameRequest(JSONObject json) {
        try {
            String fromUser = json.getString("from");
            String toUser = json.getString("to");

            System.out.println("Game request from " + fromUser + " to " + toUser);

            ClientHandler target = onlineUsers.get(toUser);
            if (target != null) {
                JSONObject response = new JSONObject();
                response.put("action", "game_request");
                response.put("from", fromUser);
                target.sendMessage(response.toString());
            }

            JSONObject ack = new JSONObject();
            ack.put("action", "game_request_response");
            ack.put("success", true);
            ack.put("message", "Request sent to " + toUser);
            return ack.toString();
        } catch (Exception ex) {
            return createErrorResponse(ex.getMessage(), "game_request");
        }
    }

    private String createErrorResponse(String errorMessage, String action) {
        JSONObject response = new JSONObject();
        response.put("action", action + "_response");
        response.put("success", false);
        response.put("message", errorMessage);
        return response.toString();
    }

    private void sendMessage(String message) {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException ex) {
            System.err.println("Failed to send message to " + username);
        }
    }
}
