package client.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public interface MultiChatModel {
  boolean isConnectionRunning();

  String getSocketInput();

  void sendText(String output);

  MultiChatModel switchPorts(String portNumber) throws IOException;

  void setUsername(String username);

  String getUsername();

  void sendFile(String fileName, long fileSize, File file) throws IOException;

  void saveFile(File file, long fileSize);
}
