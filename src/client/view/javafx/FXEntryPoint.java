package client.view.javafx;

import client.controller.Feature;
import client.view.MultiChatView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

public class FXEntryPoint extends Application implements MultiChatView {
  private FXMLController controller;
  private Stage window;
  public static FXEntryPoint currentApp;
  public static final CountDownLatch initLatch = new CountDownLatch(1);
  public static CountDownLatch nameLatch = new CountDownLatch(1);
  private String name;
  private String prompt;

  public FXEntryPoint() {
    currentApp = this;
  }

  public static FXEntryPoint getCurrentInstance() {
    try {
      initLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return currentApp;
  }


  @Override
  public void giveFeatures(Feature feature) {
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
  public void appendChatLog(String s, String color, boolean hasDate) {
    controller.appendChatLog(s, color, hasDate);
  }

  @Override
  public void setTextFieldEditable(boolean b) {

  }

  @Override
  public String getName(String prompt) {
//    return FXGetNameDialog.display(prompt);
    nameLatch = new CountDownLatch(1);
    this.prompt = prompt;
    Platform.runLater(new RunDialog());
    try {
      nameLatch.await();
    } catch(Exception e) {
      System.out.println(e.getMessage());
    }
    return name;
  }

  @Override
  public void setActiveUsers(List<String> activeUsers) {

  }

  @Override
  public void setActiveServers(List<String> activeServers) {

  }

  @Override
  public void dispose() {
    window.close();
  }

  @Override
  public void start(Stage stage) throws Exception {
    window = stage;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("MultiChatFXML.fxml"));
    VBox flowPane = loader.load();
    // Get the Controller from the FXMLLoader
    controller = loader.getController();
    Scene scene = new Scene(flowPane, 800, 800);
    window.setScene(scene);
    window.setTitle("MultiChat");
    window.getIcons().add(new Image(this.getClass().getResourceAsStream("/client/resources/logo/multichat_logo.png")));
    initLatch.countDown();
  }

  private class RunDialog implements Runnable {

    @Override
    public void run() {
      new FXGetNameDialog().display(prompt);
    }
  }

  private class FXGetNameDialog {

    private String answer;

    private void display(String msg) {
      Stage nameWindow =  new Stage();
      nameWindow.setTitle("MultiChat - Name Selection");
      nameWindow.initModality(Modality.APPLICATION_MODAL);
      nameWindow.setOnCloseRequest(e -> System.exit(3));

      ImageView banner = new ImageView();
      Image fullLogo = new Image(getClass().getResourceAsStream("/client/resources/logo/multichat_full_logo.png"));
      banner.setImage(fullLogo);
      banner.minWidth(300);

      Label label = new Label(msg);

      TextField field = new TextField();
      field.setPromptText("name...");
      field.setMinWidth(250);

      HBox buttonPanel = new HBox();
      Button submitButton = new Button("Submit");
      submitButton.setOnAction(e -> {
        name = field.getText();
        nameLatch.countDown();
        nameWindow.close();
      });

      Button cancelButton = new Button("Cancel");
      cancelButton.setOnAction(e -> {
        System.exit(3);
      });
      buttonPanel.getChildren().addAll(cancelButton, submitButton);
      buttonPanel.setAlignment(Pos.CENTER);
      buttonPanel.setSpacing(35);


      VBox inputPanel = new VBox();
      inputPanel.setAlignment(Pos.CENTER);
      inputPanel.setSpacing(10);
      inputPanel.setPadding(new Insets(10, 30, 10, 30));
      inputPanel.getChildren().addAll(label, field, buttonPanel);

      VBox layout = new VBox();
      layout.setAlignment(Pos.CENTER);
      layout.getChildren().addAll(banner, inputPanel);

      Scene scene = new Scene(layout);
      nameWindow.setScene(scene);
      nameWindow.sizeToScene();
      nameWindow.setResizable(false);
      nameWindow.getIcons().add(new Image(this.getClass().getResourceAsStream("/client/resources/logo/multichat_logo.png")));

      banner.requestFocus();
      nameWindow.showAndWait();
    }

  }


}