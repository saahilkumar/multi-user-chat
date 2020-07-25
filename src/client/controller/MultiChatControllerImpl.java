package client.controller;

import client.model.MultiChatModel;
import client.view.MultiChatView;
import java.io.IOException;
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
        view.setTextFieldEditable(true);
        view.display();
      } else if (line.startsWith("MESSAGE ")) {
        view.appendChatLog(line.substring(8), "black", true);
      } else if (line.startsWith("MESSAGEUSERJOINED ")) {
        view.appendChatLog(line.substring(18), "green", true);
      } else if (line.startsWith("MESSAGEUSERLEFT ")) {
        view.appendChatLog(line.substring(16), "red", true);
      } else if (line.startsWith("MESSAGEWELCOME ")) {
        view.appendChatLog(line.substring(15), "blue", false);
      } else if (line.startsWith("ACTIVEUSERLIST ")) {
        String[] arr = line.substring(15).split(",");
        List<String> names = new ArrayList<>(Arrays.asList(arr));
        view.setActiveUsers(names);
      } else if (line.startsWith("MESSAGEHELP ")) {
        view.appendChatLog(line.substring(12), "orange", false);
      } else if (line.startsWith("ACTIVESERVERLIST ")) {
        String[] arr = line.substring(17).split(",");
        List<String> servers = new ArrayList<>(Arrays.asList(arr));
        view.setActiveServers(servers);
      } else if (line.startsWith("REQUESTEDNEWROOM ")) {
        try {
          MultiChatModel newModel = model.switchPorts(line.substring(17));
          model.sendText("/quit");
          view.appendChatLog("Successfully left.", "red", false);
          model = newModel;
        } catch (IOException e) {
          model.sendText("UNSUCCESSFULROOMCHANGE Cannot connect to new chat room.");
        } catch (NumberFormatException nfe) {
          model.sendText("UNSUCCESSFULROOMCHANGE Cannot find specified room number.");
        }
      }
    }

    view.dispose();
  }

  @Override
  public void sendTextOut(String out) {
    model.sendText(out);
  }
}
