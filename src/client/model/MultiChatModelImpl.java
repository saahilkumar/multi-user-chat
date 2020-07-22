package client.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MultiChatModelImpl implements MultiChatModel {

  private String ipAddress;
  private Socket socket;
  private Scanner in;
  private PrintWriter out;

  public MultiChatModelImpl(String ipAddress, int portNum) throws IOException {
    this.ipAddress = ipAddress;
    socket = new Socket(this.ipAddress, portNum);
    in = new Scanner(socket.getInputStream());
    out = new PrintWriter(socket.getOutputStream(), true);
  }

  @Override
  public boolean isConnectionRunning() {
    return in.hasNextLine();
  }

  @Override
  public String getSocketInput() {
    return in.nextLine();
  }

  @Override
  public void sendText(String output) {
    out.println(output);
  }

  @Override
  public MultiChatModelImpl switchPorts(String portNumber) throws IOException,
      NumberFormatException {
    return new MultiChatModelImpl(this.ipAddress, Integer.parseInt(portNumber));
  }
}
