package server;

import java.util.HashSet;
import java.util.Set;

public class MultiChatMasterServer {

  private Set<MultiChatServer> servers = new HashSet<>();


  public void addServer(MultiChatServer server) {
    servers.add(server);
  }
}
