/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.server_tic_tac_toe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.derby.jdbc.ClientDriver;
import org.json.JSONObject;
import com.google.gson.Gson; 
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 *
 * @author LENOVO
 */
class ClientHandler extends Thread {

    private Socket socket;
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
            con = DriverManager.getConnection("jdbc:derby://localhost:1527/UserDetails", "root", "root");
        } catch (SQLException ex) {
            System.getLogger(DOA.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
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
    }

    private String processJsonRequest(String jsonReq) {
        JSONObject json = new JSONObject(jsonReq);
        if (!json.has("username") || !json.has("password")) {

        }
        String username = json.getString("username");
        String password = json.getString("password");
        if (json.getString("action").equals("login")) {
            return handleLogin(username, password);
        }
        if (json.getString("action").equals("register")) {
            return handleRegister(username, password);
        }
        return "sds";

    }

    private String handleRegister(String username, String password)  {
        try {
            String query = "SELECT * FROM USERS WHERE NAME=?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return createErrorResponse("User already sexists","register");
            }
            String sQuery = "insert into USERS(USERNAME,PASSWORD) values(?,?) ";
            PreparedStatement insertStmt = con.prepareStatement(sQuery);
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            int r = insertStmt.executeUpdate();
            if (r > 0) {
                JSONObject response = new JSONObject();
                response.put("action", "register_response");
                response.put("success", true);
                response.put("message", "Registered successfully");
                return response.toString();
                
            }else {
                return createErrorResponse("Registration failed","register");
        }
           
        } catch (SQLException ex) {
            return createErrorResponse(ex.getMessage(),"register");
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
                response.put("message", "Welcome "+username);
                return response.toString();
                
            } else {
                return createErrorResponse("Invalid username or password","login");
            }
        } catch (SQLException ex) {
            return createErrorResponse(ex.getMessage(),"login");
        }

    }

    /*private String createSuccessResponse(String message) {
        JSONObject response = new JSONObject();
        response.put("action", "login_response");
        response.put("success", true);
        response.put("message", message);
        return response.toString();
    }*/

    private String createErrorResponse(String errorMessage,String action) {
        JSONObject response = new JSONObject();
        if(action.equals("login")){
            response.put("action", "login_response");
            response.put("success", false);
            response.put("message", errorMessage);
            return response.toString();
        }else{
            response.put("action", "register_response");
            response.put("success", false);
            response.put("message", errorMessage);
            return response.toString();
        }
       
    }

}
/*
{
  "action": "login",
  "username": "Ahmed",
  "password": "1234"
}  
{
  "action": "register",
  "username": "Omar",
  "password": "abcd"
}
*/
