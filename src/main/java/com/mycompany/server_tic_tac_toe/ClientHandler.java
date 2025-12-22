package com.mycompany.server_tic_tac_toe;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.derby.jdbc.ClientDriver;
import org.json.JSONArray;
import org.json.JSONObject;

public class ClientHandler extends Thread {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private static Connection con;

    public static Map<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();
    private String username;

    // ===== Constructor =====
    public ClientHandler(Socket clientSocket) {
        this.socket = clientSocket;
    }

    // ===== Database connection =====
    static {
        try {
            DriverManager.registerDriver(new ClientDriver());
            con = DriverManager.getConnection(
                    "jdbc:derby://localhost:1527/Users",
                    "root",
                    "root"
            );
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ===== Thread run =====
    @Override
    public void run() {
        try {
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
            );
            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)
            );

            String jsonReq;
            while ((jsonReq = reader.readLine()) != null) {
                System.out.println("Received: " + jsonReq);

                String response = processJsonRequest(jsonReq);
                if (response != null) {
                    sendMessage(response);
                }
            }
        } catch (IOException ex) {
            System.err.println("Connection lost with user: " + username);
        } finally {
            handleUserDisconnection();
        }
    }

    // ===== Handle requests =====
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

    // ===== Login =====
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
                            onlineUsers.get(user).disconnect();
                        }

                        this.username = user;
                        onlineUsers.put(user, this);

                        broadcastUsersList();

                        JSONObject res = new JSONObject();
                        res.put("action", "login_response");
                        res.put("success", true);
                        res.put("message", "Welcome " + user);
                        return res.toString();
                    }
                }
            }
            return createErrorResponse("Invalid username or password", "login");

        } catch (SQLException ex) {
            return createErrorResponse(ex.getMessage(), "login");
        }
    }

    // ===== Register =====
    private String handleRegister(JSONObject json) {
        try {
            String user = json.getString("username");
            String pass = json.getString("password");

            String check = "SELECT * FROM USERS WHERE NAME=?";
            try (PreparedStatement pst = con.prepareStatement(check)) {
                pst.setString(1, user);
                if (pst.executeQuery().next()) {
                    return createErrorResponse("User already exists", "register");
                }
            }

            String insert = "INSERT INTO USERS(NAME,PASSWORD) VALUES(?,?)";
            try (PreparedStatement pst = con.prepareStatement(insert)) {
                pst.setString(1, user);
                pst.setString(2, pass);
                pst.executeUpdate();
            }

            JSONObject res = new JSONObject();
            res.put("action", "register_response");
            res.put("success", true);
            res.put("message", "Registered successfully");
            return res.toString();

        } catch (SQLException ex) {
            return createErrorResponse(ex.getMessage(), "register");
        }
    }

    // ===== Users list =====
    private String handleGetUsers() {
        JSONArray arr = new JSONArray();
        for (String user : onlineUsers.keySet()) {
            if (!user.equals(username)) arr.put(user);
        }

        JSONObject res = new JSONObject();
        res.put("action", "users_list");
        res.put("users", arr);
        return res.toString();
    }

    private void broadcastUsersList() {
        for (ClientHandler ch : onlineUsers.values()) {
            if (!ch.isConnected()) continue;

            JSONArray arr = new JSONArray();
            for (String user : onlineUsers.keySet()) {
                if (!user.equals(ch.username)) arr.put(user);
            }

            JSONObject update = new JSONObject();
            update.put("action", "users_list");
            update.put("users", arr);

            ch.sendMessage(update.toString());
        }
    }

    // ===== Game request =====
    private String handleGameRequest(JSONObject json) {
        String from = json.getString("from");
        String to = json.getString("to");

        ClientHandler target = onlineUsers.get(to);
        if (target != null) {
            JSONObject req = new JSONObject();
            req.put("action", "game_request");
            req.put("from", from);
            target.sendMessage(req.toString());
        }

        JSONObject ack = new JSONObject();
        ack.put("action", "game_request_response");
        ack.put("success", true);
        return ack.toString();
    }

    // ===== Logout & cleanup =====
    private void handleLogout(JSONObject json) {
        onlineUsers.remove(json.optString("username"));
        broadcastUsersList();
        cleanup();
    }

    private void handleUserDisconnection() {
        if (username != null) {
            onlineUsers.remove(username);
            broadcastUsersList();
        }
        cleanup();
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

    public void disconnect() {
        cleanup();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    private String createErrorResponse(String msg, String action) {
        JSONObject res = new JSONObject();
        res.put("action", action + "_response");
        res.put("success", false);
        res.put("message", msg);
        return res.toString();
    }

    private void sendMessage(String msg) {
        try {
            writer.write(msg);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to send message to " + username);
        }
    }
}
