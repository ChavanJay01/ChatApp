// ================= FILE 2 =================
// AIClientGUI.java

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class AIClientGUI {

    JFrame frame;
    JTextArea chatArea;
    JTextField messageField;
    JButton sendButton;

    Socket socket;
    BufferedReader reader;
    PrintWriter writer;

    public AIClientGUI() {

        frame = new JFrame("Client Chat");
        chatArea = new JTextArea();
        messageField = new JTextField();
        sendButton = new JButton("Send");

        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel header = new JLabel("   Chat Client", JLabel.LEFT);
        header.setOpaque(true);
        header.setBackground(new Color(7, 94, 84));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setPreferredSize(new Dimension(100, 50));
        frame.add(header, BorderLayout.NORTH);

        chatArea.setEditable(false);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(messageField, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);
        frame.add(bottom, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        frame.setVisible(true);

        connectToServer();
    }

    void connectToServer() {
        try {
            socket = new Socket("localhost", 7777);
            chatArea.append("Connected to AI Server!\n");

            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            writer = new PrintWriter(
                    socket.getOutputStream(), true);

            new Thread(() -> receiveMessages()).start();

        } catch (Exception e) {
            chatArea.append("Server Not Found\n");
        }
    }

    void sendMessage() {
        String msg = messageField.getText();

        if (!msg.isEmpty()) {
            writer.println(msg);
            chatArea.append("Me: " + msg + "\n");
            messageField.setText("");
        }
    }

    void receiveMessages() {
        try {
            String msg;
            while ((msg = reader.readLine()) != null) {
                chatArea.append(msg + "\n");
            }
        } catch (Exception e) {
            chatArea.append("Connection Closed\n");
        }
    }

    public static void main(String[] args) {
        new AIClientGUI();
    }
}
