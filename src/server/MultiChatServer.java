package server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.Buffer;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 * Represents a simple-text server handling the input and output to several clients (30).
 */
public class MultiChatServer {

  //holds the names of active clients
//  private static HashSet<String> names = new HashSet<>();
  private static Map<String, Task> users = new HashMap<>();

  //a set of writers that write to the output of a client's socket
  private static HashSet<PrintWriter> outputWriters = new HashSet<>();

  private static HashSet<String> serverNames = new HashSet();

  private static int possibleAmountOfClients;

  private static String curVictim = null;
  private static int numVotes = 0;
  private static Timer kickTimer = new Timer();
  private static Set alreadyVoted = new HashSet<>();

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
  public static void main(String[] args) throws IllegalArgumentException,
      NumberFormatException {
    if (args.length == 2) {
      possibleAmountOfClients = Integer.parseInt(args[1]);
    } else if (args.length == 1) {
      possibleAmountOfClients = 30;
    } else {
      throw new IllegalArgumentException("Supplied more than 2 argument. Please enter zero or one "
          + "integer only for number of desired clients.");
    }
    System.out.println("MultiChat Server is running...");

    new Thread(() -> {
      Scanner input = new Scanner(System.in);
      while(input.hasNextLine()) {
        String command = input.nextLine();
        switch (command) {
          case "exit":
            System.out.println("good");

            for(PrintWriter writer : outputWriters) {
              writer.println("SERVERCLOSE ");
            }
            //send to all users its closing
            //maybe a timer?
            break;
          case "users":
            //get list of all users
            break;
          case "servers":
            //get list of all servers
            break;
          default:
            System.out.println("Invalid command.");
        }
      }
      //delete user files
    }).start();

    Thread masterServerCommunication = new Thread(new RunServerCommunication(args[0]));
    masterServerCommunication.start();

    ExecutorService pool = Executors.newFixedThreadPool(possibleAmountOfClients);
    try {
      SSLServerSocket server = initSSLDetailsAndGetSocket(args[0]);
      while (true) {
        pool.execute(new Task((SSLSocket)server.accept(), args[0]));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static SSLServerSocket initSSLDetailsAndGetSocket(String portNumber)
      throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException,
      UnrecoverableKeyException, KeyManagementException {

    SSLServerSocketFactory ssf;

    // set up key manager to do server authentication
    SSLContext ctx;
    KeyManagerFactory kmf;
    KeyStore ks;
    char[] passphrase = "socketpractice".toCharArray();

    ctx = SSLContext.getInstance("TLS");
    kmf = KeyManagerFactory.getInstance("SunX509");
    ks = KeyStore.getInstance("JKS");

    ks.load(MultiChatServer.class.getClassLoader().getResourceAsStream(
        "server/resources/keystore/server_keystore.jks"), passphrase);
    kmf.init(ks, passphrase);

    ctx.init(kmf.getKeyManagers(), null, null);

    ssf = ctx.getServerSocketFactory();
    return (SSLServerSocket) ssf.createServerSocket(Integer.parseInt(portNumber));
  }

  private static class RunServerCommunication implements Runnable {

    private String portNumber;

    private RunServerCommunication(String portNumber) {
      this.portNumber = portNumber;
    }

    @Override
    public void run() {
      try {
        Socket socketToMasterServer = new Socket("localhost", 50000);
        Scanner serverIn = new Scanner(socketToMasterServer.getInputStream());
        PrintWriter serverOut = new PrintWriter(socketToMasterServer.getOutputStream(), true);
        serverOut.println("Server " + portNumber);
        while(serverIn.hasNextLine()) {
          String serverList = serverIn.nextLine();
          serverList = serverList.substring(17);
          String[] activeServers = serverList.split(", ");
          serverNames.clear();
          for (String server : activeServers) {
            serverNames.add(server);
          }
          System.out.println(serverList);
          updateServerList();
        }
        socketToMasterServer.close();
        System.exit(3);
      } catch (IOException e) {
        e.printStackTrace();
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
    private final SSLSocket clientSocket; //the socket of the client connection
    private Scanner in; //the input of the client
    private PrintWriter out; //the output to the client
    private String portNumber;

    //Captures the client's socket as a field.
    private Task(SSLSocket clientSocket, String portNumber) {
      this.clientSocket = clientSocket;
      this.portNumber = portNumber;
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
        synchronized (users) {
          if (!name.isBlank() && !users.keySet().contains(name) && !name.contains(",") && !name.contains(":")) {
            users.put(name, this);
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
      out.println("MESSAGEWELCOME Welcome to Multi-Chat, room "+ portNumber + ", "
          + name + ". Use /help for help!");
      for (PrintWriter writer : outputWriters) {
        writer.println("MESSAGEUSERJOINED " + "[" + new Date().toString() + "] " +
            name + " has joined.");
      }
      outputWriters.add(out);
      updateActiveUsers();
      updateServerList();
      new File("resources/tempFiles/" + name).mkdirs();
      System.out.println("[" + new Date().toString() + "] " + name + " has joined.");
    }

    //transmits user messages to other clients, handles user command requests as a well
    private void handleUserInput() {
      while (true) {
        String input = in.nextLine();
        if (input.toLowerCase().startsWith("/quit")) {
          userLeave();
          return;
        } else if (input.toLowerCase().startsWith("/help")) {
          printHelpMessage();
        } else if (input.toLowerCase().startsWith("/emotes")) {
          printEmoteHelpMessage();
        } else if (input.toLowerCase().startsWith("/join ")) {
          out.println("REQUESTEDNEWROOM " + input.substring(6));
        } else if (input.startsWith("UNSUCCESSFULROOMCHANGE ")) {
          out.println("MESSAGEHELP " + input.substring(23));
        } else if(input.toLowerCase().startsWith("/votekick ")) {
          printVotekickMessage(input.substring(10));
        } else if(input.toLowerCase().startsWith("/whisper ")) {
          String receiver = input.substring(9, input.indexOf(":"));
          String msg = input.substring(input.indexOf(":") + 1);
          printWhisper(receiver, msg);
        } else if(input.toLowerCase().startsWith("/privatemsg ")) {
          String[] components = input.split(": ");
          String sender = components[0].substring(12);
          String receiver = components[1];
          String inputWithoutDate = input.substring(12);
          String messageAndReceiver = inputWithoutDate.substring(inputWithoutDate.indexOf(": ") + 2);
          String message = messageAndReceiver.substring(messageAndReceiver.indexOf(": ") + 2);
          printPrivMsg(sender, receiver, message);
        } else if(input.toLowerCase().startsWith("/file ")) {
          String fileName = input.substring(6, input.lastIndexOf(":"));
          Long fileSize = Long.parseLong(input.substring(input.lastIndexOf(":") + 1));
          System.out.println("Receiving file: " + fileName);
          outputFile(fileName, fileSize);
        } else if(input.toLowerCase().startsWith("/privatefile ")) {
          String fileReceiver = input.substring(13, input.indexOf(":"));
          String fileName = input.substring(input.indexOf(":") + 1, input.lastIndexOf(":"));
          Long fileSize = Long.parseLong(input.substring(input.lastIndexOf(":") + 1));
          System.out.println("Receiving private file: " + fileName);
          outputPrivateFile(fileName, fileSize, fileReceiver);
        } else if(input.toLowerCase().startsWith("/requestfile ")) {
          try {
            String fileOwner = input.substring(13, input.indexOf(":"));
            String fileName = input.substring(input.indexOf(":") + 1);
            System.out.println(fileOwner);
            System.out.println(fileName);
            File requested = new File("resources/tempFiles/"+fileOwner+"/"+fileName);
            long fileSize = requested.length();
            byte[] buffer = new byte[4096];
            FileInputStream fis = new FileInputStream(requested);
            BufferedInputStream bis = new BufferedInputStream(fis);
            System.out.println("requested = " + requested);
            int amountRead;
            out.println("FILEDATA " + fileSize + ":" + fileName);
            while ((amountRead = bis.read(buffer, 0, buffer.length)) > -1) {
              clientSocket.getOutputStream().write(buffer, 0,  amountRead);
            }
            clientSocket.getOutputStream().flush();
            fis.close();
            bis.close();
          } catch(IOException ioe) {
            out.append("FAILEDFILETRANSFER Error fetching file");
          }
        } else {
          for (PrintWriter writer : outputWriters) {
            writer.println("MESSAGE " + "[" + new Date().toString() + "] " + name + ": " +input);
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
        users.remove(name);
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
      out.println("MESSAGEHELP Type /quit to quit MultiChat.");
      out.println("MESSAGEHELP Type /emotes to access a menu of emoticons.");
      out.println("MESSAGEHELP Type /join to join another chat room, "
          + "enter the room number such like: \"/join 59090\".");
      out.println("MESSAGEHELP Type /help to access this help menu.");
    }

    private void printEmoteHelpMessage() {
      out.println("MESSAGEHELP Emotes menu: ");
      out.println("MESSAGEHELP Smiley Face :) : \":)\"");
      out.println("MESSAGEHELP Frowny Face :( : \":(\"");
      out.println("MESSAGEHELP Ambivalent Face :/ : \":/\"");
      out.println("MESSAGEHELP Excited :D : \":D\"");
      out.println("MESSAGEHELP Despair D: : \"D:\"");
      out.println("MESSAGEHELP Quirky :p :  \":p\"");
      out.println("MESSAGEHELP Pepega( Pepega ) : \"Pepega\"");
      out.println("MESSAGEHELP Pepehands( Pepehands ): \"Pepehands\"");
    }

    private void printVotekickMessage(String victim) {
      if(alreadyVoted.contains(name)) {
        out.println("FAILEDVOTEKICK You already voted to kick " + victim + "!");
        return;
      }

      if(victim.equals(name)) {
        out.println("FAILEDVOTEKICK You can't kick yourself!");
        return;
      }

      if(!users.keySet().contains(victim)) {
        out.println("FAILEDVOTEKICK There is no one here named " + victim);
        return;
      }

      if(curVictim != null & !victim.equals(curVictim)) {
        out.println("FAILEDVOTEKICK You cannot kick " + victim + " because someone else is currently being voted on.");
        return;
      }

      // if this is the first vote for someone, then start the votekick
      if(curVictim == null) {
        for(PrintWriter writer : outputWriters) {
          if(writer.equals(out)) {
            writer.println("VOTEKICK You have started a votekick for " + victim + "!");
          }
          else {
            writer.println("VOTEKICK Someone has started a votekick for " + victim + "!");
          }
        }
        numVotes = 1;
        curVictim = victim;

        // after ten seconds, the votekick ends
        // ask david to clean this up lol, idk how lambdas work
        kickTimer = new Timer();
        kickTimer.schedule(
            new java.util.TimerTask() {
              @Override
              public void run() {
                for(PrintWriter writer : outputWriters) {
                  writer.println("FAILEDVOTEKICK The votekick for " + victim + " has run out of time!");
                }
                curVictim = null;
                numVotes = 0;
                alreadyVoted.clear();
                cancel();
              }
            },
            20000
        );

      } else {
        numVotes++;
        out.println("VOTEKICK You have voted to kick " + victim + "!");

        // if the majority voted to kick
        if(numVotes > users.size() / 2) {
          kickUser();
          return;
        }
      }

      alreadyVoted.add(name);

    }

    private void kickUser() {
      for(PrintWriter writer : outputWriters) {
        writer.println("SUCCESSFULVOTEKICK " + curVictim + " was kicked!");
      }

      try {
        users.get(curVictim).clientSocket.close();
        users.remove(curVictim);
        updateActiveUsers();
      } catch (IOException ioe) {
        System.out.println(ioe.getMessage());
      } finally {
        // reset values
        numVotes = 0;
        curVictim = null;

        // cancel the timer
        kickTimer.cancel();

        alreadyVoted.clear();
      }
    }

    private void printWhisper(String receiver, String msg) {
      if(!users.containsKey(receiver)) {
        out.println("FAILEDWHISPER There is no user named " + receiver + " to whisper to!");
        return;
      }
      users.get(receiver).out.println("WHISPER " + "[" + new Date().toString() + "] " + name + ": " + msg);
      out.println("WHISPER " + "[" + new Date().toString() + "] " + name + ": " + msg);
    }

    private void printPrivMsg(String sender, String receiver, String msg) {
      out.println("PRIVATEMESSAGE " + "[" + new Date().toString() + "] " +
          sender + ": " + receiver + ": " + msg);
      users.get(receiver).out.println("PRIVATEMESSAGE " + "[" + new Date().toString() + "] " +
          sender + ": " + receiver + ": " + msg);
    }

    private void outputFile(String fileName, long fileSize) {
      try {
        byte[] buf = new byte[4096];
        File file = new File("resources/tempFiles/" + name + "/" + fileName);
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file, false);
        while(fileSize > 0 && clientSocket.getInputStream().read(buf, 0, (int)Math.min(buf.length, fileSize)) > -1) {
          fos.write(buf, 0, buf.length);
          fos.flush();
          fileSize -= buf.length;
        }
        fos.close();

        for(PrintWriter writer : outputWriters) {
          writer.println("FILE " + "[" + new Date() + "] " + name + ": " + fileName);
        }

      } catch(FileNotFoundException fnfe) {
        out.println("FAILEDFILETRANSFER Improper file name");
      } catch(IOException ioe) {
        out.println("FAILEDFILETRANSFER Error communicating with server");
      }
    }

    private void outputPrivateFile(String fileName, long fileSize, String reciever) {
      try {
        byte[] buf = new byte[4096];
        File file = new File("resources/tempFiles/" + name + "/" + fileName);
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file, false);
        while(fileSize > 0 && clientSocket.getInputStream().read(buf, 0, (int)Math.min(buf.length, fileSize)) > -1) {
          fos.write(buf, 0, buf.length);
          fos.flush();
          fileSize -= buf.length;
        }
        fos.close();

        users.get(reciever).out.println("PRIVATEFILE " + "[" + new Date() + "] " + name + ": " + reciever + ": " + fileName);
        out.println("PRIVATEFILE " + "[" + new Date() + "] " + name + ": " + reciever + ": " + fileName);

//        for(PrintWriter writer : outputWriters) {
//          writer.println("FILE " + "[" + new Date() + "] " + name + ": " + fileName);
//        }

      } catch(FileNotFoundException fnfe) {
        out.println("FAILEDFILETRANSFER Improper file name");
      } catch(IOException ioe) {
        out.println("FAILEDFILETRANSFER Error communicating with server");
      }
    }


    private void updateActiveUsers() {
      for (PrintWriter writer : outputWriters) {
        StringBuilder activeUserList = new StringBuilder();
        activeUserList.append("ACTIVEUSERLIST ");
        for (String name : users.keySet()) {
          activeUserList.append(name + ",");
        }
        writer.println(activeUserList.toString());
      }
    }
  }

  private static void updateServerList() {
    for (PrintWriter out : outputWriters) {
      StringBuilder serverList = new StringBuilder();
      serverList.append("ACTIVESERVERLIST ");
      for (String serverName : serverNames) {
        serverList.append(serverName + ",");
      }
      out.println(serverList.toString());
      System.out.println(serverList.toString());
    }
  }
}