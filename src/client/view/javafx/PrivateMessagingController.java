package client.view.javafx;

import client.controller.Feature;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class PrivateMessagingController extends AbstractFXMLController {
  private String receiver;
  private String sender;
  private Stage window;
  private Scene scene;



  public void initialize(String receiver, String sender, Feature features, Stage window, Scene scene) {
    this.receiver = receiver;
    this.sender = sender;
    this.preface = "/privatemsg " + sender + ": " + receiver + ": ";
    this.features = features;
    this.window = window;
    this.scene = scene;

    super.initController();
  }

  @FXML
  private void openFileExplorer() {
    Platform.runLater(() -> {
      FileChooser dialog = new FileChooser();
      dialog.setTitle("Select a file to upload.");
      File selected = dialog.showOpenDialog(window);
      if (!(selected == null)) {
        if (selected.length() > 25000000) {

        } else {
          //appendChatLog("The file size cannot exceed 25mb.", "orange", false, "MESSAGEHELP");
        }
      }
    });
  }

  public String getReceiver() {
    return receiver;
  }

  public Stage getWindow() {
    return window;
  }

  public Scene getScene() {
    return scene;
  }
}
