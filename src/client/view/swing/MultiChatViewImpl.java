package client.view.swing;

import client.controller.Feature;
import client.view.MultiChatView;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class MultiChatViewImpl extends JFrame implements MultiChatView {
  private JTextPane chatLog;
  private JTextPane activeUsers;
  private JTextPane activeServers;
  private JTextArea chatField;
  private Feature feature;
  private StringBuilder log;
  private CountDownLatch latch;
  private JMenuBar menu;

  private String prevName;
  private int dateLength;

  public MultiChatViewImpl() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    menu = new CustomMenuBar();
    this.setJMenuBar(menu);

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


    this.prevName = "";
    this.dateLength = 0;

    this.pack();

    this.setLocationRelativeTo(null);

    this.setResizable(false);
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
    return namePane.getInput();
  }

  private String removeHtml(String str) {
    str = str.replaceAll("<", "&lt;");
    str = str.replaceAll(">", "&gt;");
    return str;
  }

  @Override
  public void setTextFieldEditable(boolean b) {
    chatField.setEditable(b);
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
  public void appendChatLog(String s, String color, boolean hasDate, String protocol) {
    String toAdd = "";
    if(hasDate) {
      toAdd = "<pre><span style=\"font-size: 9px;color:"+ color +"\">" + convertEmote(removeHtml(formatDate(s))) + " </span></pre>";
      String user = extractName(toAdd);
      dateLength = extractDateLength(toAdd);
      if(user.equals(prevName)) {
        // getting the message that comes after the name
        s = s.substring(s.indexOf(user) + user.length() + 1);

        // recursive call of appendChatLog but this time with padded spaces instead of date/name
        appendChatLog(String.format("%-" + (dateLength + prevName.length() + 2) + "s", "") + s, color, false, protocol);
        return;
      }
      prevName = user;
    } else {
      toAdd = "<pre><span style=\"font-size: 9px;color:"+ color +"\">" + convertEmote(removeHtml(s)) + " </span></pre>";
      // if the message is not padded, reset prevName
      if(!s.startsWith("     ")) {
        prevName = "";
      }
    }
    log.append(toAdd);
    chatLog.setText(log.toString());
    chatLog.setCaretPosition(chatLog.getDocument().getLength());
  }

  private String extractName(String msg) {
    return msg.substring(msg.indexOf("]") + 2).split(": ")[0];
  }

  private int extractDateLength(String msg) {
    if(msg.contains("]") && msg.contains("[")) {
      return msg.substring(msg.indexOf("["), msg.indexOf("]") + 1).length();
    }
    return 0;
  }

  private String convertEmote(String msg) {
    StringBuilder builder = new StringBuilder();

    // split message by space
    String[] words = msg.split(" ");

    for(String word : words) {
      // if the word equals an emoji name (ex. <3) then replace it with html image code
      if(MultiChatView.HTML_EMOTES.containsKey(word.trim())) {
        builder.append("<img src = \"" + MultiChatViewImpl.class.getClassLoader()
              .getResource("client/resources/images/emojis/" + MultiChatView.HTML_EMOTES.get(word.trim())).toString() + "\"" +
              " alt = \"error\" width = \"20\" height = \"20\">");
      } else if(MultiChatView.TWITCH_EMOTES.containsKey(word.trim())) {
        builder.append("<img src = \"" + MultiChatViewImpl.class.getClassLoader()
            .getResource("client/resources/images/twitch/" + MultiChatView.TWITCH_EMOTES.get(word.trim())).toString() + "\"" +
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
      builder.append("<pre>" + removeHtml(name) + "</pre>");
    }
    activeUsers.setText(builder.toString());
  }

  @Override
  public void setActiveServers(List<String> servers) {
    StringBuilder builder = new StringBuilder();
    builder.append("<h3>Active Servers:</h3>");
    for(String server : servers) {
      builder.append("<pre>" + server + "</pre>");
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

      JScrollPane scrollChatLog = new JScrollPane(chatLog);
      scrollChatLog.setPreferredSize(new Dimension(400, 300));
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
//            feature.sendTextOut(removeHtml(event.getText(0, event.getLength() - 1)));
            feature.sendTextOut(event.getText(0, event.getLength() - 1));
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
      this.setLocationRelativeTo(null);
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

  private class CustomMenuBar extends JMenuBar {
    private JMenu settings;
    private JMenu view;
    private JMenu help;

    private JMenu switchRooms;
    private JMenu privateMessage;

    private JMenuItem darkmode;
    private JMenuItem font;
    private JMenuItem helpItem;

    private CustomMenuBar() {
      settings = new JMenu("MultiChat");
      this.add(settings);
      view = new JMenu("View");
      this.add(view);
      help = new JMenu("Help");
      this.add(help);

      switchRooms = new JMenu("Switch Rooms");
      privateMessage = new JMenu("Private Message...");
      darkmode = new JMenuItem("Enable Darkmode");
      font = new JMenuItem("Font");
      helpItem = new JMenuItem("Help");

      try {
        Image switchRoomIcon = ImageIO.read(getClass().getResource(
            "/client/resources/images/jmenuicons/switchrooms.png"));
        switchRooms.setIcon(new ImageIcon(switchRoomIcon));
        Image privateMsgIcon = ImageIO.read(getClass().getResource(
            "/client/resources/images/jmenuicons/priv message.png"));
        privateMessage.setIcon(new ImageIcon(privateMsgIcon));
        Image fontIcon = ImageIO.read(getClass().getResource(
            "/client/resources/images/jmenuicons/font.png"));
        font.setIcon(new ImageIcon(fontIcon));
        Image darkModeIcon = ImageIO.read(getClass().getResource(
            "/client/resources/images/jmenuicons/darkmode.png"));
        darkmode.setIcon(new ImageIcon(darkModeIcon));
        Image questionIcon = ImageIO.read(getClass().getResource(
            "/client/resources/images/jmenuicons/question.png"));
        helpItem.setIcon(new ImageIcon(questionIcon));
      } catch (IOException ioe) {
        System.out.print("Failed to open load icon images.");
      }

      settings.add(switchRooms);
      settings.add(privateMessage);
      view.add(darkmode);
      view.add(font);
      help.add(helpItem);

      helpItem.addActionListener(e -> createHelpDialog());
    }
  }
  private void createHelpDialog() {
    new HelpDialog();
  }

  @Override
  public void displayError(boolean remainRunningWhenClosed, String errorMessage) {
    return;
  }

  @Override
  public File showSaveDialog(String fileName) {
    return null;
  }

}