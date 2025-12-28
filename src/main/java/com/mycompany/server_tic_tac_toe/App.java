package com.mycompany.server_tic_tac_toe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import static javafx.application.Application.launch;
import javafx.scene.image.Image;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("server_ui"), 900, 600);
        stage.getIcons().add(new Image(App.class.getResourceAsStream("/images/server.png")));
        stage.setTitle("Server Tic-Tac-Toe");
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
            App.class.getResource(
                "/com/mycompany/server_tic_tac_toe/" + fxml + ".fxml"
            )
        );
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
