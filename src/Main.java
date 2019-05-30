import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;
import ru.roboticsnt.visitorCounter.CameraController;
import ru.roboticsnt.visitorCounter.Counter;
import ru.roboticsnt.visitorCounter.gui.GUIController;


public class Main extends Application
{


    private final String _APP_NAME = "Visitors counter";
    private final String _APP_VERSION = "0.1";


    @Override
    public void start(Stage primaryStage) throws Exception
    {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("ru/roboticsnt/visitorCounter/gui/sample.fxml"));

        Parent root = loader.load();

        GUIController guiController = loader.getController();

        CameraController cameraController = new CameraController();
        Counter counter = new Counter();

        guiController.init(counter, cameraController);

        cameraController.setOnFrameListener((frame, detectionResult) ->
        {
            counter.update(detectionResult);
            guiController.update(frame);
        });

        primaryStage.setOnCloseRequest(windowEvent ->
        {
            System.out.println("CLOSE APPLICATION...");
            cameraController.stop();
        });

        primaryStage.setTitle(_APP_NAME + " | " + _APP_VERSION);
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();

    }


    public static void main(String[] args)
    {
        launch(args);
    }

}
