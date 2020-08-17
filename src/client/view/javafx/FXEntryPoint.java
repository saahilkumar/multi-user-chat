package client.view.javafx;

import client.controller.Feature;
import client.view.MultiChatView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * A JavaFX Application class that provides an entry point in creating the MultiChat JavaFX view. Every instance stores
 * itself as a static variable so the controller can access this object when Application.launch has started. This class
 * also stores the controller of the GUI (FXMLController), the window, and the name of the user.
 */
public class FXEntryPoint extends Application implements MultiChatView {

  //a static variable to access the created anonymous object when launch is called
  private static FXEntryPoint currentApp;

  private FXMLController controller; //controller class that handles the javafx gui components
  private Stage window; //primary stage (window) of the javafx gui
  private String name; //the name of the user of this client

  private static final CountDownLatch waitForInitLatch = new CountDownLatch(1);
  private static CountDownLatch waitForNameLatch;
  private String prompt; //prompt for username request dialog windows

  private Feature feature;

  /**
   * Creates an instance of FXEntryPoint. When Application.launch() creates a new instance of this class, set the
   * created object to the public static variable
   */
  public FXEntryPoint() {
    currentApp = this;
  }

  /**
   * Gets the most recent instance of FXEntryPoint. A blocking method that will wait until an instance is called
   * and initialized.
   * @return the most recent instance of FXEntryPoint
   */
  public static FXEntryPoint getCurrentInstance() {
    try {
      waitForInitLatch.await(); //ensures a proper instance by waiting until the object has been fully initialized
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return currentApp;
  }

  @Override
  public void giveFeatures(Feature feature) {
    this.feature = feature;
    controller.setFeatures(feature);
  }

  @Override
  public void display() {
    Platform.runLater(() -> {
      window.show();
      window.setTitle("MultiChat - " + name);
    });

  }

  @Override
  public void displayError(boolean remainRunningWhenClosed, String errorMessage) {
    controller.displayError(remainRunningWhenClosed, errorMessage);
  }

  @Override
  public void appendChatLog(String s, String color, boolean hasDate, String protocol) {
    controller.appendChatLog(s, color, hasDate, protocol);
  }

  @Override
  public void setTextFieldEditable(boolean b) {
    controller.setTextFieldEditable(b);
  }

  @Override
  public String getName(String prompt) {
    this.prompt = prompt;
    Platform.runLater(() -> this.displayNameDialog(prompt)); //creates a pop-up dialog that requests a username

    //blocks this method, until the pop-up dialog supplies a legitimate name
    waitForNameLatch = new CountDownLatch(1);
    try {
      waitForNameLatch.await();
    } catch (InterruptedException e) {
      displayError(false, "Something went wrong: Name unsuccessfully sent.");
    }
    return name;
  }

  @Override
  public void setActiveUsers(List<String> activeUsers) {
    controller.setActiveUsers(activeUsers);
  }

  @Override
  public void setActiveServers(List<String> activeServers) {
    controller.setActiveServers(activeServers);
  }

  @Override
  public void dispose() {
    Platform.runLater(() -> window.close());
  }

  @Override
  public void start(Stage stage) throws Exception {
    window = stage;

    //get a FXML loader object based on the fxml file given
    FXMLLoader loader = new FXMLLoader(getClass().getResource("MultiChatFXML.fxml"));
    Parent pane = loader.load();

    // get the controller (FXMLController) from the FXMLLoader that handles gui logic and components
    controller = loader.getController();

    //initialize the primary scene from the fxml file
    Scene scene = new Scene(pane);
    window.setScene(scene);
    window.sizeToScene();
    controller.initialize(scene);

    //adds the icons to the taskbar and window frame
    window.getIcons().add(new Image(this.getClass().getResourceAsStream(
        "/client/resources/logo/multichat_logo.png")));

    window.setResizable(false);
    window.setOnCloseRequest(e -> {
      this.feature.sendTextOut("/quit");
    });

    //allows this instance to be accessed by the latch once this method finishes
    waitForInitLatch.countDown();
  }

  //a method that creates a dialog that requests the user for an username
  private void displayNameDialog(String message) {
    Stage dialogWindow = new Stage();
    dialogWindow.setOnCloseRequest(e -> {
      System.exit(4);
    });
    dialogWindow.setTitle("MultiChat - Name Selection");
    dialogWindow.initModality(Modality.APPLICATION_MODAL);

    VBox root = new VBox();
    root.setAlignment(Pos.CENTER);
    root.setSpacing(10);

    //inside root
    ImageView banner = new ImageView();
    Image fullLogo = new Image(getClass().getResourceAsStream(
        "/client/resources/logo/multichat_full_logo.png"));
    banner.setImage(fullLogo);

    //inside root
    VBox inputPanel = new VBox();
    inputPanel.setPadding(new Insets(10,30,10,30));
    inputPanel.setAlignment(Pos.CENTER);
    inputPanel.setSpacing(10);

    //inside inputPanel
    Label prompt = new Label(message);
    TextField field = new TextField();
    field.setPromptText("Enter your name...");

    //inside input Panel
    HBox buttonPanel = new HBox();
    buttonPanel.setAlignment(Pos.CENTER);
    buttonPanel.setSpacing(35);

    //inside button Panel
    Button submitButton = new Button("Submit");
    Button cancelButton = new Button("Cancel");
    submitButton.setOnAction((e) -> {
      name = field.getText();
      waitForNameLatch.countDown();
      dialogWindow.close();
    });
    cancelButton.setOnAction((e) -> System.exit(3));


    buttonPanel.getChildren().addAll(cancelButton, submitButton);
    inputPanel.getChildren().addAll(prompt, field, buttonPanel);
    root.getChildren().addAll(banner, inputPanel);

    dialogWindow.setScene(new Scene(root));
    dialogWindow.sizeToScene();
    dialogWindow.getIcons().add(new Image(this.getClass().getResourceAsStream(
        "/client/resources/logo/multichat_logo.png")));
    dialogWindow.setResizable(false);
    banner.requestFocus();
    dialogWindow.showAndWait();
  }



}