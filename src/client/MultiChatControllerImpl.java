package client;

import java.awt.Color;

public class MultiChatControllerImpl implements MultiChatController, Feature {

  private MultiChatModel model;
  private MultiChatView view;

  public MultiChatControllerImpl(MultiChatModel model, MultiChatView view) {
    this.model = model;
    this.view = view;
    this.view.giveFeatures(this);
  }

  public void run() {

    while(model.isConnectionRunning()) {
      String line = model.getSocketInput();
      System.out.println(line);

      if (line.startsWith("SUBMITNAME")) {
        String username = view.getName();
        model.sendText(username);
      } else if (line.startsWith("SUBMITANOTHERNAME")) {
        String username = view.getName();
        model.sendText(username);
      }else if (line.startsWith("NAMEACCEPTED")) {
        view.setTitle("MultiChat - " + line.substring(13));
        view.setTextFieldEditable(true);
        view.display();
      } else if (line.startsWith("MESSAGE ")) {
        view.appendChatLog(line.substring(8) + "\n", "black");
      } else if (line.startsWith("MESSAGEUSERJOINED ")) {
        view.appendChatLog(line.substring(18) + "\n", "green");
      } else if (line.startsWith("MESSAGEUSERLEFT ")) {
        view.appendChatLog(line.substring(16) + "\n", "red");
      }
    }
  }

  @Override
  public void sendTextOut(String out) {
    model.sendText(out);
  }
}
