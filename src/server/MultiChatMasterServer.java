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

public class MultiChatMasterServer {

  private static Set<String> activeServers = new HashSet<>();
  private static Set<PrintWriter> outputWriters = new HashSet<>();

  public static void main(String[] args) throws IOException {
    System.out.println("MultiChat Master Server is running...");
    ExecutorService pool = Executors.newFixedThreadPool(10);
    try (ServerSocket server = new ServerSocket(50000)) {
      while (true) {
        pool.execute(new ServerCommunicationHandler(server.accept()));
      }
    }
  }

  private static class ServerCommunicationHandler implements Runnable {
    private Socket multiChatServerClientSocket;
    private Scanner in; //the input of the server
    private PrintWriter out; //the output to the server
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

    private void checkServerRunning() throws IOException {
      while(true) {
        if (multiChatServerClientSocket.getInputStream().read() == -1) {
          outputWriters.remove(out);
          activeServers.remove(serverPortName);
          updateServerList();
          multiChatServerClientSocket.close();
          return;
        }
      }
    }

    //Wraps the clients input and outputs streams into a Scanner and PrintWriter respectively.
    private void wrapClientIO() throws IOException {
      in = new Scanner(multiChatServerClientSocket.getInputStream());
      out = new PrintWriter(multiChatServerClientSocket.getOutputStream(), true);
    }

    private void addServer() {
      synchronized(activeServers) {
        String input = in.nextLine();
        activeServers.add(input);
        serverPortName = input;
      }
      outputWriters.add(out);
      updateServerList();
    }

    private void updateServerList() {
      for (PrintWriter output : outputWriters) {
        StringBuilder serverListBuilder = new StringBuilder();
        serverListBuilder.append("ACTIVESERVERLIST ");
        for (String server : activeServers) {
          serverListBuilder.append(server + ", ");
        }
        output.println(serverListBuilder.toString());
        System.out.print(serverListBuilder.toString());
      }
    }
  }

}