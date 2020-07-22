package client.view;

import client.controller.Feature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public interface MultiChatView {

  Map<String, String> EMOTES = Map.ofEntries(
      entry("&lt;3", "heart.png"),
      entry(":)", "smiley.png"),
      entry(":(", "frowny.png"),
      entry(":/", "confused.png"),
      entry(":D", "excited.png"),
      entry("D:", "anguish.png"),
      entry(":p", "tongue.png")
  );

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
