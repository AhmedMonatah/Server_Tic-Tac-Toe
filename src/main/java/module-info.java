module com.mycompany.server_tic_tac_toe {
    requires javafx.controls;
    requires javafx.fxml;
    requires derbyclient;
    requires java.base;
    requires java.sql;
    requires org.json;
    
    
    exports com.mycompany.server_tic_tac_toe;
    opens com.mycompany.server_tic_tac_toe to javafx.fxml;
    exports com.mycompany.server_tic_tac_toe.controllers;
    opens com.mycompany.server_tic_tac_toe.controllers to javafx.fxml;
}
