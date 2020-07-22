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
  private Map<String, String> emotes;


  public MultiChatModelImpl(String ipAddress, int portNum) throws IOException {
    this.ipAddress = ipAddress;
    socket = new Socket(this.ipAddress, portNum);
    in = new Scanner(socket.getInputStream());
    out = new PrintWriter(socket.getOutputStream(), true);

    emotes = new HashMap<>();
    emotes.put("&lt;3", "heart_emoji.png");
    emotes.put(":\\)", "smiley.png");
    emotes.put(":\\(", "frowny.png");
    emotes.put(":/", "confused.png");
    emotes.put(":D", "excited.png");
    emotes.put("D:", "anguish.png");
    emotes.put(":p", "tongue.png");
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

  @Override
  public Map<String, String> getEmotes() {
    return emotes;
  }
}
