package client.view.javafx;

import client.controller.Feature;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class PrivateMessagingController extends AbstractFXMLController {
  private String receiver;
  private String sender;
  private Stage window;



  public void initialize(String receiver, String sender, Feature features, Stage window) {
    this.receiver = receiver;
    this.sender = sender;
    this.features = features;
    this.window = window;

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

  @FXML
  private void onEnter(KeyEvent ke) {
    super.onEnter(ke, "/privatemsg " + sender + ": " + receiver + ": ");
  }


  public String getReceiver() {
    return receiver;
  }

  public Stage getWindow() {
    return window;
  }
}
