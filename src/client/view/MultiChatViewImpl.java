package client.view;

import client.controller.Feature;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
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

  public MultiChatViewImpl() {
    this.setLayout(new FlowLayout());

    log = new StringBuilder();

    activeUsers = new JTextPane();
    activeUsers.setEditable(false);
    activeUsers.setText("<h1>Active Users:</h1>");
    activeUsers.setContentType("text/html");
    activeUsers.setAutoscrolls(true);
    activeUsers.setPreferredSize(new Dimension(100, 300));
    this.add(new JScrollPane(activeUsers));

    CenterPanel center = new CenterPanel();
    this.add(center);

    activeServers = new JTextPane();
    activeServers.setEditable(false);
    activeServers.setText("Active Servers:");
    activeServers.setPreferredSize(new Dimension(100, 300));
    this.add(new JScrollPane(activeServers));



    this.pack();

//    setActiveUsers(new ArrayList<>(Arrays.asList("Saahil", "David", "Dog")));
  }

  @Override
  public String getName() {
    return JOptionPane.showInputDialog(this, "Choose a screen name:", "Screen name selection",
        JOptionPane.PLAIN_MESSAGE);
  }

  @Override
  public void setTextFieldEditable(boolean b) {
    chatField.setEditable(b);
    chatField.getDocument().addDocumentListener(new TextAreaListener());
  }

  @Override
  public void giveFeatures(Feature feature) {
    this.feature = feature;
  }

  @Override
  public void display() {
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setVisible(true);
  }

  @Override
  public void appendChatLog(String s, String color) {
//    String currentLog = this.chatLog.getText();
//    this.chatLog.setText(currentLog + s + "\n");
    String toAdd = "<span style=\"color:"+ color +"\">" + s + "</span><br>";
    log.append(toAdd);
    chatLog.setText(log.toString());
    chatLog.setCaretPosition(chatLog.getDocument().getLength());
  }

  @Override
  public void setActiveUsers(List<String> names) {
    StringBuilder builder = new StringBuilder();
    builder.append("<h1>Active Users:</h1>");
    for(String name : names) {
      builder.append(name + "<br>");
    }
    activeUsers.setText(builder.toString());
  }

  @Override
  public void setActiveServers(List<String> servers) {

  }

  private void produceErrorMessage(String msg) {
    JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
  }

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
      chatField.setLineWrap(true);
      chatField.setAutoscrolls(true);
      this.add(new JScrollPane(chatField));
    }
  }

  private class TextAreaListener implements DocumentListener {

    @Override
    public void insertUpdate(DocumentEvent e) {
      Document event = e.getDocument();
      try {
        if (event.getText(event.getLength() - 1, 1).equals("\n")) {
          // if the message is not empty (not counting the newline)
          if (event.getLength() > 1) {
            feature.sendTextOut(event.getText(0, event.getLength() - 1));
          }
          SwingUtilities.invokeLater(()->chatField.setText(""));
        }
      } catch(BadLocationException ble) {
        produceErrorMessage("Failure in capturing user input");
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
}