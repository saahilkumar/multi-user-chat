package client.controller;

import java.util.Map;

public interface Feature {
  void sendTextOut(String out);

  Map<String, String> getModelEmotes();
}
