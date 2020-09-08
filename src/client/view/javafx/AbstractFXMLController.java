package client.view.javafx;

import client.controller.Feature;
import client.view.MultiChatView;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

public class AbstractFXMLController {
  @FXML
  protected VBox chatLog;
  @FXML
  protected TextArea chatField;
  @FXML
  protected VBox fileButtons;
  @FXML
  protected GridPane twitchEmotePanel;
  @FXML
  protected GridPane standardEmotePanel;
  @FXML
  protected ScrollPane scrollPane;

  protected TranslateTransition slideUp;
  protected TranslateTransition slideDown;
  protected TranslateTransition slideFurtherUp;

  protected Feature features;
  protected String preface;


  protected void initController() {
    scrollPane.vvalueProperty().bind(chatLog.heightProperty());
    prepareButtonAnimation();
    initEmotePanels(MultiChatView.EMOTES,"/client/resources/images/emojis/", standardEmotePanel);
    initEmotePanels(MultiChatView.TWITCH_EMOTES,"/client/resources/images/twitch/", twitchEmotePanel);
  }

  private void initEmotePanels(Map<String, String> map, String path, GridPane panel) {
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
      emoteButton.getStyleClass().clear();
      emoteButton.getStyleClass().add("emote-button");
      emoteButton.setOnAction(e -> {
        features.sendTextOut(preface + emote);
      });
      panel.add(emoteButton, column, row);
      column++;
      if (column >= 16) {
        row ++;
        column = 0;
      }
    }

  }

  private void prepareButtonAnimation() {
    slideUp = new TranslateTransition(new Duration(300), fileButtons);
    slideUp.setToY(400);
    slideDown = new TranslateTransition(new Duration(300), fileButtons);
    slideDown.setToY(450);
    slideFurtherUp = new TranslateTransition(new Duration(300), fileButtons);
    slideFurtherUp.setToY(300);
  }

  @FXML
  protected void onEnter(KeyEvent ke) {
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
        features.sendTextOut(preface + text);
        chatField.setText("");
      }
    }
  }

  protected void getFile(boolean isPrivate, Window window, String receiver, String sender) {
    Platform.runLater(() -> {
      FileChooser dialog = new FileChooser();
      dialog.setTitle("Select a file to upload.");
      File selected = dialog.showOpenDialog(window);
      if (!(selected == null)) {
        if (selected.length() < 25000000) {
          try {
            features.sendFile(selected.getName(), selected.length(), selected, isPrivate, receiver, sender);
          } catch (IOException ioe) {
            displayError(true, "Something went wrong with sending the file!");
          }

        } else {
          appendChatLog("The file size cannot exceed 25mb", "orange", false, "MESSAGEHELP");
        }
      }
    });
  }

  protected void getImage(boolean isPrivate, Window window, String receiver, String sender) {
    Platform.runLater(() -> {
      FileChooser dialog = new FileChooser();
      dialog.getExtensionFilters().addAll(
          new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.png", "*.gif"),
          new FileChooser.ExtensionFilter("PNG", "*.png"),
          new FileChooser.ExtensionFilter("JPG", "*.jpg"),
          new FileChooser.ExtensionFilter("GIF", "*.gif")
      );
      dialog.setTitle("Select a file to upload.");
      File selected = dialog.showOpenDialog(window);
      if (!(selected == null)) {
        if (selected.length() < 25000000) {
          try {
            features.sendFile(selected.getName(), selected.length(), selected, isPrivate, receiver, sender);
          } catch (IOException ioe) {
            displayError(true, "Something went wrong with sending the file!");
          }

        } else {
          appendChatLog("The file size cannot exceed 25mb", "orange", false, "MESSAGEHELP");
        }
      }
    });
  }

  protected void appendChatLog(String s, String color, boolean hasDate, String protocol) {
    if (hasDate) {
      appendMessage(formatDate(s), getColor(color), hasDate, protocol);
    } else {
      appendMessage(s, getColor(color), hasDate, protocol);
    }
  }

  private void appendMessage(String msg, Color c, boolean hasDate, String protocol) {
    String date;
    String[] words;
    if (hasDate) {
      date = retrieveDate(msg);
      String restOfMessage = msg.substring(msg.indexOf("]") + 1);
      words = restOfMessage.split(" ");
    } else {
      // split message by space to check for emotes
      date = "";
      words = msg.split(" ");
    }

    Platform.runLater(() -> {
      VBox messageContainer = new VBox();
      StackPane bubbleWithMsg = new StackPane(); //stacks the text on top of a chat bubble
      messageContainer.setMaxWidth(Double.MAX_VALUE); //the stackpane fills to the width of chatlog
      bubbleWithMsg.setMaxWidth(Double.MAX_VALUE);

      if (!protocol.equals("MESSAGE") && !protocol.equals("WHISPER") && !protocol.equals("FILE") && !protocol.equals("PRIVATEMESSAGE") && !protocol.equals("PRIVATEFILE")) {
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

      HBox surface;
      if(protocol.equals("FILE") || protocol.equals("PRIVATEFILE")) {
        surface = createHyperLink(msg);
      } else {
        surface = textMessageWithImages(words, c); //contains the texts and images sent
      }

      //holds the surface HBox to ensure stackpane alignment affects all children
      Group message = new Group(surface);
      Rectangle rect;

      if (hasDate) {
        Text dateText = new Text(date);
        dateText.setFill(Color.GREY);
        dateText.setFont(new Font("Verdana", 8));
        //have the stackpane include text and a bubble underneath
        messageContainer.getChildren().addAll(bubbleWithMsg, dateText);
      } else {
        //have the stackpane include text and a bubble underneath
        messageContainer.getChildren().addAll(bubbleWithMsg);
      }
      rect = getBubbleGraphic(surface, msg, protocol); //the bubble underneath
      bubbleWithMsg.getChildren().addAll(rect, message);
      chatLog.getChildren().add(messageContainer); //append the stackpane to the chatlog
    });
  }

  private Color getColor(String color) {
    switch (color) {
      case "blue":
        return Color.CORNFLOWERBLUE;
      case "red":
        return Color.INDIANRED;
      case "green":
        return Color.LIMEGREEN;
      case "orange":
        return Color.ORANGE;
      case "white":
        return Color.WHITE;
      default:
        return Color.BLACK;
    }
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

  private String retrieveDate(String msg) {
    return msg.substring(0, msg.indexOf("]") + 1);
  }

  protected String extractName(String msg) {
    return msg.substring(msg.indexOf("]") + 2).split(": ")[0];
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

  private ImageView getEmote(String imagePath, int size) {
    ImageView imageView = new ImageView(
        new Image(getClass().getResource(imagePath).toExternalForm())
    );
    imageView.setFitHeight(size);
    imageView.setFitWidth(size);
    return imageView;
  }

  private HBox createHyperLink(String msg) {
    HBox surface = new HBox();
    surface.setPadding(new Insets(5, 5, 5, 5));
    surface.setAlignment(Pos.CENTER);
    Text name = new Text(extractName(msg) + ": ");
    String filename = msg.substring(msg.indexOf(": ") + 2);
    Hyperlink link = new Hyperlink();
    link.setText(filename);
    if (features.getClientUsername().equals(extractName(msg))) {
      name.setFill(Color.WHITE);
      link.setTextFill(Color.WHITE);
    }
    link.setOnAction(e -> features.sendTextOut("/requestfile " + extractName(msg) + ":" + filename));
    surface.getChildren().add(name);
    surface.getChildren().add(link);
    surface.setPrefWidth(new Text(extractName(msg) + ": " + filename).prefWidth(-1) + 20);
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
    if (protocol.equals("MESSAGE") || protocol.equals("FILE") || protocol.equals("PRIVATEMESSAGE") || protocol.equals("PRIVATEFILE")) {
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

  public void displayError(boolean remainRunningWhenClosed, String errorMessage) {
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage);
      if (!remainRunningWhenClosed) {
        alert.setOnCloseRequest(e -> System.exit(1));
      }
      alert.showAndWait();
    });
  }

}
