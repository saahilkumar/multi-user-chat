package client;

import java.awt.Color;

public interface MultiChatView {

  String getName();

  void giveFeatures(Feature feature);

  void setTextFieldEditable(boolean b);

  void display();

  void appendChatLog(String s, String color);

  void setTitle(String s);
}
