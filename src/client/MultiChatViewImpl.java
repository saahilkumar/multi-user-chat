package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.util.concurrent.Flow;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class MultiChatViewImpl extends JFrame implements MultiChatView {
  private JTextPane chatLog;
  private JTextArea activeUsers;
  private JTextArea activeServers;
  private JTextArea chatField;
  private Feature feature;
  private StringBuilder log;

  public MultiChatViewImpl() {
    this.setLayout(new FlowLayout());

    log = new StringBuilder();

    activeUsers = new JTextArea(15, 10);
    activeUsers.setEditable(false);
    activeUsers.setText("Active Users:");
    this.add(new JScrollPane(activeUsers));

    CenterPanel center = new CenterPanel();
    this.add(center);

    activeServers = new JTextArea(15, 10);
    activeServers.setEditable(false);
    activeServers.setText("Active Servers:");
    this.add(new JScrollPane(activeServers));



    this.pack();
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
      this.add(new JScrollPane(chatLog));

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
          feature.sendTextOut(event.getText(0, event.getLength() - 1));
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