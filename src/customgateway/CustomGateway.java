/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package customgateway;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * @author veckardt
 */
public class CustomGateway extends Application {
  
    public static Stage stage;
    public static String title = Copyright.programName + " - v" + Copyright.programVersion;

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("CustomGateway.fxml"));

        Scene scene = new Scene(root);
        CustomGateway.stage = stage;
        Image applicationIcon = new Image(getClass().getResourceAsStream("resources/word-icon.png"));
        stage.getIcons().add(applicationIcon);
        
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }    
     
}
