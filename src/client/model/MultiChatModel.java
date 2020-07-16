package client.model;

public interface MultiChatModel {
  boolean isConnectionRunning();

  String getSocketInput();

  void sendText(String output);
}
