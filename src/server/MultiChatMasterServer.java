package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A master server that keeps track of active MultiChat servers and updates the
 * list of active servers every time there is a change to the list.
 */
public class MultiChatMasterServer {

  // list of active MultiChat servers running and connected to this server
  private static Set<String> activeServers = new HashSet<>();

  // the set of writers of the output streams to every MultiChat server
  private static Set<PrintWriter> outputWriters = new HashSet<>();

  /**
   * Runs this master server and runs a function object for every server connection to this master server.
   * Every server connected to this master server is added to the list of servers connected to it.
   *
   * @param args command line arguments, first argument describes the amount of active MultiChat
   *             servers that can possibly run at one time
   * @throws IOException throws when a connection to a MultiChatServer's socket fails
   */
  public static void main(String[] args) throws IOException {
    System.out.println("MultiChat Master Server is running...");
    ExecutorService pool = Executors.newFixedThreadPool(10);
    try (ServerSocket server = new ServerSocket(50000)) {
      while (true) {
        pool.execute(new ServerCommunicationHandler(server.accept()));
      }
    }
  }

  // a function object that stores the task that every thread runs for each new server connection
  private static class ServerCommunicationHandler implements Runnable {
    private Socket multiChatServerClientSocket;
    private Scanner in; // the input of the server
    private PrintWriter out; // the output to the server
    private String serverPortName;

    private ServerCommunicationHandler(Socket multiChatServerClientSocket) {
      this.multiChatServerClientSocket = multiChatServerClientSocket;
    }

    @Override
    public void run() {
      try {
        wrapClientIO();
        addServer();
        checkServerRunning();
      } catch (IOException ioe) {
        System.out.println("Server input/output failed to connect: " +
            multiChatServerClientSocket.toString());
      }
    }

    // checks whether the server is running, if it is not then it removes the server
    // from the list of active servers
    private void checkServerRunning() throws IOException {
      while(true) {
        try {
          multiChatServerClientSocket.getInputStream().read();
        } catch (IOException ioe) {
          outputWriters.remove(out);
          activeServers.remove(serverPortName);
          multiChatServerClientSocket.close();
          updateServerList();
          return;
        }
      }
    }

    // Wraps the clients input and outputs streams into a Scanner and PrintWriter respectively.
    private void wrapClientIO() throws IOException {
      in = new Scanner(multiChatServerClientSocket.getInputStream());
      out = new PrintWriter(multiChatServerClientSocket.getOutputStream(), true);
    }

    // adds a server to the active server list
    private void addServer() {
      synchronized(activeServers) {
        String input = in.nextLine();
        activeServers.add(input);
        serverPortName = input;
      }
      outputWriters.add(out);
      updateServerList();
    }

    // writes to all the active servers an updated version of the list
    private void updateServerList() {
      for (PrintWriter output : outputWriters) {
        StringBuilder serverListBuilder = new StringBuilder();
        serverListBuilder.append("ACTIVESERVERLIST ");
        for (String server : activeServers) {
          serverListBuilder.append(server + ", ");
        }
        output.println(serverListBuilder.toString());
        System.out.println(serverListBuilder.toString());
      }
    }
  }

}