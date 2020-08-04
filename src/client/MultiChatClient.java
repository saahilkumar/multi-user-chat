package client;

import client.controller.MultiChatController;
import client.controller.MultiChatControllerImpl;
import client.model.MultiChatModel;
import client.model.MultiChatModelImpl;
import client.view.MultiChatView;
import client.view.javafx.FXEntryPoint;
import client.view.swing.MultiChatViewImpl;
import java.io.IOException;
import javafx.application.Application;

public class MultiChatClient {
  public static void main(String[] args) {
    String ipAddress = "";
    int portNum = 59090;
    if(args.length == 1) {
      ipAddress = args[0];
    } else if(args.length == 2) {
      ipAddress = args[0];
      portNum = Integer.parseInt(args[1]);
    } else {
      System.out.println("Please enter the ip address and the port number as the two inputs");
    }

    try {
      MultiChatModel model = new MultiChatModelImpl(ipAddress, portNum);
//      MultiChatView view = new MultiChatViewImpl();
      new Thread(() -> Application.launch(FXEntryPoint.class)).start();
      var view = FXEntryPoint.getCurrentInstance();
      MultiChatController controller = new MultiChatControllerImpl(model, view);

      controller.run();
    } catch(IOException ioe) {
      System.out.println("Failed to connect to Server Socket");
    }
  }
}
