package client.view.javafx;

import client.controller.Feature;
import client.view.MultiChatView;
import client.view.swing.MultiChatViewImpl;
import java.awt.TextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

  public void setTextFieldEditable(boolean b) {
    chatField.setEditable(b);
  }

  public void appendChatLog(String s, String color, boolean hasDate) {
    appendMessage(s, getColor(color));
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

  private void appendMessage(String msg, Color c) {

    // split message by space
    String[] words = msg.split(" ");

    for (String word : words) {
      // if the word equals an emoji name (ex. <3) then replace it with an ImageView of the emoji
      // if the word equals a twitch emoji (ex. PepeHands) then replace it with an ImageView of the twitch emoji
      // otherwise just add the word as plaintext
      if (MultiChatView.EMOTES.containsKey(word.trim())) {
        ImageView emoji = getEmote("/client/resources/images/emojis/" + MultiChatView.EMOTES.get(word.trim()), 20);
        Platform.runLater(() -> chatLog.getChildren().add(emoji));
      } else if (MultiChatView.TWITCH_EMOTES.containsKey(word.trim())) {
        ImageView twitchEmote = getEmote("/client/resources/images/twitch/" + MultiChatView.TWITCH_EMOTES.get(word.trim()), 40);
        Platform.runLater(() -> chatLog.getChildren().add(twitchEmote));
      } else {
        Text txt = new Text(word + " ");
        txt.setFill(c);
        Platform.runLater(() -> chatLog.getChildren().add(txt));
      }
    }

    Platform.runLater(() -> chatLog.getChildren().add(new Text("\n")));
  }

  private ImageView getEmote(String imagePath, int size) {
    ImageView imageView = new ImageView(
        new Image(getClass().getResource(imagePath).toExternalForm())
    );
    imageView.setFitHeight(size);
    imageView.setFitWidth(size);
    return imageView;
  }

}