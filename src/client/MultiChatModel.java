package client;

public interface MultiChatModel {
  boolean isConnectionRunning();

  String getSocketInput();

  void sendText(String output);
}
