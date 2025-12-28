package app.client.views;

import app.client.Client;
import app.client.db.DatabaseManager;
import app.config.Config;
import app.types.Message;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Chat {
    private static final Logger logger = LoggerFactory.getLogger(Chat.class);
    private Stage window;
    public static Scene scene;
    private TextField messageField;
    private ArrayList<Message> messages;
    private VBox messagesVBox;

    // A client has a socket to connect to the server and a reader and writer to receive and send messages respectively.
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public Chat(Stage primaryStage) throws SQLException {
        try {
            // Connect to server using configuration
            this.socket = new Socket(Config.getServerHost(), Config.getServerPort());
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            bufferedWriter.write(Client.currentUser.getName());
            bufferedWriter.newLine();
            bufferedWriter.flush();

            logger.info("Connected to chat server successfully");
            this.listenForMessage();
        } catch (IOException e) {
            logger.error("Error connecting to chat server", e);
            showError("Failed to connect to chat server. Please try again later.");
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

        this.window = primaryStage;

        // Create modern chat header
        HBox headerBox = new HBox();
        headerBox.getStyleClass().add("chat-header");
        headerBox.setPadding(new Insets(20));
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Left side: title and user info
        VBox headerLeft = new VBox(4);
        headerLeft.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(headerLeft, Priority.ALWAYS);

        javafx.scene.text.Text chatTitle = new javafx.scene.text.Text("Group Chat");
        chatTitle.setStyle("-fx-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        javafx.scene.text.Text userInfo = new javafx.scene.text.Text("Logged in as: " + Client.currentUser.getName());
        userInfo.setStyle("-fx-fill: #e0e7ff; -fx-font-size: 13px;");

        headerLeft.getChildren().addAll(chatTitle, userInfo);

        // Right side: sign out button
        Button signOutButton = new Button("Sign Out");
        signOutButton.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        signOutButton.setOnAction(e -> this.logout());

        headerBox.getChildren().addAll(headerLeft, signOutButton);

        // Messages scroll pane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("messages-container");
        messagesVBox = new VBox(8);
        messagesVBox.setPadding(new Insets(16));
        messagesVBox.setFillWidth(true);
        scrollPane.setContent(messagesVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Auto-scroll to bottom when new messages arrive
        messagesVBox.heightProperty().addListener((obs, oldVal, newVal) -> {
            scrollPane.setVvalue(1.0);
        });

        // Get messages from the database
        loadMessagesFromDatabase();

        // Input area
        HBox inputBox = new HBox(12);
        inputBox.getStyleClass().add("message-input-container");
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(16));

        messageField = new TextField();
        messageField.getStyleClass().add("message-input");
        messageField.setPromptText("Type a message...");
        messageField.setPrefHeight(40);
        HBox.setHgrow(messageField, Priority.ALWAYS);

        // Send on Enter key
        messageField.setOnAction(e -> this.sendMessage());

        Button sendButton = new Button("Send");
        sendButton.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12 24; -fx-cursor: hand;");
        sendButton.setOnAction(e -> this.sendMessage());

        inputBox.getChildren().addAll(messageField, sendButton);

        // Main layout
        VBox mainContainer = new VBox();
        mainContainer.getChildren().addAll(headerBox, scrollPane, inputBox);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        scene = new Scene(mainContainer, 700, 600);

        // Load CSS
        String css = Chat.class.getResource("/styles/main.css").toExternalForm();
        scene.getStylesheets().add(css);
    }

    /**
     * Appends a message to the chat display
     * @param senderId ID of the message sender
     * @param senderName Name of the sender
     * @param message Message content
     */
    public void appendToMessages(int senderId, String senderName, String message) {
        boolean isSent = (senderId == Client.currentUser.getId());
        String displayName = isSent ? "You" : senderName;

        MessageBubble bubble = new MessageBubble(displayName, message, isSent);
        messagesVBox.getChildren().add(bubble);
    }

    /**
     * Loads chat messages from the database
     */
    private void loadMessagesFromDatabase() {
        String sql = "SELECT u.id as senderId, u.name as senderName, m.text as message " +
                     "FROM messages m " +
                     "LEFT JOIN users u ON u.id = m.userId " +
                     "ORDER BY m.created ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                appendToMessages(
                        rs.getInt("senderId"),
                        rs.getString("senderName"),
                        rs.getString("message")
                );
            }

            logger.info("Loaded chat messages from database");

        } catch (SQLException e) {
            logger.error("Error loading messages from database", e);
            showError("Failed to load chat history.");
        }
    }

    // Sending a message isn't blocking and can be done without spawning a thread, unlike waiting for a message.
    public void sendMessage() {
        try {
            String messageToSend = messageField.getText().trim();
            if (messageToSend.isEmpty()) {
                showWarning("Please type a message first!");
                return;
            }

            // Store message in the database
            String sql = "INSERT INTO messages (userId, text) VALUES (?, ?)";

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, Client.currentUser.getId());
                ps.setString(2, messageToSend);
                ps.executeUpdate();

                logger.info("Message saved to database from user: {}", Client.currentUser.getName());

            } catch (SQLException e) {
                logger.error("Error saving message to database", e);
                showError("Failed to save message.");
                return;
            }

            // Broadcast new message to other users
            bufferedWriter.write(Client.currentUser.getName() + ": " + messageToSend);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            messageField.clear();

            // Display new message
            appendToMessages(Client.currentUser.getId(), Client.currentUser.getName(), messageToSend);

        } catch (IOException e) {
            logger.error("Error sending message", e);
            showError("Failed to send message.");
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Listens for messages from the server in a background thread
     * Runs continuously until connection is closed
     */
    public void listenForMessage() {
        Thread listenerThread = new Thread(() -> {
            String msgFromGroupChat;
            logger.info("Started listening for messages");

            // Continue to listen while socket is connected
            while (socket.isConnected()) {
                try {
                    msgFromGroupChat = bufferedReader.readLine();

                    if (msgFromGroupChat == null) {
                        logger.info("Server closed connection");
                        break;
                    }

                    // Parse message format: "SenderName: message text"
                    String finalMsg = msgFromGroupChat;
                    Platform.runLater(() -> {
                        // Extract sender and message
                        int colonIndex = finalMsg.indexOf(":");
                        if (colonIndex > 0) {
                            String sender = finalMsg.substring(0, colonIndex).trim();
                            String text = finalMsg.substring(colonIndex + 1).trim();

                            // Create message bubble (received from others)
                            MessageBubble bubble = new MessageBubble(sender, text, false);
                            messagesVBox.getChildren().add(bubble);
                        } else {
                            // System message (like "User joined")
                            Text systemMsg = new Text(finalMsg);
                            systemMsg.setStyle("-fx-fill: #6b7280; -fx-font-size: 12px; -fx-font-style: italic;");
                            messagesVBox.getChildren().add(systemMsg);
                        }
                    });

                } catch (IOException e) {
                    logger.error("Error receiving message", e);
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
            }

            logger.info("Stopped listening for messages");
        });

        listenerThread.setDaemon(true);
        listenerThread.setName("MessageListener-" + Client.currentUser.getName());
        listenerThread.start();
    }

    // Helper method to close everything, so you don't have to repeat yourself.
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        // Note you only need to close the outer wrapper as the underlying streams are closed when you close the wrapper.
        // Note you want to close the outermost wrapper so that everything gets flushed.
        // Note that closing a socket will also close the socket's InputStream and OutputStream.
        // Closing the input stream closes the socket. You need to use shutdownInput() on socket to just close the input stream.
        // Closing the socket will also close the socket's input stream and output stream.
        // Close the socket after closing the streams.
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        closeEverything(socket, bufferedReader, bufferedWriter);
        logger.info("User logged out: {}", Client.currentUser.getName());
        Client.currentUser = null;
        window.setScene(Login.scene);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
