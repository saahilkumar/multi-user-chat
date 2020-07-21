package client;

import client.controller.MultiChatController;
import client.controller.MultiChatControllerImpl;
import client.model.MultiChatModel;
import client.model.MultiChatModelImpl;
import client.view.MultiChatView;
import client.view.MultiChatViewImpl;
import java.io.IOException;

public class MultiChatClient {
  public static void main(String[] args) {
    if(args.length != 1) {
      System.out.println("Pass the server IP as the sole input");
      return;
    }

    try {
      MultiChatModel model = new MultiChatModelImpl(args[0]);
      MultiChatView view = new MultiChatViewImpl();
      MultiChatController controller = new MultiChatControllerImpl(model, view);

      controller.run();
    } catch(IOException ioe) {
      System.out.println("Failed to connect to Server Socket");
    }
  }
}
