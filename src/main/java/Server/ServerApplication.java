package Server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ServerApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        URL serverUrl = getClass().getResource("/fxml/log.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(serverUrl);
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        stage.setTitle("ServerLog");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
