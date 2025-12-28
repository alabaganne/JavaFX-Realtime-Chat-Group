package app.server;

import app.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Chat server that handles multiple clients using a thread pool
 * Broadcasts messages in real-time to all connected clients
 */
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int THREAD_POOL_SIZE = 50; // Maximum concurrent clients

    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private volatile boolean running = true;

    /**
     * Creates a new server with the given server socket
     * @param serverSocket The server socket to listen on
     */
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        logger.info("Server initialized with thread pool size: {}", THREAD_POOL_SIZE);
    }

    /**
     * Starts the server and listens for client connections
     * Each client connection is handled in a separate thread from the pool
     */
    public void startServer() {
        logger.info("Server started on port {}", serverSocket.getLocalPort());
        logger.info("Waiting for client connections...");

        try {
            while (running && !serverSocket.isClosed()) {
                // Accept client connection (blocking call)
                Socket socket = serverSocket.accept();
                logger.info("New client connected from: {}", socket.getRemoteSocketAddress());

                // Handle client in a thread from the pool
                ClientHandler clientHandler = new ClientHandler(socket);
                threadPool.execute(clientHandler);

                logger.info("Active client handlers: {}", ClientHandler.getClientCount());
            }
        } catch (IOException e) {
            if (running) {
                logger.error("Error accepting client connection", e);
            } else {
                logger.info("Server stopped accepting connections");
            }
        } finally {
            shutdown();
        }
    }

    /**
     * Stops the server and shuts down gracefully
     */
    public void stop() {
        running = false;
        closeServerSocket();
    }

    /**
     * Shuts down the server and thread pool gracefully
     */
    private void shutdown() {
        logger.info("Shutting down server...");

        closeServerSocket();

        // Shutdown thread pool gracefully
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warn("Thread pool did not terminate in time, forcing shutdown");
                threadPool.shutdownNow();
            }
            logger.info("Thread pool shut down successfully");
        } catch (InterruptedException e) {
            logger.error("Thread pool shutdown interrupted", e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("Server shutdown complete");
    }

    /**
     * Closes the server socket gracefully
     */
    private void closeServerSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                logger.info("Server socket closed");
            }
        } catch (IOException e) {
            logger.error("Error closing server socket", e);
        }
    }

    /**
     * Main method to start the chat server
     * Reads port from configuration
     */
    public static void main(String[] args) {
        try {
            int port = Config.getServerPort();
            logger.info("Starting chat server on port {}...", port);

            ServerSocket serverSocket = new ServerSocket(port);
            Server server = new Server(serverSocket);

            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown signal received");
                server.stop();
            }));

            server.startServer();

        } catch (IOException e) {
            logger.error("Failed to start server", e);
            System.exit(1);
        }
    }
}
