package client.controller;

import client.model.MultiChatModel;
import client.view.MultiChatView;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    String clientName = "";

    while(model.isConnectionRunning()) {
      String line = model.getSocketInput();
      System.out.println(line);

      if (line.startsWith("SUBMITNAME")) {
        String username = view.getName("Choose a screen name:");
        model.sendText(username);
        clientName = username;
      } else if (line.startsWith("SUBMITANOTHERNAME")) {
        String username = view.getName("Please select a different screen name:");
        model.sendText(username);
        clientName = username;
      } else if (line.startsWith("NAMEACCEPTED")) {
        view.display();
        view.setTextFieldEditable(true);
        model.setUsername(clientName);
      } else if (line.startsWith("MESSAGE ")) {
        view.appendChatLog(line.substring(8), "black", true, "MESSAGE");
      } else if (line.startsWith("MESSAGEUSERJOINED ")) {
        view.appendChatLog(line.substring(18), "green", true, "MESSAGEUSERJOINED");
      } else if (line.startsWith("MESSAGEUSERLEFT ")) {
        view.appendChatLog(line.substring(16), "red", true, "MESSAGEUSERLEFT");
      } else if (line.startsWith("MESSAGEWELCOME ")) {
        view.appendChatLog(line.substring(15), "blue", false, "MESSAGEWELCOME");
      } else if (line.startsWith("ACTIVEUSERLIST ")) {
        String[] arr = line.substring(15).split(",");
        List<String> names = new ArrayList<>(Arrays.asList(arr));
        view.setActiveUsers(names);
      } else if (line.startsWith("MESSAGEHELP ")) {
        view.appendChatLog(line.substring(12), "orange", false, "MESSAGEHELP");
      } else if (line.startsWith("ACTIVESERVERLIST ")) {
        String[] arr = line.substring(17).split(",");
        List<String> servers = new ArrayList<>(Arrays.asList(arr));
        view.setActiveServers(servers);
      } else if(line.startsWith("VOTEKICK ")) {
        view.appendChatLog(line.substring(9), "orange", false, "VOTEKICK");
      } else if(line.startsWith("FAILEDVOTEKICK ")) {
        view.appendChatLog(line.substring(15), "red", false, "FAILEDVOTEKICK");
      } else if(line.startsWith("SUCCESSFULVOTEKICK ")) {
        view.appendChatLog(line.substring(19), "red", false, "SUCCESSFULVOTEKICK");
      } else if(line.startsWith("WHISPER ")) {
        view.appendChatLog(line.substring(8), "white", true, "WHISPER");
      } else if(line.startsWith("FAILEDWHISPER ")) {
        view.appendChatLog(line.substring(14), "red", false, "FAILEDWHISPER");
      } else if(line.startsWith("PRIVATEMESSAGE ")) {
        view.appendChatLog(line.substring(15), "black", true, "PRIVATEMESSAGE");
      } else if(line.startsWith("FILE ")) {
        view.appendChatLog(line.substring(5), "black", false, "FILE");
      } else if(line.startsWith("FAILEDFILETRANSFER ")) {
        view.appendChatLog(line.substring(19), "red", false, "FAILEDFILETRANSFER");
      }
      else if (line.startsWith("REQUESTEDNEWROOM ")) {
        try {
          MultiChatModel newModel = model.switchPorts(line.substring(17));
          model.sendText("/quit");
          view.appendChatLog("Successfully left.", "red", false, "REQUESTEDNEWROOM");
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

  @Override
  public String getClientUsername() {
    return model.getUsername();
  }

  @Override
  public void sendFile(String fileName, long fileSize, File file) throws IOException {
    model.sendFile(fileName, fileSize, file);
  }
}
