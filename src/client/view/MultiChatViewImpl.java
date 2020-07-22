package client.view;

import client.controller.Feature;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.FocusListener;
import server.MultiChatServer;

public class MultiChatViewImpl extends JFrame implements MultiChatView {
  private JTextPane chatLog;
  private JTextPane activeUsers;
  private JTextPane activeServers;
  private JTextArea chatField;
  private Feature feature;
  private StringBuilder log;
  private CountDownLatch latch;

  public MultiChatViewImpl() {
    this.setLayout(new FlowLayout());
    CenterPanel center = new CenterPanel();

    log = new StringBuilder();

    activeUsers = new JTextPane();
    activeUsers.setEditable(false);
    activeUsers.setContentType("text/html");
    activeUsers.setText("<h3>Active Users:</h3>");
    activeUsers.setAutoscrolls(true);
    activeUsers.setPreferredSize(new Dimension(100, 375));
    this.add(new JScrollPane(activeUsers));

    this.add(center);

    activeServers = new JTextPane();
    activeServers.setEditable(false);
    activeServers.setContentType("text/html");
    activeServers.setText("<h3>Active Servers:</h3>");
    activeServers.setPreferredSize(new Dimension(100, 375));
    this.add(new JScrollPane(activeServers));



    this.pack();

    this.setResizable(false);

//    emotes = new HashMap<>();
//    emotes.put("&lt;3", "heart_emoji.png");
//    emotes.put(":\\)", "smiley.png");
//    emotes.put(":\\(", "frowny.png");
//    emotes.put(":/", "confused.png");
//    emotes.put(":D", "excited.png");
//    emotes.put("D:", "anguish.png");
//    emotes.put(":p", "tongue.png");
  }

  @Override
  public String getName(String prompt) {
    ScreenNameSelection namePane = new ScreenNameSelection(prompt);
    this.setVisible(false);

    latch = new CountDownLatch(1);
    try {
      latch.await();
    } catch(InterruptedException ie) {
      System.out.println("oof:" + ie.getMessage());
    }

    this.setTitle("MultiChat - " + namePane.getInput());
    return removeHtml(namePane.getInput());
  }

  private String removeHtml(String str) {
    str = str.replaceAll("<", "&lt;");
    str = str.replaceAll(">", "&gt;");
    return str;
  }

  @Override
  public void setTextFieldEditable(boolean b) {
    chatField.setEditable(b);
//    chatField.getDocument().addDocumentListener(new TextAreaListener());
  }

  @Override
  public void giveFeatures(Feature feature) {
    this.feature = feature;
  }

