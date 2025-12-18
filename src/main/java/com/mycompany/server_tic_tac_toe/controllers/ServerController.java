package com.mycompany.server_tic_tac_toe.controllers;

import com.mycompany.server_tic_tac_toe.App;
import java.io.IOException;
import javafx.fxml.FXML;

public class ServerController {

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}
