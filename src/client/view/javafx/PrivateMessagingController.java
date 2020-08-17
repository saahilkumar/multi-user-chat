package client.view.javafx;

import client.controller.Feature;
import client.view.MultiChatView;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Map;

public class PrivateMessagingController {
  private String receiver;
  private String sender;
  private Feature features;
  private Stage window;

  @FXML
  private VBox chatLog;
  @FXML
  private TextArea chatField;
  @FXML
  private VBox fileButtons;
  @FXML
  private GridPane twitchEmotePanel;
  @FXML
  private GridPane standardEmotePanel;

  private TranslateTransition slideUp;
  private TranslateTransition slideDown;
  private TranslateTransition slideFurtherUp;

  public void initialize(String receiver, String sender, Feature features, Stage window) {
    this.receiver = receiver;
    this.sender = sender;
    this.features = features;
    this.window = window;
    prepareButtonAnimation();
    initializeEmotePanels(MultiChatView.EMOTES,"/client/resources/images/emojis/", standardEmotePanel);
    initializeEmotePanels(MultiChatView.TWITCH_EMOTES,"/client/resources/images/twitch/", twitchEmotePanel);
  }

  public void appendChatLog(String s, String color, boolean hasDate, String protocol) {
    if (hasDate) {
      appendMessage(formatDate(s), getColor(color), hasDate, protocol);
    } else {
      appendMessage(s, getColor(color), hasDate, protocol);
    }
  }

  private void appendMessage(String msg, Color c, boolean hasDate, String protocol) {
    String date;
    String restOfMessage;
    String[] words;
    if (hasDate) {
      date = retrieveDate(msg);
      restOfMessage = msg.substring(msg.indexOf("]") + 1);
      words = restOfMessage.split(" ");
    } else {
      // split message by space to check for emotes
      date = "";
      restOfMessage = "";
      words = msg.split(" ");
    }

    Platform.runLater(() -> {
      VBox messageContainer = new VBox();
      StackPane bubbleWithMsg = new StackPane(); //stacks the text on top of a chat bubble
      messageContainer.setMaxWidth(Double.MAX_VALUE); //the stackpane fills to the width of chatlog
      messageContainer.setSpacing(2);
      bubbleWithMsg.setMaxWidth(Double.MAX_VALUE);

      if (!protocol.equals("PRIVATEMESSAGE") && !protocol.equals("WHISPER")) {
        messageContainer.setAlignment(Pos.CENTER); //if it is not user text, center it (ie. black text messages)
        bubbleWithMsg.setAlignment(Pos.CENTER); //if it is not user text, center it (ie. black text messages)
      } else if (extractName(msg).equals(features.getClientUsername())) {
        //if it is user text and sent by the user, align to right
        messageContainer.setAlignment(Pos.BASELINE_RIGHT);
        bubbleWithMsg.setAlignment(Pos.BASELINE_RIGHT);
      } else {
        //if it is user text and not sent by the user, align to left
        messageContainer.setAlignment(Pos.BASELINE_LEFT);
        bubbleWithMsg.setAlignment(Pos.BASELINE_LEFT);
      }

      HBox surface = textMessageWithImages(words, c); //contains the texts and images sent
      //holds the surface HBox to ensure stackpane alignment affects all children
      Group message = new Group(surface);
      Rectangle rect;

      if (hasDate) {
        Text dateText = new Text(date);
        dateText.setFill(Color.GREY);
        dateText.setFont(new Font("Verdana", 8));
        rect = getBubbleGraphic(surface, restOfMessage, protocol); //the bubble underneath
        //have the stackpane include text and a bubble underneath
        bubbleWithMsg.getChildren().addAll(rect, message);
        if (protocol.equals("WHISPER")){
          Text fromMessage = new Text(extractName(msg) + " whispered to you: ");
          fromMessage.setFill(Color.GREY);
          fromMessage.setFont(new Font("Verdana", 8));
          messageContainer.getChildren().addAll(fromMessage, bubbleWithMsg, dateText);
        } else {
          messageContainer.getChildren().addAll(bubbleWithMsg, dateText);
        }
      } else {
        rect = getBubbleGraphic(surface, msg, protocol); //the bubble underneath
        //have the stackpane include text and a bubble underneath
        bubbleWithMsg.getChildren().addAll(rect, message);
        messageContainer.getChildren().addAll(bubbleWithMsg);
      }

      chatLog.getChildren().add(messageContainer); //append the stackpane to the chatlog

    });
  }

