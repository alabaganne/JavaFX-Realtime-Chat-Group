package app.client.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom message bubble component for chat interface
 * Displays messages with sender name, text, and timestamp
 */
public class MessageBubble extends HBox {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, HH:mm");

    private final boolean isSent;
    private final VBox bubbleContainer;
    private final Text senderText;
    private final Text messageText;
    private final Text timeText;

    /**
     * Creates a new message bubble
     * @param senderName Name of message sender
     * @param message Message content
     * @param isSent Whether this message was sent by the current user
     */
    public MessageBubble(String senderName, String message, boolean isSent) {
        this.isSent = isSent;

        // Main container
        this.setPadding(new Insets(4, 12, 4, 12));
        this.setAlignment(isSent ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        this.setMaxWidth(Double.MAX_VALUE);

        // Bubble container
        bubbleContainer = new VBox(4);
        bubbleContainer.setPadding(new Insets(10, 14, 10, 14));
        bubbleContainer.setMaxWidth(500);

        if (isSent) {
            bubbleContainer.getStyleClass().add("message-bubble-sent");
        } else {
            bubbleContainer.getStyleClass().add("message-bubble-received");
        }

        // Sender name (only show for received messages)
        if (!isSent) {
            senderText = new Text(senderName);
            senderText.getStyleClass().add("message-sender");
            bubbleContainer.getChildren().add(senderText);
        } else {
            senderText = null;
        }

        // Message text
        messageText = new Text(message);
        messageText.setWrappingWidth(450);
        messageText.getStyleClass().add("message-text");
        if (isSent) {
            messageText.getStyleClass().add("message-text-sent");
        }
        bubbleContainer.getChildren().add(messageText);

        // Timestamp
        timeText = new Text(getCurrentTime());
        timeText.getStyleClass().add("message-time");
        bubbleContainer.getChildren().add(timeText);

        this.getChildren().add(bubbleContainer);
    }

    /**
     * Creates a message bubble with a custom timestamp
     * @param senderName Name of message sender
     * @param message Message content
     * @param isSent Whether this message was sent by the current user
     * @param timestamp Message timestamp
     */
    public MessageBubble(String senderName, String message, boolean isSent, LocalDateTime timestamp) {
        this(senderName, message, isSent);
        updateTimestamp(timestamp);
    }

    /**
     * Updates the timestamp display
     * @param timestamp New timestamp
     */
    public void updateTimestamp(LocalDateTime timestamp) {
        if (timeText != null && timestamp != null) {
            LocalDateTime now = LocalDateTime.now();
            String timeString;

            // If message is from today, show time only
            if (timestamp.toLocalDate().equals(now.toLocalDate())) {
                timeString = timestamp.format(TIME_FORMATTER);
            } else {
                // If message is from another day, show date and time
                timeString = timestamp.format(DATE_FORMATTER);
            }

            timeText.setText(timeString);
        }
    }

    /**
     * Gets current time formatted as HH:mm
     * @return Formatted time string
     */
    private String getCurrentTime() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }

    /**
     * Updates the message text
     * @param message New message content
     */
    public void setMessage(String message) {
        if (messageText != null) {
            messageText.setText(message);
        }
    }

    /**
     * Gets the message text
     * @return Message content
     */
    public String getMessage() {
        return messageText != null ? messageText.getText() : "";
    }

    /**
     * Checks if this is a sent message
     * @return true if sent by current user
     */
    public boolean isSent() {
        return isSent;
    }
}
