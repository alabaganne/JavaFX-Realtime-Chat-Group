package app.types;

import java.sql.Timestamp;

/**
 * Message model representing a chat message
 * Encapsulates message data with proper getter/setter methods
 */
public class Message {
    private int id;
    private int senderId;
    private String text;
    private Timestamp created;
    private String senderName; // From JOIN with users table

    /**
     * Default constructor
     */
    public Message() {}

    /**
     * Full constructor
     * @param id Message ID
     * @param senderId ID of user who sent the message
     * @param text Message content
     * @param created Timestamp when message was created
     * @param senderName Name of sender (from JOIN)
     */
    public Message(int id, int senderId, String text, Timestamp created, String senderName) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
        this.created = created;
        this.senderName = senderName;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", senderName='" + senderName + '\'' +
                ", text='" + text + '\'' +
                ", created=" + created +
                '}';
    }
}
