package client.view.javafx;

import client.controller.Feature;
import java.awt.TextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;


public class FXMLController {
  private Feature features;
  private Scene root;
  private String name;

  @FXML
  private TextArea chatField;
  @FXML
  private TextFlow chatLog;

  public void setFeatures(Feature features) {
    this.features = features;
    System.out.print("features:  " + this.features);
  }

  public void onEnter(KeyEvent ke) {
    if(ke.getCode() == KeyCode.ENTER) {
      ke.consume();
      String text = chatField.getText().substring(0, chatField.getText().length() - 1);
      features.sendTextOut(text);
      chatField.setText("");
    }
  }

  public void appendChatLog(String s, String color, boolean hasDate) {
//    chatLog.appendText(s + "\n");
    Text msg = new Text(s + "\n");
    msg.setFill(getColor(color));
    Platform.runLater(() -> chatLog.getChildren().add(msg));
  }

  private Color getColor(String color) {
    switch(color) {
      case "blue":
        return Color.BLUE;
      case "red":
        return Color.RED;
      case "green":
        return Color.GREEN;
      case "orange":
        return Color.ORANGE;
      default:
        return Color.BLACK;
    }
  }
}