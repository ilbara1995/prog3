package Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MailClientMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {

            URL clientUrl = getClass().getResource("/fxml/client.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(clientUrl);
            Scene scene = new Scene(fxmlLoader.load(), 900, 600);
            stage.setTitle("Email Client");
            stage.setScene(scene);
            stage.show();
            stage.setOnCloseRequest(windowEvent -> stage.close());
    }

    public static void main(String[] args) {
        launch();
    }
}