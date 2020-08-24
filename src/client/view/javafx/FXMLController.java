package client.view.javafx;

import client.controller.Feature;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FXMLController extends AbstractFXMLController {
  private Scene scene;
  private Set<PrivateMessagingController> privateMessagingWindows = new HashSet<>();

  @FXML
  private ListView<String> userListView = new ListView<>();
  @FXML
  private ListView<String> serverListView = new ListView<>();

  private ObservableList<String> userList = FXCollections.observableArrayList();
  private ObservableList<String> serverList = FXCollections.observableArrayList();
  private Map<String, Color> nameColors = new HashMap<>();

  private File chosenFile;

  // settings
  private boolean isDarkMode = false;
  private boolean muted = false;

  private static Color randomColor() {
    int red = ((int)(Math.random() * 255)) + 1;
    int green = ((int)(Math.random() * 255)) + 1;
    int blue = ((int)(Math.random() * 255)) + 1;
    return Color.web(String.format("rgb(%d,%d,%d)", red, green, blue));
  }

  public void setFeatures(Feature features) {
    this.features = features;
  }

  public void initialize(Scene scene) {
    this.scene = scene;
    this.serverListView.setItems(serverList);
    this.userListView.setItems(userList);

    super.initController();
  }

  @FXML
  private void onEnter(KeyEvent ke) {
    super.onEnter(ke, "");
  }

  public void appendChatLog(String s, String color, boolean hasDate, String protocol) {
    if(features.getClientUsername().equals(extractName(s))) {
      color = "white";
    }
    if (protocol.equals("PRIVATEMESSAGE")) {
      appendPrivateChatLog(s, color, hasDate, protocol);
    } else {
      super.appendChatLog(s, color, hasDate, protocol);
    }

  }

  private void appendPrivateChatLog(String s, String color, boolean hasDate, String protocol) {
    String[] components = s.substring(s.indexOf("]") + 2).split(": ");
    String date = s.substring(0, s.indexOf("]") + 1);
    String sender = components[0];
    String receiver = components[1];
    String messageAndReceiver = s.substring(s.indexOf(": ") + 2);
    String message = messageAndReceiver.substring(messageAndReceiver.indexOf(": ") + 2);
    if (sender.equals(features.getClientUsername())) { //if user sent this message
      for (PrivateMessagingController con : privateMessagingWindows) {
        if(con.getReceiver().equals(receiver)) {
          con.appendChatLog(date + " " + sender + ": " + message, color, hasDate, protocol);
          return;
        }
      }
    } else { //incoming message
      for (PrivateMessagingController con : privateMessagingWindows) {
        if(con.getReceiver().equals(sender)) {
          con.appendChatLog( date + " " + sender + ": " + message, color, hasDate, protocol);
          return;
        }
      }
      Platform.runLater(() ->{
        openPrivateMessagingWindow(sender);
        for (PrivateMessagingController con : privateMessagingWindows) {
          if(con.getReceiver().equals(sender)) {
            con.appendChatLog(date + " " + sender + ": " + message, color, hasDate, protocol);
            return;
          }
        }
      });
    }
  }

  public void setTextFieldEditable(boolean b) {
    chatField.setEditable(b);
  }

  public void setActiveUsers(List<String> activeUsers) {
    setActiveList(activeUsers, this.userList, this.userListView, true);
  }

  public void setActiveServers(List<String> activeServers) {
    setActiveList(activeServers, this.serverList, this.serverListView, false);
  }

  private void setActiveList(List<String> listOfNames, ObservableList<String> observableList,
      ListView<String> listView, boolean isUserList) {
    Platform.runLater(() -> {
      observableList.clear();
      observableList.addAll(listOfNames);
      this.mapNameToColor(listOfNames);
      listView.setCellFactory(lv -> new Cell(isUserList));
    });
  }

  @FXML
  private void openSettingsPanel() {
    new SettingsPanel();
  }

  @FXML
  private void openNewChatWindow() {
    new NewChatPanel();
  }

  private void mapNameToColor(List<String> listOfNames) {
    for(String name : listOfNames) {
      if(!nameColors.containsKey(name)) {
        nameColors.put(name, randomColor());
      }
    }
  }

  @FXML
  private void openFileExplorer() {
    Platform.runLater(() -> {
      FileChooser dialog = new FileChooser();
      dialog.setTitle("Select a file to upload.");
      File selected = dialog.showOpenDialog(scene.getWindow());
      if (!(selected == null)) {
        if (selected.length() < 25000000) {
          try {
            features.sendFile(selected.getName(), selected.length(), selected);
          } catch(IOException ioe) {
            displayError(true, "Something went wrong with sending the file!");
          }

        } else {
          appendChatLog("The file size cannot exceed 25mb", "orange", false, "MESSAGEHELP");
        }
      }
    });
  }

  @FXML
  private void openImageExplorer() {
    Platform.runLater(() -> {
      FileChooser dialog = new FileChooser();
      dialog.getExtensionFilters().addAll(
//          new FileChooser.ExtensionFilter("All Images", "*.*"),
          new FileChooser.ExtensionFilter("PNG", "*.png"),
          new FileChooser.ExtensionFilter("JPG", "*.jpg")
      );
      dialog.setTitle("Select a file to upload.");
      File selected = dialog.showOpenDialog(scene.getWindow());
      if (!(selected == null)) {
        if (selected.length() < 25000000) {
          try {
            features.sendFile(selected.getName(), selected.length(), selected);
          } catch(IOException ioe) {
            displayError(true, "Something went wrong with sending the file!");
          }

        } else {
          appendChatLog("The file size cannot exceed 25mb", "orange", false, "MESSAGEHELP");
        }
      }
    });
  }

  private void openPrivateMessagingWindow(String receiver) {
    if (features.getClientUsername().equals(receiver)) {
      appendChatLog("You cannot privately message yourself.", "red", false, "MESSAGEHELP");
      return;
    }
    for (PrivateMessagingController con : privateMessagingWindows) {
      if (con.getReceiver().equals(receiver)) {
        con.getWindow().toFront();
        return;
      }
    }
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("PrivateMessaging.fxml"));
      Parent pane = loader.load();
      Stage window = new Stage();
      window.setScene(new Scene(pane));
      PrivateMessagingController controller = loader.getController();
      controller.initialize(receiver, features.getClientUsername(), features, window);
      privateMessagingWindows.add(controller);
      controller.appendChatLog("You are now privately messaging " +
          receiver + ".", "blue", false, "MESSAGEWELCOME");
      window.sizeToScene();
      window.setResizable(false);
      window.setOnCloseRequest(e -> {
        privateMessagingWindows.remove(controller);
      });
      window.setTitle("Private Messaging - " + receiver);
      window.show();
    } catch (IOException ioe) {
      displayError(true,"Something went wrong: Unable to private message.");
    }
  }

  public File showSaveDialog(String fileName) {
    Platform.runLater(() -> {
      FileChooser chooser = new FileChooser();
      chooser.setInitialFileName(fileName);
      File file = chooser.showSaveDialog(scene.getWindow());
      setChosenFile(file);
    });
    return chosenFile;
  }

  private void setChosenFile(File file) {
    chosenFile = file;
  }

  public void setDarkMode(boolean isSelected) {
    isDarkMode = isSelected;
    if(isSelected) {
        scene.getStylesheets().add(getClass().getResource("Darkmode2.css").toExternalForm());
    } else {
        scene.getStylesheets().remove(getClass().getResource("Darkmode2.css").toExternalForm());
    }
  }

  private class Cell extends ListCell<String> {
    private boolean isUserList;

    private Cell(boolean isUserList) {
      this.isUserList = isUserList;
    }

    @Override
    public void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);
      if (empty) {
        setText(null);
        setGraphic(null);
      } else if (item != null) {
        MenuButton button;
        Circle userIcon = new Circle(5);
        if(isUserList) {
          userIcon.setFill(nameColors.get(item));

          MenuItem privateMessage = new MenuItem("Private Message");
          privateMessage.setOnAction(e -> openPrivateMessagingWindow(item));

          MenuItem kick = new MenuItem("Kick");
          kick.setOnAction(e -> features.sendTextOut("/votekick " + item));

          MenuItem whisper = new MenuItem("Whisper");
          whisper.setOnAction(e -> {
            Platform.runLater(() -> {
              chatField.setText("/whisper " + item + ": " + chatField.getText());
              chatField.requestFocus();
              chatField.positionCaret(chatField.getText().length());
            });
          });


          button = new MenuButton(item, userIcon, privateMessage, whisper, new SeparatorMenuItem(), kick);
        } else {
          int roomNum = Integer.parseInt(item.split(" ")[1]);
          userIcon.setFill(Color.GREEN);
          MenuItem join = new MenuItem("Join");
          join.setOnAction(e -> {
            features.sendTextOut("/join " + roomNum);
          });
          button = new MenuButton(item, userIcon, join);
        }

        button.setMaxWidth(Double.MAX_VALUE);
        setGraphic(button);
      }
    }
  }

  private class NewChatPanel {
    private NewChatPanel() {
      ObservableList<String> otherUsers = FXCollections.observableArrayList();
      for (String user : userList) {
        if (!user.equals(features.getClientUsername())) {
          otherUsers.add(user);
        }
      }

      VBox layout = new VBox();
      ImageView banner = new ImageView(new Image(getClass().getResourceAsStream(
          "/client/resources/logo/multichat_full_logo.png")));
      VBox textAndList = new VBox();
      textAndList.setPadding(new Insets(5, 5, 5, 5));
      textAndList.setSpacing(5);
      Text header = new Text("Please select a user to start chatting with.");
      header.setFont(new Font("Verdana", 12));
      ListView<String> displayNames = new ListView<>();
      displayNames.setItems(otherUsers);
      textAndList.getChildren().addAll(header, displayNames);
      textAndList.setAlignment(Pos.CENTER);

      Button submit = new Button("Create Chat");
      layout.getChildren().addAll(banner, textAndList, submit);
      layout.setAlignment(Pos.CENTER);

      Stage newChatWindow = new Stage();

      submit.setOnAction(e -> {
        String chosen = displayNames.getSelectionModel().getSelectedItem();
        if(chosen != null) {
          openPrivateMessagingWindow(chosen);
          newChatWindow.close();
        }
      });

      newChatWindow.initModality(Modality.APPLICATION_MODAL);
      newChatWindow.setScene(new Scene(layout));
      newChatWindow.setTitle("Start a new chat");
      newChatWindow.setResizable(false);
      newChatWindow.sizeToScene();
      newChatWindow.showAndWait();
    }
  }

  private class SettingsPanel {
    private SettingsPanel() {
      Stage settingsWindow = new Stage();
      VBox layout = new VBox();
      layout.setPadding(new Insets(20, 20, 20, 20));
      layout.setSpacing(20);

      CheckBox mute = new CheckBox("Mute Notification Sounds");
      mute.setSelected(muted);

      CheckBox darkMode = new CheckBox("Dark Mode");
      darkMode.setSelected(isDarkMode);
      darkMode.setOnAction(e -> setDarkMode(darkMode.isSelected()));

      layout.getChildren().addAll(mute, new Separator(), darkMode);

      settingsWindow.initModality(Modality.APPLICATION_MODAL);
      settingsWindow.setScene(new Scene(layout));
      settingsWindow.setTitle("Settings");
      settingsWindow.setResizable(false);
      settingsWindow.sizeToScene();
      settingsWindow.showAndWait();
    }
  }
}