  @Override
  public void display() {
    // if you close the window, it tells the server that you're saying /quit
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent event) {
        feature.sendTextOut("/quit");
        System.exit(3);
      }
    });

    this.setVisible(true);
  }

  @Override
  public void appendChatLog(String s, String color, boolean hasDate) {
//    String currentLog = this.chatLog.getText();
//    this.chatLog.setText(currentLog + s + "\n");
    if(hasDate) {
      s = formatDate(s);
    }
    s = convertEmote(s);
    String toAdd = "<span style=\"color:"+ color +"\">" + s + " </span><br>";
//    toAdd = convertEmote(toAdd);
    log.append(toAdd);
    chatLog.setText(log.toString());
    chatLog.setCaretPosition(chatLog.getDocument().getLength());
  }

  private String convertEmote(String msg) {
    StringBuilder builder = new StringBuilder();

    // split message by space
    String[] words = msg.split(" ");

    for(String word : words) {
      // if the word equals an emoji name (ex. <3) then replace it with html image code
      if(MultiChatView.EMOTES.containsKey(word.trim())) {
        builder.append("<img src = \"" + MultiChatServer.class.getClassLoader()
              .getResource("resources/images/" + MultiChatView.EMOTES.get(word.trim())).toString() + "\"" +
              " alt = \"error\" width = \"20\" height = \"20\">");
      } else if(MultiChatView.TWITCH_EMOTES.containsKey(word.trim())) {
        builder.append("<img src = \"" + MultiChatServer.class.getClassLoader()
            .getResource("resources/images/" + MultiChatView.TWITCH_EMOTES.get(word.trim())).toString() + "\"" +
            " alt = \"error\" width = \"40\" height = \"40\">");
      } else {
        builder.append(word);
      }

      // add the space back in (got removed when splitting)
      builder.append(" ");
    }

    return builder.toString();
  }

  private String formatDate(String msg) {
    String date = msg.substring(0, msg.indexOf("]") + 1);
    String[] dateComponents = date.split(" ");
    String month = dateComponents[1];
    String day = dateComponents[2];
    String time = dateComponents[3];
    String timeZone = dateComponents[4];

    StringBuilder builder = new StringBuilder();
    builder.append("[");
    builder.append(month);
    builder.append(" ");
    builder.append(day);
    builder.append(" ");
    builder.append(time);
    builder.append(" ");
    builder.append(timeZone);
    builder.append("]");
    builder.append(msg.substring(msg.indexOf("]") + 1));
    return builder.toString();
  }

  @Override
  public void setActiveUsers(List<String> names) {
    StringBuilder builder = new StringBuilder();
    builder.append("<h3>Active Users:</h3>");
    for(String name : names) {
      builder.append(name + "<br>");
    }
    activeUsers.setText(builder.toString());
  }

  @Override
  public void setActiveServers(List<String> servers) {
    StringBuilder builder = new StringBuilder();
    builder.append("<h3>Active Servers:</h3>");
    for(String server : servers) {
      builder.append(server + "<br>");
    }
    activeServers.setText(builder.toString());
  }

  private void produceErrorMessage(String msg) {
    JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
  }

  /**
   * A class representing the center panel storing the chat log and chat field.
   */
  private class CenterPanel extends JPanel {

    private CenterPanel() {
      this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      chatLog = new JTextPane();
      chatLog.setContentType("text/html");
      chatLog.setEditable(false);
      chatLog.setPreferredSize(new Dimension(400, 300));
//      chatLog.setAutoscrolls(true);
//      chatLog.setBorder(new LineBorder(Color.MAGENTA, 1));

      JScrollPane scrollChatLog = new JScrollPane(chatLog);
      scrollChatLog.setBorder(new LineBorder(Color.BLUE, 1));
      this.add(scrollChatLog);

      chatField = new JTextArea(3, 50);
      chatField.setText("Type here...");
      chatField.setLineWrap(true);
      chatField.setAutoscrolls(true);
      chatField.addFocusListener(new JTextAreaListener());
      chatField.getDocument().addDocumentListener(new TextAreaListener());
      this.add(new JScrollPane(chatField));
    }
  }

  /**
   * A class to listen to the chatLog that stores everyone's messages.
   */
  private class TextAreaListener implements DocumentListener {

    @Override
    public void insertUpdate(DocumentEvent e) {
      Document event = e.getDocument();
      try {
        if (event.getText(event.getLength() - 1, 1).equals("\n")) {
          // if the message is not empty (not counting the newline)
          if (event.getLength() > 1) {
            feature.sendTextOut(removeHtml(event.getText(0, event.getLength() - 1)));
          }
          SwingUtilities.invokeLater(()->chatField.setText(""));
        }
      } catch(BadLocationException ble) {
        produceErrorMessage("Failure in capturing user input");
        feature.sendTextOut("/quit");
        MultiChatViewImpl.this.dispose();
      }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      return;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      return;
    }
  }

  /**
   * A class representing a JDialog that asks the user for their screen name.
   */
  private class ScreenNameSelection extends JDialog {
    private JLabel prompt;
    private JTextField field;
    private JButton submit;
    private JButton cancel;

    private ScreenNameSelection(String prompt) {
      this.prompt = new JLabel(prompt);
      this.field = new JTextField();
      this.submit = new JButton("Ok");
      this.cancel  = new JButton("Cancel");

      this.setTitle("Screen Name Selection");
//      this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      getContentPane().setLayout(
          new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS)
      );
      this.prompt.setAlignmentX(CENTER_ALIGNMENT);
      this.add(this.prompt);

      this.field.setPreferredSize(new Dimension(150, 20));
      this.add(this.field);

      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new FlowLayout());
      this.cancel.addActionListener(e -> cancel());
      this.submit.addActionListener(e -> submitName());
      buttonPanel.add(this.cancel);
      buttonPanel.add(this.submit);

      this.add(buttonPanel);
      this.pack();
      this.setVisible(true);
      this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

      this.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent event) {
          System.exit(2);
        }
      });
    }

    private String getInput() {
      return this.field.getText();
    }

    private void submitName() {
      latch.countDown();
      this.dispose();
    }

    private void cancel() {
      System.exit(1);
    }
  }

  /**
   * A FocusListener to check whether or not the user is focusing on the chat field.
   */
  private class JTextAreaListener implements FocusListener {

    boolean startedTyping = false;
    @Override
    public void focusGained(FocusEvent e) {
      if(!startedTyping) {
        chatField.setText("");
        startedTyping = true;
      }
    }

    @Override
    public void focusLost(FocusEvent e) {
      return;
    }
  }
}