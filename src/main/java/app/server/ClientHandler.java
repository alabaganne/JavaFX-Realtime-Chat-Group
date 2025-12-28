package app.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles client connections in separate threads
 * Each ClientHandler manages communication with one connected client
 * Thread-safe implementation using CopyOnWriteArrayList for concurrent access
 */
public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    // Thread-safe list of all active client handlers for broadcasting messages
    private static final List<ClientHandler> clientHandlers = new CopyOnWriteArrayList<>();

    // Socket and I/O streams for client communication
    private final Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    /**
     * Creates a new client handler for the given socket
     * @param socket The client socket connection
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Read client username (first message sent by client)
            this.clientUsername = bufferedReader.readLine();

            if (clientUsername == null || clientUsername.trim().isEmpty()) {
                logger.warn("Client connected without username");
                closeEverything(socket, bufferedReader, bufferedWriter);
                return;
            }

            clientHandlers.add(this);
            logger.info("User '{}' joined the chat. Total users: {}", clientUsername, clientHandlers.size());
            broadcastMessage(clientUsername + " has entered the chat!");

        } catch (IOException e) {
            logger.error("Error initializing client handler", e);
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Main thread execution method
     * Listens for messages from the client and broadcasts them
     * Runs until the client disconnects
     */
    @Override
    public void run() {
        String messageFromClient;

        try {
            // Continue listening for messages while connection is active
            while (socket.isConnected() && !socket.isClosed()) {
                try {
                    messageFromClient = bufferedReader.readLine();

                    // Client disconnected gracefully
                    if (messageFromClient == null) {
                        logger.info("Client '{}' disconnected", clientUsername);
                        break;
                    }

                    logger.debug("Message from '{}': {}", clientUsername, messageFromClient);
                    broadcastMessage(messageFromClient);

                } catch (IOException e) {
                    logger.warn("Error reading message from client '{}'", clientUsername, e);
                    break;
                }
            }
        } finally {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Sends data to this specific client
     * @param data The data to send
     */
    public synchronized void sendData(String data) {
        try {
            if (bufferedWriter != null && socket.isConnected()) {
                bufferedWriter.write(data);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            logger.error("Error sending data to client '{}'", clientUsername, e);
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Broadcasts a message to all connected clients except the sender
     * @param messageToSend The message to broadcast
     */
    public void broadcastMessage(String messageToSend) {
        if (messageToSend == null || messageToSend.trim().isEmpty()) {
            return;
        }

        logger.debug("Broadcasting message: {}", messageToSend);

        for (ClientHandler clientHandler : clientHandlers) {
            try {
                // Don't send the message back to the sender
                if (!clientHandler.clientUsername.equals(this.clientUsername)) {
                    synchronized (clientHandler) {
                        if (clientHandler.bufferedWriter != null && clientHandler.socket.isConnected()) {
                            clientHandler.bufferedWriter.write(messageToSend);
                            clientHandler.bufferedWriter.newLine();
                            clientHandler.bufferedWriter.flush();
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Error broadcasting to client '{}'", clientHandler.clientUsername, e);
                clientHandler.closeEverything(clientHandler.socket, clientHandler.bufferedReader, clientHandler.bufferedWriter);
            }
        }
    }

    /**
     * Removes this client handler from the active list
     * Notifies other clients that this user has left
     */
    private void removeClientHandler() {
        boolean removed = clientHandlers.remove(this);
        if (removed && clientUsername != null) {
            logger.info("User '{}' left the chat. Remaining users: {}", clientUsername, clientHandlers.size());
            broadcastMessage(clientUsername + " has left the chat!");
        }
    }

    /**
     * Closes all resources gracefully
     * @param socket The socket to close
     * @param bufferedReader The reader to close
     * @param bufferedWriter The writer to close
     */
    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();

        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        } catch (IOException e) {
            logger.error("Error closing buffered reader", e);
        }

        try {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        } catch (IOException e) {
            logger.error("Error closing buffered writer", e);
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing socket", e);
        }

        logger.debug("All resources closed for client '{}'", clientUsername);
    }

    /**
     * Gets the current number of connected clients
     * @return Number of active client handlers
     */
    public static int getClientCount() {
        return clientHandlers.size();
    }

    /**
     * Gets the username of this client
     * @return Client username
     */
    public String getClientUsername() {
        return clientUsername;
    }
}