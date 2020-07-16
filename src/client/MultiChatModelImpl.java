package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MultiChatModelImpl implements MultiChatModel {

  private String ipAddress;
  private Socket socket;
  private Scanner in;
  private PrintWriter out;


  public MultiChatModelImpl(String ipAddress) throws IOException {
    this.ipAddress = ipAddress;
    socket = new Socket(this.ipAddress, 59090);
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
}
