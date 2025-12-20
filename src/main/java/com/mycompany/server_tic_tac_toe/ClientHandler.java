package com.mycompany.server_tic_tac_toe;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import org.json.JSONObject;
import org.apache.derby.jdbc.ClientDriver;

class ClientHandler extends Thread {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private static Connection con;

    public ClientHandler(Socket clientSocket) {
        this.socket = clientSocket;
    }

    static {
        try {
            DriverManager.registerDriver(new ClientDriver());
            con = DriverManager.getConnection("jdbc:derby://localhost:1527/Users", "root", "root");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

public void run() {
    try {
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        String jsonReq;
        while ((jsonReq = reader.readLine()) != null) {
            System.out.println("Received: " + jsonReq);

            String response = processJsonRequest(jsonReq);
            writer.write(response);
            writer.newLine();
            writer.flush();
        }

    } catch (IOException ex) {
        ex.printStackTrace();
    }
}


    private String processJsonRequest(String jsonReq) throws IOException {
        JSONObject json = new JSONObject(jsonReq);
        String action = json.getString("action");
        String username = json.getString("username");
        String password = json.getString("password");

        switch(action) {
            case "login": return handleLogin(username, password);
            case "register": return handleRegister(username, password);
            default: return createErrorResponse("Unknown action", action);
        }
    }

private String handleRegister(String username, String password) throws IOException {
    try {
        String query = "SELECT * FROM USERS WHERE NAME=?";
        PreparedStatement pst = con.prepareStatement(query);
        pst.setString(1, username);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return createErrorResponse("User already exists", "register");
        }

        String insertQuery = "INSERT INTO USERS(NAME,PASSWORD) VALUES(?,?)";
        PreparedStatement insertStmt = con.prepareStatement(insertQuery);
        insertStmt.setString(1, username);
        insertStmt.setString(2, password);
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

    } catch (SQLException ex) {
        return createErrorResponse(ex.getMessage(), "register");
    }
}


    private String handleLogin(String username, String password) {
        try {
            String query = "SELECT * FROM USERS WHERE NAME=? AND PASSWORD=?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                JSONObject response = new JSONObject();
                response.put("action", "login_response");
                response.put("success", true);
                response.put("message", "Welcome " + username);
                return response.toString();
            } else {
                return createErrorResponse("Invalid username or password", "login");
            }

        } catch (SQLException ex) {
            return createErrorResponse(ex.getMessage(), "login");
        }
    }

    private String createErrorResponse(String errorMessage, String action) {
        JSONObject response = new JSONObject();
        if ("login".equals(action)) {
            response.put("action", "login_response");
        } else {
            response.put("action", "register_response");
        }
        response.put("success", false);
        response.put("message", errorMessage);
        return response.toString();
    }
}
