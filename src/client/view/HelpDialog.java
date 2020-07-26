package client.view;

import java.awt.Dimension;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class HelpDialog extends JDialog {

  public HelpDialog() {
    super();
    JTextPane displayText = new JTextPane();
    displayText.setPreferredSize(new Dimension(500, 300));
    displayText.setContentType("text/html");
    displayText.setEditable(false);
    this.add(new JScrollPane(displayText));

    displayText.setText("<h2>Welcome to MultiChat.</h2>"
        + "<h3> A free socket to socket texting application with AES encryption.</h3>"
        + "MultiChat displays active MultiChat servers on the right, the active users in your "
        + "current room on the left. Every message in the chat room will appear in the center."
        + "<h2>Commands are below.</h2>"
        + "Type /quit to quit MultiChat.<br>"
        + "Type /emotes to access a menu of emoticons.<br>"
        + "Type /join to join another chat room, "
        + "enter the room number such like: \"/join 59090\".<br>"
        + "Type /help to access this help menu.");

    this.pack();
    this.setLocationRelativeTo(null);
    this.setResizable(false);
    this.setVisible(true);
    this.setTitle("MultiChat - Help");
    this.setDefaultCloseOperation(HIDE_ON_CLOSE);
  }
}