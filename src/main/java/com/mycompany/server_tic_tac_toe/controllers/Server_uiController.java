/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */

package com.mycompany.server_tic_tac_toe.controllers;
import com.mycompany.server_tic_tac_toe.DOA;
import com.mycompany.server_tic_tac_toe.ServerClass;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.util.Duration;

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
    private BarChart<String, Number> barChartGraph;
    @FXML
    private Button startServerButton;

    private ServerClass server;
    private javafx.animation.Timeline timeline;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        stopServerButton.setDisable(true);
        
        Series<String, Number> series = new Series<>();
        series.setName("Players");
        series.getData().add(new Data<>("Online", 0));
        series.getData().add(new Data<>("Offline", 0));
        barChartGraph.getData().add(series);

        // Setup Timeline for periodic updates (every 2 seconds)
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(2), event -> updateDashboard()));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    @FXML
    private void startServer(ActionEvent event) {

        if (server == null) {
            server = new ServerClass();
        }
        server.startServerFunc();
        startMonitoring();

        startServerButton.setDisable(true);
        stopServerButton.setDisable(false);

    }

    @FXML
    private void stopServer(ActionEvent event) {
        if (server != null) {
            server.stopServerFunc();
        }
        stopMonitoring();

        startServerButton.setDisable(false);
        stopServerButton.setDisable(true);
    }

    @FXML
    private void barChartFunc(MouseEvent event) {
    }

    private void startMonitoring() {
        if (timeline != null) {
            timeline.play();
        }
    }

    private void stopMonitoring() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    private void updateDashboard() {

        if (server == null) return;

        int onlineCount = server.getOnlineUsers().size();
        int totalCount = DOA.getTotalPlayers();
        int offlineCount = totalCount - onlineCount;

        if (offlineCount < 0) offlineCount = 0;

        numOfOnline.setText(String.valueOf(onlineCount));
        numOfOffline.setText(String.valueOf(offlineCount));
        totalNum.setText(String.valueOf(totalCount));

        if (!barChartGraph.getData().isEmpty()) {
            Series<String, Number> series = barChartGraph.getData().get(0);
            for (Data<String, Number> data : series.getData()) {
                if ("Online".equals(data.getXValue())) {
                    data.setYValue(onlineCount);
                    data.getNode().setStyle("-fx-bar-fill: green;");
                } else if ("Offline".equals(data.getXValue())) {
                    data.setYValue(offlineCount);
                    data.getNode().setStyle("-fx-bar-fill: red;");
                }
            }
        }
    }

}
