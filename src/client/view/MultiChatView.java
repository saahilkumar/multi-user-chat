package client.view;

import client.controller.Feature;
import java.util.List;
import java.util.Map;

// CHANGE LOG
// ADDED <pre> to appendChatLog and setActiveUsers/Servers
// Removes <br> from everywhere in ViewImpl and \n from everywhere in controllerImpl

import static java.util.Map.entry;

public interface MultiChatView {

  Map<String, String> EMOTES = Map.ofEntries(
      entry("&lt;3", "heart.png"),
      entry(":)", "smiley.png"),
      entry(":(", "frowny.png"),
      entry(":/", "confused.png"),
      entry(":D", "excited.png"),
      entry("D:", "anguish.png"),
      entry(":p", "tongue.png"),
      entry("&gt;:(", "angry.png"),
      entry(":O", "wow.png"),
      entry(":|", "neutral.png"),
      entry(";)", "wink.png")
  );

  Map<String, String> TWITCH_EMOTES = Map.ofEntries(
      entry("PepeHands", "PepeHands.png"),
      entry("Pepega", "Pepega.png"),
      entry("Kappa", "Kappa.png"),
      entry("forsenCD", "forsenCD.jpg"),
      entry("pepeD", "pepeD.gif"),
      entry("pepoG", "pepoG.png"),
      entry("WeirdChamp", "WeirdChamp.png"),
      entry("widepeepohappy", "widepeepohappy.png"),
      entry("FeelsBadMan", "FeelsBadMan.png"),
      entry("FeelsGoodMan", "FeelsGoodMan.png"),
      entry("Pog", "Pog.png"),
      entry("OMEGALUL", "OMEGALUL.png"),
      entry("KEKW", "KEKW.png"),
      entry("monkaHmm", "monkaHmm.png"),
      entry("monkaGun", "monkaGun.png"),
      entry("5Head", "5Head.png"),
      entry("PepeLaugh", "PepeLaugh.png"),
      entry("POGGERS", "POGGERS.png"),
      entry("ratirlCoffee", "ratirlCoffee.png")
  );

  String getName(String prompt);

  void giveFeatures(Feature feature);

  void setTextFieldEditable(boolean b);

  void display();

  void appendChatLog(String s, String color, boolean hasDate);

  void setActiveUsers(List<String> names);

  void dispose();

  void setActiveServers(List<String> servers);
}
