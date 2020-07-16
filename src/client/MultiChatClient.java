package client;

import java.io.IOException;
import javax.swing.JFrame;

public class MultiChatClient {
  public static void main(String[] args) {
    if(args.length != 1) {
      System.out.println("Pass the server IP as the sole input");
      return;
    }

//    MultiChatView view = new MultiChatViewImpl();
//    view.display();
    try {
      MultiChatView view = new MultiChatViewImpl();
      MultiChatModel model = new MultiChatModelImpl(args[0]);
      MultiChatController controller = new MultiChatControllerImpl(model, view);

      controller.run();
    } catch(IOException ioe) {
      System.out.println("Failed to connect to Server Socket");
    }
//    gui.setTitle("MultiChat");
//    gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    gui.setVisible(true);
//    gui.setPreferredSize(new Dimension(300, 300));
//    JTextField textField = new JTextField(50);
//    gui.add(textField);
//    textField.setVisible(true);
//    gui.setVisible(true);
  }
}
