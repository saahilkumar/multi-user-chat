package client.view.javafx;

import client.controller.Feature;
import client.view.MultiChatView;
import client.view.swing.MultiChatViewImpl;
import java.awt.TextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;


public class FXMLController {
  private Feature features;
  private Scene root;
  private String name;
  private Scene scene;

  @FXML
  private TextArea chatField;
  @FXML
  private TextFlow chatLog;
//  @FXML
//  private CheckMenuItem darkModeMenuItem;

  public void setFeatures(Feature features) {
    this.features = features;
    System.out.print("features:  " + this.features);
  }

  public void setScene(Scene scene) {
    this.scene = scene;
  }

//  public void setDarkMode() {
//    if(darkModeMenuItem.isSelected()) {
//      darkModeMenuItem.setText("Disable Dark Mode");
//      scene.getStylesheets().add(getClass().getResource("Darkmode.css").toExternalForm());
//    } else {
//      darkModeMenuItem.setText("Enable Dark Mode");
//      scene.getStylesheets().remove(getClass().getResource("Darkmode.css").toExternalForm());
//    }
//  }

  public void onEnter(KeyEvent ke) {
    if(ke.getCode() == KeyCode.ENTER) {
      if(chatField.getText().isBlank()){
        chatField.setText("");
      }
      else {
        ke.consume();
        String text = chatField.getText();
        // removing the newline char that was added by pressing the enter key
        if (text.charAt(text.length() - 1) == '\n') {
          text = text.substring(0, chatField.getText().length() - 1);
        }
        features.sendTextOut(text);
        chatField.setText("");
      }
    }
  }

  public void setTextFieldEditable(boolean b) {
    chatField.setEditable(b);
  }

  public void appendChatLog(String s, String color, boolean hasDate) {
    if(hasDate) {
      appendMessage(formatDate(s), getColor(color));
    } else {
      appendMessage(s, getColor(color));
    }
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

    StackPane bubble = new StackPane();
    HBox surface = new HBox();
    surface.setPadding(new Insets(5, 5, 5, 5));

    for (String word : words) {
      // if the word equals an emoji name (ex. <3) then replace it with an ImageView of the emoji
      // if the word equals a twitch emoji (ex. PepeHands) then replace it with an ImageView of the twitch emoji
      // otherwise just add the word as plaintext
      if (MultiChatView.EMOTES.containsKey(word.trim())) {
        ImageView emoji = getEmote("/client/resources/images/emojis/" + MultiChatView.EMOTES.get(word.trim()), 20);
//        Platform.runLater(() -> chatLog.getChildren().add(emoji));
        surface.getChildren().add(emoji);
      } else if (MultiChatView.TWITCH_EMOTES.containsKey(word.trim())) {
        ImageView twitchEmote = getEmote("/client/resources/images/twitch/" + MultiChatView.TWITCH_EMOTES.get(word.trim()), 40);
//        Platform.runLater(() -> chatLog.getChildren().add(twitchEmote));
        surface.getChildren().add(twitchEmote);
      } else {
        Text txt = new Text(word + " ");
        txt.setFill(c);
//        Platform.runLater(() -> chatLog.getChildren().add(txt));
        surface.getChildren().add(txt);
      }
    }
    Rectangle rect = new Rectangle();
    rect.setX(0);
    rect.setY(0);
    rect.setWidth(surface.prefWidth(-1));
    rect.setHeight(surface.prefHeight(-1));
    System.out.println(surface.getWidth() + " " + surface.getHeight());
    System.out.println(surface.prefWidth(-1));
    rect.setArcWidth(20);
    rect.setArcHeight(20);
    rect.setFill(Color.CORNFLOWERBLUE);

    bubble.getChildren().addAll(rect, surface);
    Platform.runLater(() -> chatLog.getChildren().add(bubble));
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

  private String formatDate(String message) {
    String date = message.substring(0, message.indexOf("]"));
    String[] dateComponents = date.split(" ");
    String month = dateComponents[1];
    String day = dateComponents[2];
    String time = dateComponents[3];
    time = time.substring(0, time.lastIndexOf(":"));
    String timezone = dateComponents[4];

    StringBuilder buildDate = new StringBuilder();
    buildDate.append("[");
    buildDate.append(month);
    buildDate.append(" ");
    buildDate.append(day);
    buildDate.append(" ");
    buildDate.append(time);
    buildDate.append(" ");
    buildDate.append(timezone);
    buildDate.append("]");
    buildDate.append(message.substring(message.indexOf("]") + 1));

    return buildDate.toString();
  }

}