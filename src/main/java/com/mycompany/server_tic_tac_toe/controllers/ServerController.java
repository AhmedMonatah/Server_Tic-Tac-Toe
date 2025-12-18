package com.mycompany.server_tic_tac_toe.controllers;

import com.mycompany.server_tic_tac_toe.App;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class ServerController {

    @FXML
    private Text num_Online;
    @FXML
    private Text num_Offline;
    @FXML
    private Text total_num;
    @FXML
    private Button stop_button;
    @FXML
    private Button start_button;
    @FXML
    private BarChart<?, ?> barChart;

    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }

    @FXML
    private void stop_button_func(ActionEvent event) {
    }

    @FXML
    private void start_button_func(ActionEvent event) {
    }

    @FXML
    private void barChart(MouseEvent event) {
    }
}
