/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */

package com.mycompany.server_tic_tac_toe.controllers;
//import com.mycompany.server_tic_tac_toe.ClientHandler;




import com.mycompany.server_tic_tac_toe.ServerClass;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author LENOVO
 */
public class Server_uiController implements Initializable {

    @FXML
    private Button stopServerButton;
    @FXML
    private Text numOfOnline;
    @FXML
    private Text numOfOffline;
    @FXML
    private Text totalNum;
    @FXML
    private BarChart<?, ?> barChartGraph;
    @FXML
    private Button startServerButton;
    ServerClass server;
   

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        stopServerButton.setDisable(true);
        //statusLabel.setText("Server stopped");
       
        
    }    

    @FXML
    private void startServer(ActionEvent event) {
        
          if (server == null) {
              server = new ServerClass();
           }
        server.startServerFunc();
        startServerButton.setDisable(true);
        stopServerButton.setDisable(false);
        
    }

    @FXML
    private void stopServer(ActionEvent event) {
          if (server != null) {
            server.stopServerFunc();
         }
          startServerButton.setDisable(false);
          stopServerButton.setDisable(true);
    }

    @FXML
    private void barChartFunc(MouseEvent event) {
    }
    
}
