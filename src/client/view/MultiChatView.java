package client.view;

import client.controller.Feature;
import java.awt.Color;
import java.util.List;

public interface MultiChatView {

  String getName(String prompt);

  void giveFeatures(Feature feature);

  void setTextFieldEditable(boolean b);

  void display();

  void appendChatLog(String s, String color, boolean hasDate);

  void setTitle(String s);

  void setActiveUsers(List<String> names);

  void dispose();

  void setActiveServers(List<String> servers);
}
