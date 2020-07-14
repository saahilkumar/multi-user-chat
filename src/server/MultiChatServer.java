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

public class MultiChatServer {

  private static Set<String> names = new HashSet<>();
  private static Set<PrintWriter> outputs = new HashSet<>();

  public static void main(String[] args) throws IOException {
    System.out.println("MultiChat Server is now running!");

    ExecutorService pool = Executors.newFixedThreadPool(25);

    try(ServerSocket server = new ServerSocket(59090)) {
      while(true) {
        pool.execute(new Task(server.accept()));
      }
    }
  }

  private static class Task implements Runnable {
    private Socket clientSocket;
    private String name;
    private Scanner in;
    private PrintWriter out;

    public Task(Socket socket) {
      clientSocket = socket;
      try {
        in = new Scanner(clientSocket.getInputStream());
        out = new PrintWriter(clientSocket.getOutputStream(), true);
      } catch(IOException ioe) {
        System.out.println("Client input/output failed to connect: " + clientSocket);
      }
    }

    @Override
    public void run() {

      String submitNameProtocol = "SUBMITNAME";
      while(true) {
        out.println(submitNameProtocol);
        name = in.nextLine();

        if(name == null) {
          userLeaves();
          return;
        }
        synchronized (names) {
          if(!name.isBlank() && !names.contains(name)) {
            names.add(name);
            break;
          }
        }

        submitNameProtocol = "NAMETAKEN";
      }

      out.println("NAMEACCEPTED " + name);
      for(PrintWriter output : outputs) {
        output.println("MESSAGEUSERJOINED " + name + " has joined!");
      }
      outputs.add(out);

      // messages
      while(true) {
        String msg = in.nextLine();

        if (msg.toLowerCase().startsWith("/quit")) {
          userLeaves();
          return;
        }

        for(PrintWriter output : outputs) {
          output.println("MESSAGE " + name + ": " + msg);
        }

      }
    }

    private void userLeaves() {
      if (out != null) {
        outputs.remove(out);
      }
      if (name != null) {
        System.out.println(name + " is leaving");
        names.remove(name);
        for (PrintWriter output : outputs) {
          output.println("MESSAGE " + name + " has left");
        }
      }
      try {
        clientSocket.close();
      } catch (IOException e) {
        out.println("Unable to close socket: " + clientSocket);
      }
    }
  }
}
