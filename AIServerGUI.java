import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.URI;

public class AIServerGUI {

    JFrame frame;
    JTextArea chatArea;
    JTextField messageField;
    JButton sendButton;

    ServerSocket serverSocket;
    Socket socket;
    BufferedReader reader;
    PrintWriter writer;

    // 🔐 Secure API Key (set in ENV variable)
    String API_KEY = System.getenv("sk-...gz0A");

    public AIServerGUI() {

        // ===== UI =====
        frame = new JFrame("AI Server ChatBot");
        chatArea = new JTextArea();
        messageField = new JTextField();
        sendButton = new JButton("Send");

        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Header
        JLabel header = new JLabel("   🤖 ChatGPT AI Server", JLabel.LEFT);
        header.setOpaque(true);
        header.setBackground(new Color(37, 211, 102));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setPreferredSize(new Dimension(100, 50));
        frame.add(header, BorderLayout.NORTH);

        // Chat area
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Bottom panel
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(messageField, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);
        frame.add(bottom, BorderLayout.SOUTH);

        // Events
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        frame.setVisible(true);

        startServer();
    }

    // ================= SERVER =================

    void startServer() {
        try {
            serverSocket = new ServerSocket(7777);
            chatArea.append("Server Started...\nWaiting for client...\n");

            socket = serverSocket.accept();
            chatArea.append("Client Connected!\n");

            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            writer = new PrintWriter(
                    socket.getOutputStream(), true);

            new Thread(this::receiveMessages).start();

        } catch (Exception e) {
            chatArea.append("Server Error: " + e.getMessage() + "\n");
        }
    }

    // ================= SEND =================

    void sendMessage() {
        String msg = messageField.getText();

        if (!msg.isEmpty()) {
            writer.println(msg);
            chatArea.append("Me: " + msg + "\n");
            messageField.setText("");
        }
    }

    // ================= RECEIVE =================

    void receiveMessages() {
        try {
            String msg;

            while ((msg = reader.readLine()) != null) {

                chatArea.append("Client: " + msg + "\n");

                // 🤖 AI Reply
                String aiReply = getAIReply(msg);

                chatArea.append("AI: " + aiReply + "\n");
                writer.println("AI: " + aiReply);
            }

        } catch (Exception e) {
            chatArea.append("Connection Closed\n");
        }
    }

    // ================= AI METHOD =================

    private String getAIReply(String userMsg) {

        try {

            if (API_KEY == null || API_KEY.isEmpty()) {
                return "API Key Missing!";
            }

            String json = """
            {
              "model": "gpt-4.1-mini",
              "input": "%s"
            }
            """.formatted(userMsg.replace("\"", "'"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/responses"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response =
                    client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            String body = response.body();

            // Simple parse
            String reply = body.split("\"output_text\":\"")[1]
                               .split("\"")[0];

            return reply;

        } catch (Exception e) {
            return "AI Error 😢";
        }
    }

    // ================= MAIN =================

    public static void main(String[] args) {
        new AIServerGUI();
    }
}
