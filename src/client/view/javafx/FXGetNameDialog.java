package client.view.javafx;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FXGetNameDialog {

  public static String answer;

  public static String display(String msg) {
    Stage window =  new Stage();
    window.setTitle("Name Selection");
    window.initModality(Modality.APPLICATION_MODAL);

    Label label = new Label(msg);

    TextField field = new TextField();
    field.setPromptText("name...");

    HBox buttonPanel = new HBox();
    Button submitButton = new Button("Submit");
    submitButton.setOnAction(e -> {
      answer = field.getText();
      window.close();
    });

    Button cancelButton = new Button("Cancel");
    buttonPanel.getChildren().addAll(submitButton, cancelButton);


    VBox layout = new VBox();
    layout.getChildren().addAll(label, field, buttonPanel);
    Scene scene = new Scene(layout, 500, 500);
    window.setScene(scene);
    window.showAndWait();

    return answer;
  }

}
