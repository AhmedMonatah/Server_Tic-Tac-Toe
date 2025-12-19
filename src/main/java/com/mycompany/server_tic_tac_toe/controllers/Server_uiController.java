/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.server_tic_tac_toe.controllers;

import java.net.URL;
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
    private Button startServerButtom;
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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void startServer(ActionEvent event) {
    }

    @FXML
    private void stopServer(ActionEvent event) {
    }

    @FXML
    private void barChartFunc(MouseEvent event) {
    }
    
}