  private HBox textMessageWithImages(String[] words, Color c) {
    HBox surface = new HBox();
    surface.setPadding(new Insets(5,5,5,5));
    for (String word : words) {
      // if the word equals an emoji name (ex. <3) then replace it with an ImageView of the emoji
      // if the word equals a twitch emoji (ex. PepeHands) then replace it with an ImageView of the twitch emoji
      // otherwise just add the word as plaintext
      if (MultiChatView.EMOTES.containsKey(word.trim())) {
        ImageView emoji = getEmote("/client/resources/images/emojis/" +
            MultiChatView.EMOTES.get(word.trim()), 20);
        surface.getChildren().add(emoji);
      } else if (MultiChatView.TWITCH_EMOTES.containsKey(word.trim())) {
        ImageView twitchEmote = getEmote("/client/resources/images/twitch/" +
            MultiChatView.TWITCH_EMOTES.get(word.trim()), 40);
        surface.getChildren().add(twitchEmote);
      } else {
        Text txt = new Text(word + " ");
        txt.setFill(c);
        txt.setTextAlignment(TextAlignment.RIGHT);
        surface.getChildren().add(txt);
      }
    }
    return surface;
  }

  private Rectangle getBubbleGraphic(HBox surface, String msg, String protocol) {
    Rectangle rect = new Rectangle();
    rect.setX(0);
    rect.setY(0);
    rect.setWidth(surface.prefWidth(-1));
    rect.setHeight(surface.prefHeight(-1));
    rect.setArcWidth(20);
    rect.setArcHeight(20);
    if (protocol.equals("PRIVATEMESSAGE")) {
      if (extractName(msg).equals(features.getClientUsername())) {
        rect.setFill(Color.CORNFLOWERBLUE);
      } else {
        rect.setFill(Color.LIGHTGREY);
      }
    } else if(protocol.equals("WHISPER")) {
      rect.setFill(Color.HOTPINK);
    } else {
      rect.setFill(Color.TRANSPARENT);
    }
    return rect;
  }

  //extracts the username of the message
  private String extractName(String msg) {
    return msg.substring(msg.indexOf("]") + 2).split(": ")[0];
  }

