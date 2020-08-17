package client.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public interface Feature {
  void sendTextOut(String out);

  String getClientUsername();

  void sendFile(String fileName, long fileSize, File file) throws IOException;
}
