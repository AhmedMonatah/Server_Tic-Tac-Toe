package com.mycompany.server_tic_tac_toe;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class ClientHandler extends Thread {

    private final Socket socket;
    private final ServerClass server;
    private final Connection con;

    private BufferedReader reader;
    private BufferedWriter writer;
    private String username;

    public ClientHandler(Socket socket, ServerClass server) {
        this.socket = socket;
        this.server = server;
        this.con = server.getConnection();
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
            while (!socket.isClosed() && (jsonReq = reader.readLine()) != null) {
                System.out.println("Server Received: " + jsonReq);

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
                case "request_response":
                    return handleRequestResonse(json);
                case "logout":
                    handleLogout(json);
                    return null;
                case "game_move":
                    return handleGameMove(json);
                case "new_round_request":
                case "new_round_accept":
                case "new_round_decline":
                    return handleForwarding(json);
                case "player_left":
                    // Fix: Use dedicated handler for player_left to update status
                    return handlePlayerLeft(json);
                case "new_round":
                    return handleNewRound(json);
                default:
                    return createErrorResponse("Unknown action", action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("Invalid JSON format", "error");
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

                       if (server.getOnlineUsers().containsKey(user)) {
                           return createErrorResponse("This account is already logged in", "login");
                       }

                       this.username = user;
                       server.getOnlineUsers().put(user, this);
                       
                       // Ensure user is marked as available on login
                       updateAvailability(user, true);

                       broadcastUsersList();

                       JSONObject res = new JSONObject();
                       res.put("action", "login_response");
                       res.put("success", true);
                       res.put("username", user);
                       return res.toString();
                   }
               }
           }
           return createErrorResponse("Invalid username or password", "login");

       } catch (SQLException ex) {
           return createErrorResponse(ex.getMessage(), "login");
       }
   }


    private String handleRegister(JSONObject json) {
        try {
            String user = json.getString("username");
            String email = json.getString("email"); 
            String pass = json.getString("password");

            String check = "SELECT * FROM USERS WHERE NAME=? OR EMAIL=?";
            try (PreparedStatement pst = con.prepareStatement(check)) {
                pst.setString(1, user);
                pst.setString(2, email);
                if (pst.executeQuery().next()) {
                    return createErrorResponse("This username or email is already used", "register");
                }
            }

            String insert = "INSERT INTO USERS(NAME, EMAIL, PASSWORD) VALUES(?,?,?)";
            try (PreparedStatement pst = con.prepareStatement(insert)) {
                pst.setString(1, user);
                pst.setString(2, email);
                pst.setString(3, pass);
                pst.executeUpdate();
            }

            JSONObject res = new JSONObject();
            res.put("action", "register_response");
            res.put("success", true);
            res.put("username", user);
            return res.toString();

        } catch (SQLException ex) {
            return createErrorResponse(ex.getMessage(), "register");
        }
    }


    private String handleGetUsers() {
        JSONArray arr = new JSONArray();

        String sql = "SELECT NAME, IS_AVAILABLE FROM USERS";

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("NAME");

                if (!server.getOnlineUsers().containsKey(name)) continue;
                if (name.equals(username)) continue;

                JSONObject userObj = new JSONObject();
                userObj.put("username", name);
                userObj.put("is_available", rs.getInt("IS_AVAILABLE") == 1);

                arr.put(userObj);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        JSONObject res = new JSONObject();
        res.put("action", "users_list");
        res.put("users", arr);
        return res.toString();
    }


    public void broadcastUsersList() {
        JSONArray arr = new JSONArray();

        // Use a snapshot of keys to avoid concurrent modification issues if possible
        for (String user : server.getOnlineUsers().keySet()) {

            boolean isAvailable = true;
            try (PreparedStatement pst = con.prepareStatement("SELECT IS_AVAILABLE FROM USERS WHERE NAME=?")) {
                pst.setString(1, user);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    isAvailable = rs.getInt("IS_AVAILABLE") == 1;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            JSONObject userObj = new JSONObject();
            userObj.put("username", user);
            userObj.put("is_available", isAvailable);

            arr.put(userObj);
        }

        for (ClientHandler ch : server.getOnlineUsers().values()) {
            if (!ch.isConnected()) continue;
            JSONArray filteredArr = new JSONArray();
            // Send list of *other* users
            for (int i = 0; i < arr.length(); i++) {
                JSONObject u = arr.getJSONObject(i);
                if (!u.getString("username").equals(ch.username)) {
                    filteredArr.put(u);
                }
            }
            JSONObject update = new JSONObject();
            update.put("action", "users_list");
            update.put("users", filteredArr);
            ch.sendMessage(update.toString());
        }
    }



    private String handleGameRequest(JSONObject json) {
        String from = json.getString("from");
        String to = json.getString("to");

        String check = "SELECT IS_AVAILABLE FROM USERS WHERE NAME=?";

        try (PreparedStatement pst = con.prepareStatement(check)) {
            pst.setString(1, to);
            ResultSet rs = pst.executeQuery();

            if (rs.next() && rs.getInt("IS_AVAILABLE") == 0) {
                return createErrorResponse("User is busy", "game_request");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ClientHandler target = server.getOnlineUsers().get(to);
        if (target != null) {
            JSONObject req = new JSONObject();
            req.put("action", "show_game_request");
            req.put("from", from);
            req.put("to", to);
            target.sendMessage(req.toString());
        }

        return new JSONObject()
                .put("action", "game_request_response")
                .put("success", true)
                .toString();
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

    public void sendMessage(String msg) {
        try {
            writer.write(msg);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to send message to " + username);
        }
    }
    
    private String handleGameMove(JSONObject json) {
        String to = json.getString("to");
        ClientHandler target = server.getOnlineUsers().get(to);

        if (target != null) {
            target.sendMessage(json.toString()); 
            System.out.println("Forwarding move to: " + to);
        }

        JSONObject ack = new JSONObject();
        ack.put("action", "game_move_response");
        ack.put("success", true);
        return ack.toString();
    }
    
    private String handleNewRound(JSONObject json) {
        String to = json.getString("to");
        ClientHandler target = server.getOnlineUsers().get(to);

        if (target != null) {
            target.sendMessage(json.toString()); 
            System.out.println("Forwarding New Round request to: " + to);
        }

        JSONObject ack = new JSONObject();
        ack.put("action", "new_round_response");
        ack.put("success", true);
        return ack.toString();
    }
    
    private String handleForwarding(JSONObject json) {
        String to = json.getString("to");
        ClientHandler target = server.getOnlineUsers().get(to);
        if (target != null) {
            target.sendMessage(json.toString());
        }
        return new JSONObject().put("success", true).toString();
    }
    
    private String handlePlayerLeft(JSONObject json) {
        String to = json.optString("to"); // Opponent name

        if (this.username != null) {
            updateAvailability(this.username, true);
        }

        if (to != null && !to.isEmpty()) {
            updateAvailability(to, true);
            
            // 3. Notify opponent
            ClientHandler target = server.getOnlineUsers().get(to);
            if (target != null) {
                target.sendMessage(json.toString());
            }
        }

        broadcastUsersList();

        return new JSONObject()
                .put("action", "player_left_response")
                .put("success", true)
                .toString();
    }
    
    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            interrupt(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateAvailability(String user, boolean available) {
        try (PreparedStatement pst = con.prepareStatement(
                "UPDATE USERS SET IS_AVAILABLE=? WHERE NAME=?")) {
            pst.setInt(1, available ? 1 : 0);
            pst.setString(2, user);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void handleLogout(JSONObject json) {
        String user = json.optString("username");
        server.getOnlineUsers().remove(user);
        updateAvailability(user, true); 
        broadcastUsersList(); 
        
        cleanup();
    }

    private void handleUserDisconnection() {
        if (username != null) {
            server.getOnlineUsers().remove(username);
            updateAvailability(username, true); // Reset status 
            broadcastUsersList();
        }
        cleanup();
    }

    private String handleRequestResonse(JSONObject json) {
        String from = json.getString("from");
        String to = json.getString("to");
        String response = json.getString("response");
        ClientHandler target = server.getOnlineUsers().get(to);

        if (target != null) {
            JSONObject req = new JSONObject();
            req.put("action", "request_response");
            req.put("to", to);
            req.put("from", from);
            req.put("response", response);
            target.sendMessage(req.toString());
        }

        if ("accept".equals(response)) {
            updateAvailability(from, false);
            updateAvailability(to, false);
            broadcastUsersList();
        }

        JSONObject ack = new JSONObject();
        ack.put("action", "game_request_response");
        ack.put("success", true);
        return ack.toString();
    }

}
