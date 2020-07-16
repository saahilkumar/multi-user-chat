package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Represents a simple-text server handling the input and output to several clients (30).
 */
public class MultiChatServer {

  //holds the names of active clients
  private static HashSet<String> names = new HashSet<>();

  //a set of writers that write to the output of a client's socket
  private static HashSet<PrintWriter> outputWriters = new HashSet<>();

  /**
   * Main method to start the MultiChat server and listens for 1<=x<=args connections on local port
   * 59090 where args is the desired number of possible clients (default 30). Creating a thread for
   * each connection and creating and running a Task for each connection.
   *
   * @param args command line arguments describing the desired number of clients
   * @throws IOException when a creation of ServerSocket fails and/or the listening for
   * a connection to the ServerSocket has failed.
   * @throws IllegalArgumentException when supplied more than one argument
   * @throws NumberFormatException when given a non-integer argument.
   */
  public static void main(String[] args) throws IOException, IllegalArgumentException,
      NumberFormatException {
    int possibleAmountOfClients;
    if (args.length == 1) {
      possibleAmountOfClients = Integer.parseInt(args[0]);
    } else if (args.length == 0) {
      possibleAmountOfClients = 30;
    } else {
      throw new IllegalArgumentException("Supplied more than 1 argument. Please enter zero or one "
          + "integer only for number of desired clients.");
    }
    System.out.println("MultiChat Server is running...");
    ExecutorService pool = Executors.newFixedThreadPool(possibleAmountOfClients);
    try (ServerSocket server = new ServerSocket(59090)) {
      while (true) {
        pool.execute(new Task(server.accept()));
      }
    }
  }

  /*
  Represents a "Task" that is run for every client connected to the server per thread.
  Implements Runnable, captures the Socket that is connected and wraps the input and output
  and handles the text input/output of the client.
   */
  private static class Task implements Runnable {

    private String name; //name of client
    private final Socket clientSocket; //the socket of the client connection
    private Scanner in; //the input of the client
    private PrintWriter out; //the output to the client

    //Captures the client's socket as a field.
    private Task(Socket clientSocket) {
      this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
      try {
        wrapClientIO();
        requestUsername();
        acceptAndProcessUsername();
        handleUserInput();
      } catch (IOException ioe) {
        System.out.println("Client input/output failed to connect: " + clientSocket.toString());
      } catch (IllegalArgumentException | NoSuchElementException iae) {
        System.out.println(iae.getMessage());
      }
    }

    //Wraps the clients input and outputs streams into a Scanner and PrintWriter respectively.
    private void wrapClientIO() throws IOException {
      in = new Scanner(clientSocket.getInputStream());
      out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    /*
    Requests the username from the client, sending SUBMITNAME. If given a taken or invalid
    username, will send SUBMITANOTHERNAME repeatedly until a valid one has been submit. When a valid
    username has been submitted, it will update the names of the active clients list of the server.
     */
    private void requestUsername() throws IllegalArgumentException, NoSuchElementException {
      String submitNameProtocol = "SUBMITNAME";
      while (true) {
        out.println(submitNameProtocol);
        name = in.nextLine();
        if (name == null) {
          userLeave();
          throw new IllegalArgumentException("Supplied a null name.");
        }
        synchronized (names) {
          if (!name.isBlank() && !names.contains(name)) {
            names.add(name);
            break;
          }
        }
        submitNameProtocol = "SUBMITANOTHERNAME";
      }
    }

    /*
    Tells client that name has been accepted and sends a message to the rest of the clients that
    a new user has joined. Then adds the client's output to the list of client output printwriters.
     */
    private void acceptAndProcessUsername() {
      out.println("NAMEACCEPTED " + name);
      out.println("MESSAGEWELCOME Welcome to MultiChat " + name + "! Use /help if you need any assistance!");
      for (PrintWriter writer : outputWriters) {
        writer.println("MESSAGEUSERJOINED " + "[" + new Date().toString() + "] " +
            name + " has joined.");
      }
      outputWriters.add(out);
      System.out.println("[" + new Date().toString() + "] " + name + " has joined.");

      updateActiveUsers();
    }

    //transmits user messages to other clients, handles user command requests as a well
    private void handleUserInput() {
      while (true) {
        String input = in.nextLine();
        if (input.toLowerCase().startsWith("/quit")) {
          userLeave();
          return;
        }
        if (input.toLowerCase().startsWith("/help")) {
          printHelpMessage();
        } else if (input.toLowerCase().startsWith("/emotes")) {
          //TODO: add emote handling
        } else {
          for (PrintWriter writer : outputWriters) {
            writer.println("MESSAGE " + "[" + new Date().toString() + "] " + name + ": " + input);
          }
        }
      }
    }

    //handles when a client leaves the chatroom
    private void userLeave() {
      if (out != null) {
        outputWriters.remove(out);
      }
      if (name != null) {
        System.out.println("[" + new Date().toString() + "] " + name + " has left.");
        names.remove(name);
        for (PrintWriter writer : outputWriters) {
          writer.println("MESSAGEUSERLEFT " + "[" + new Date().toString() + "] " + name
              + " has left");
        }
        out.println("Successfully left.");
        updateActiveUsers();
      }
      try {
        clientSocket.close();
      } catch (IOException e) {
        System.out.print("Failure to close client socket: " + clientSocket.toString());
      }
    }

    //prints a help menu with commands to use in MultiChat.
    private void printHelpMessage() {
      out.println("Type /quit to quit MultiChat.");
      out.println("Type /emotes to access a menu of emoticons.");
      out.println("Type /help to access this help menu.");
    }

    private void updateActiveUsers() {
      for(PrintWriter writer : outputWriters) {
        StringBuilder builder = new StringBuilder();
        builder.append("ACTIVEUSERLIST ");
        for(String user : names) {
          builder.append(user + ",");
        }
        writer.println(builder.toString());
      }
    }
  }
}