  private Color getColor(String color) {
    switch (color) {
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

  private ImageView getEmote(String imagePath, int size) {
    ImageView imageView = new ImageView(
        new Image(getClass().getResource(imagePath).toExternalForm())
    );
    imageView.setFitHeight(size);
    imageView.setFitWidth(size);
    return imageView;
  }

  private void prepareButtonAnimation() {
    slideUp = new TranslateTransition(new Duration(300), fileButtons);
    slideUp.setToY(400);
    slideDown = new TranslateTransition(new Duration(300), fileButtons);
    slideDown.setToY(450);
    slideFurtherUp = new TranslateTransition(new Duration(300), fileButtons);
    slideFurtherUp.setToY(300);
  }

  //formats new Date.toString() into more readable dates, removing the seconds and year from the time
  //returns the entire message with the date formatted
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

  private String retrieveDate(String msg) {
    return msg.substring(0, msg.indexOf("]") + 1);
  }

  private void initializeEmotePanels(Map<String, String> map, String path, GridPane panel) {
    int column = 0;
    int row = 0;
    for (String emote : map.keySet()) {
      Button emoteButton = new Button();
      emoteButton.setMinSize(30, 30);
      emoteButton.setMaxSize(30, 30);
      Image image = new Image(getClass().getResource(path + map.get(emote)).toExternalForm(),
          emoteButton.getWidth(), emoteButton.getHeight(), false, true, true);
      BackgroundImage bImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
          BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
          new BackgroundSize(emoteButton.getWidth(), emoteButton.getHeight(), true, true, true, false));

      Background backGround = new Background(bImage);
      emoteButton.setBackground(backGround);
      emoteButton.setOnAction(e -> {
        features.sendTextOut("/privatemsg " + sender + ": " + receiver + ": " + emote);
      });
      panel.add(emoteButton, column, row);
      column++;
      if (column >= 16) {
        row ++;
        column = 0;
      }
    }

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
  private void showEmotePanel() {
    if (fileButtons.getTranslateY() == 300) {
      slideUp.play();
    } else {
      slideFurtherUp.play();
    }

  }

  @FXML
  private void showButtonPanel() {
    Platform.runLater(() -> {
      if(fileButtons.getTranslateY() == 450){
        slideUp.play();
      } else{
        slideDown.play();
      }
    });
  }

  @FXML
  private void onEnter(KeyEvent ke) {
    String text = chatField.getText();
    if (ke.getCode() == KeyCode.ENTER) {
      if (text.isBlank()) {
        chatField.setText("");
      } else {
        ke.consume();
        String lastCharacter = text.substring(text.length() - 1);
        if (lastCharacter.equals("\n")) {
          text = text.substring(0, text.length() - 1);
        }
        features.sendTextOut("/privatemsg " + sender + ": " + receiver + ": " + text);
        chatField.setText("");
      }
    }
  }

  public String getReceiver() {
    return receiver;
  }

  public Stage getWindow() {
    return window;
  }
}

//package client.view.javafx;
//
//import client.controller.Feature;
//import client.view.MultiChatView;
//import java.io.File;
//import java.util.Map;
//import javafx.animation.TranslateTransition;
//import javafx.application.Platform;
//import javafx.fxml.FXML;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Group;
//import javafx.scene.control.Button;
//import javafx.scene.control.TextArea;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.input.KeyCode;
//import javafx.scene.input.KeyEvent;
//import javafx.scene.layout.Background;
//import javafx.scene.layout.BackgroundImage;
//import javafx.scene.layout.BackgroundPosition;
//import javafx.scene.layout.BackgroundRepeat;
//import javafx.scene.layout.BackgroundSize;
//import javafx.scene.layout.GridPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.StackPane;
//import javafx.scene.layout.VBox;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Rectangle;
//import javafx.scene.text.Font;
//import javafx.scene.text.Text;
//import javafx.scene.text.TextAlignment;
//import javafx.stage.FileChooser;
//import javafx.stage.Stage;
//import javafx.util.Duration;
//
//public class PrivateMessagingController {
//  private String receiver;
//  private String sender;
//  private Feature features;
//  private Stage window;
//
//  private TranslateTransition slideUp;
//  private TranslateTransition slideDown;
//  private TranslateTransition slideFurtherUp;
//
//  @FXML
//  private GridPane twitchEmotePanel;
//  @FXML
//  private GridPane standardEmotePanel;
//  @FXML
//  private VBox fileButtons;
//  @FXML
//  private TextArea chatField;
//  @FXML
//  private VBox chatLog;
//
//  public PrivateMessagingController() {
//
//  }
//
//  public void initialize(String receiver, String sender, Feature features, Stage window) {
//    this.receiver = receiver;
//    this.sender = sender;
//    this.features = features;
//    this.window = window;
//
//    prepareButtonAnimation();
//    initializeEmotePanels(MultiChatView.EMOTES,"/client/resources/images/emojis/", standardEmotePanel);
//    initializeEmotePanels(MultiChatView.TWITCH_EMOTES,"/client/resources/images/twitch/", twitchEmotePanel);
//  }
//
//  @FXML
//  private void openFileExplorer() {
//    Platform.runLater(() -> {
//      FileChooser dialog = new FileChooser();
//      dialog.setTitle("Select a file to upload.");
//      File selected = dialog.showOpenDialog(window);
//      if (!(selected == null)) {
//        if (selected.length() > 25000000) {
//
//        } else {
//
//        }
//      }
//    });
//  }
//
//  @FXML
//  private void showEmotePanel() {
//    if (fileButtons.getTranslateY() == 300) {
//      slideUp.play();
//    } else {
//      slideFurtherUp.play();
//    }
//
//  }
//
//  @FXML
//  private void showButtonPanel() {
//    Platform.runLater(() -> {
//      if(fileButtons.getTranslateY() == 450){
//        slideUp.play();
//      }else{
//        slideDown.play();
//      }
//    });
//  }
//
//  @FXML
//  private void onEnter(KeyEvent ke) {
//    String text = chatField.getText();
//    if (ke.getCode() == KeyCode.ENTER) {
//      if (text.isBlank()) {
//        chatField.setText("");
//      } else {
//        ke.consume();
//        String lastCharacter = text.substring(text.length() - 1);
//        if (lastCharacter.equals("\n")) {
//          text = text.substring(0, text.length() - 1);
//        }
//        features.sendTextOut("/privatemsg " + sender + ":" + receiver + ":" + text);
//        chatField.setText("");
//      }
//    }
//  }
//
//  private void prepareButtonAnimation() {
//    slideUp = new TranslateTransition(new Duration(300), fileButtons);
//    slideUp.setToY(400);
//    slideDown = new TranslateTransition(new Duration(300), fileButtons);
//    slideDown.setToY(450);
//    slideFurtherUp = new TranslateTransition(new Duration(300), fileButtons);
//    slideFurtherUp.setToY(300);
//  }
//
//  private void initializeEmotePanels(Map<String, String> map, String path, GridPane panel) {
//    int column = 0;
//    int row = 0;
//    for (String emote : map.keySet()) {
//      Button emoteButton = new Button();
//      emoteButton.setMinSize(30, 30);
//      emoteButton.setMaxSize(30, 30);
//      Image image = new Image(getClass().getResource(path + map.get(emote)).toExternalForm(),
//          emoteButton.getWidth(), emoteButton.getHeight(), false, true, true);
//      BackgroundImage bImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
//          BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
//          new BackgroundSize(emoteButton.getWidth(), emoteButton.getHeight(), true, true, true, false));
//
//      Background backGround = new Background(bImage);
//      emoteButton.setBackground(backGround);
//      emoteButton.setOnAction(e -> {
//        features.sendTextOut("/privatemsg " + sender + ":" + receiver + ":" + emote);
//      });
//      panel.add(emoteButton, column, row);
//      column++;
//      if (column >= 16) {
//        row ++;
//        column = 0;
//      }
//    }
//
//  }
//
//
//
//  public void appendChatLog(String s, String color, boolean hasDate, String protocol) {
//    if (hasDate) {
//      appendMessage(formatDate(s), getColor(color), hasDate, protocol);
//    } else {
//      appendMessage(s, getColor(color), hasDate, protocol);
//    }
//  }
//
//  private void appendMessage(String msg, Color c, boolean hasDate, String protocol) {
//    String date;
//    String restOfMessage;
//    String[] words;
//    if (hasDate) {
//      date = retrieveDate(msg);
//      restOfMessage = msg.substring(msg.indexOf("]") + 1);
//      words = restOfMessage.split(" ");
//    } else {
//      // split message by space to check for emotes
//      date = "";
//      restOfMessage = "";
//      words = msg.split(" ");
//    }
//
//    Platform.runLater(() -> {
//      VBox messageContainer = new VBox();
//      StackPane bubbleWithMsg = new StackPane(); //stacks the text on top of a chat bubble
//      messageContainer.setMaxWidth(Double.MAX_VALUE); //the stackpane fills to the width of chatlog
//      bubbleWithMsg.setMaxWidth(Double.MAX_VALUE);
//
//      if (!protocol.equals("MESSAGE") && !protocol.equals("WHISPER")) {
//        messageContainer.setAlignment(Pos.CENTER); //if it is not user text, center it (ie. black text messages)
//        bubbleWithMsg.setAlignment(Pos.CENTER); //if it is not user text, center it (ie. black text messages)
//      } else if (extractName(msg).equals(features.getClientUsername())) {
//        //if it is user text and sent by the user, align to right
//        messageContainer.setAlignment(Pos.BASELINE_RIGHT);
//        bubbleWithMsg.setAlignment(Pos.BASELINE_RIGHT);
//      } else {
//        //if it is user text and not sent by the user, align to left
//        messageContainer.setAlignment(Pos.BASELINE_LEFT);
//        bubbleWithMsg.setAlignment(Pos.BASELINE_LEFT);
//      }
//
//      HBox surface = textMessageWithImages(words, c); //contains the texts and images sent
//      //holds the surface HBox to ensure stackpane alignment affects all children
//      Group message = new Group(surface);
//      Rectangle rect;
//
//      if (hasDate) {
//        Text dateText = new Text(date);
//        dateText.setFill(Color.GREY);
//        dateText.setFont(new Font("Verdana", 8));
//        rect = getBubbleGraphic(surface, restOfMessage, protocol); //the bubble underneath
//        //have the stackpane include text and a bubble underneath
//        bubbleWithMsg.getChildren().addAll(rect, message);
//        messageContainer.getChildren().addAll(bubbleWithMsg, dateText);
//        chatLog.getChildren().add(messageContainer); //append the stackpane to the chatlog
//      } else {
//        rect = getBubbleGraphic(surface, msg, protocol); //the bubble underneath
//        //have the stackpane include text and a bubble underneath
//        bubbleWithMsg.getChildren().addAll(rect, message);
//        messageContainer.getChildren().addAll(bubbleWithMsg);
//        chatLog.getChildren().add(messageContainer); //append the stackpane to the chatlog
//      }
//    });
//  }
//
//  private HBox textMessageWithImages(String[] words, Color c) {
//    HBox surface = new HBox();
//    surface.setPadding(new Insets(5,5,5,5));
//    for (String word : words) {
//      // if the word equals an emoji name (ex. <3) then replace it with an ImageView of the emoji
//      // if the word equals a twitch emoji (ex. PepeHands) then replace it with an ImageView of the twitch emoji
//      // otherwise just add the word as plaintext
//      if (MultiChatView.EMOTES.containsKey(word.trim())) {
//        ImageView emoji = getEmote("/client/resources/images/emojis/" +
//            MultiChatView.EMOTES.get(word.trim()), 20);
//        surface.getChildren().add(emoji);
//      } else if (MultiChatView.TWITCH_EMOTES.containsKey(word.trim())) {
//        ImageView twitchEmote = getEmote("/client/resources/images/twitch/" +
//            MultiChatView.TWITCH_EMOTES.get(word.trim()), 40);
//        surface.getChildren().add(twitchEmote);
//      } else {
//        Text txt = new Text(word + " ");
//        txt.setFill(c);
//        txt.setTextAlignment(TextAlignment.RIGHT);
//        surface.getChildren().add(txt);
//      }
//    }
//    return surface;
//  }
//
//  private Rectangle getBubbleGraphic(HBox surface, String msg, String protocol) {
//    Rectangle rect = new Rectangle();
//    rect.setX(0);
//    rect.setY(0);
//    rect.setWidth(surface.prefWidth(-1));
//    rect.setHeight(surface.prefHeight(-1));
//    rect.setArcWidth(20);
//    rect.setArcHeight(20);
//    if (protocol.equals("MESSAGE")) {
//      if (extractName(msg).equals(features.getClientUsername())) {
//        rect.setFill(Color.CORNFLOWERBLUE);
//      } else {
//        rect.setFill(Color.LIGHTGREY);
//      }
//    } else if(protocol.equals("WHISPER")) {
//      rect.setFill(Color.HOTPINK);
//    } else {
//      rect.setFill(Color.TRANSPARENT);
//    }
//    return rect;
//  }
//
//  private ImageView getEmote(String imagePath, int size) {
//    ImageView imageView = new ImageView(
//        new Image(getClass().getResource(imagePath).toExternalForm())
//    );
//    imageView.setFitHeight(size);
//    imageView.setFitWidth(size);
//    return imageView;
//  }
//
//  private Color getColor(String color) {
//    switch (color) {
//      case "blue":
//        return Color.BLUE;
//      case "red":
//        return Color.RED;
//      case "green":
//        return Color.GREEN;
//      case "orange":
//        return Color.ORANGE;
//      case "white":
//        return Color.WHITE;
//      default:
//        return Color.BLACK;
//    }
//  }
//
//  private String formatDate(String message) {
//    String date = message.substring(0, message.indexOf("]"));
//    String[] dateComponents = date.split(" ");
//    String month = dateComponents[1];
//    String day = dateComponents[2];
//    String time = dateComponents[3];
//    time = time.substring(0, time.lastIndexOf(":"));
//    String timezone = dateComponents[4];
//
//    StringBuilder buildDate = new StringBuilder();
//    buildDate.append("[");
//    buildDate.append(month);
//    buildDate.append(" ");
//    buildDate.append(day);
//    buildDate.append(" ");
//    buildDate.append(time);
//    buildDate.append(" ");
//    buildDate.append(timezone);
//    buildDate.append("]");
//    buildDate.append(message.substring(message.indexOf("]") + 1));
//
//    return buildDate.toString();
//  }
//
//  //extracts the username of the message
//  private String extractName(String msg) {
//    return msg.substring(msg.indexOf("]") + 2).split(": ")[0];
//  }
//
//  private String retrieveDate(String msg) {
//    return msg.substring(0, msg.indexOf("]") + 1);
//  }
//
//  public String getReceiver() {
//    return this.receiver;
//  }
//
//
//}
