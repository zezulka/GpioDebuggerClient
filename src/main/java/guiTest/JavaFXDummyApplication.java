package guiTest;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Miloslav Zezulka
 */
public class JavaFXDummyApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        String pathToFxml = "fxml/MasterWindow.fxml";
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource(pathToFxml));
        
        Scene scene = new Scene(root, 1000, 800);
        stage.setMinHeight(800);
        stage.setMinWidth(1000);
        stage.setTitle(pathToFxml);
        stage.setScene(scene);
        stage.show();
    }

}
