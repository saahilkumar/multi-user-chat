package client.controller;

import client.model.MultiChatModel;
import client.view.MultiChatView;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        String username = view.getName("Choose a screen name:");
        model.sendText(username);
      } else if (line.startsWith("SUBMITANOTHERNAME")) {
        String username = view.getName("Please select a different screen name:");
        model.sendText(username);
      } else if (line.startsWith("NAMEACCEPTED")) {
        view.setTitle("MultiChat - " + line.substring(13));
        view.setTextFieldEditable(true);
        view.display();
      } else if (line.startsWith("MESSAGE ")) {
        view.appendChatLog(line.substring(8) + "\n", "black", true);
      } else if (line.startsWith("MESSAGEUSERJOINED ")) {
        view.appendChatLog(line.substring(18) + "\n", "green", true);
      } else if (line.startsWith("MESSAGEUSERLEFT ")) {
        view.appendChatLog(line.substring(16) + "\n", "red", true);
      } else if (line.startsWith("MESSAGEWELCOME ")) {
        view.appendChatLog(line.substring(15) + "\n", "blue", false);
      } else if (line.startsWith("ACTIVEUSERLIST ")) {
        String[] arr = line.substring(15).split(",");
        List<String> names = new ArrayList<>(Arrays.asList(arr));
        view.setActiveUsers(names);
      } else if (line.startsWith("MESSAGEHELP ")) {
        view.appendChatLog(line.substring(12) + "\n", "orange", false);
      }
    }

    view.dispose();
  }

  @Override
  public void sendTextOut(String out) {
    model.sendText(out);
  }
}
