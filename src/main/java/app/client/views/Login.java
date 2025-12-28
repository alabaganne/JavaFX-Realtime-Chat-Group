package app.client.views;

import app.client.Client;
import app.client.db.DatabaseManager;
import app.types.User;
import app.util.PasswordUtil;
import app.util.ValidationUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {
    private static final Logger logger = LoggerFactory.getLogger(Login.class);
    private Stage window;
    public static Scene scene;
    public GridPane root;
    private TextField emailTextField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private Button loginButton = new Button("Login");

    public Login(Stage primaryStage) {
        this.window = primaryStage;

        // Main container with card styling
        root = new GridPane();
        root.getStyleClass().add("login-container");
        root.setVgap(16);
        root.setHgap(12);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setMaxWidth(450);

        // Header
        javafx.scene.text.Text header = new javafx.scene.text.Text("Welcome Back");
        header.getStyleClass().add("login-header");
        root.add(header, 0, 0, 2, 1);

        javafx.scene.text.Text subtitle = new javafx.scene.text.Text("Sign in to continue to your account");
        subtitle.getStyleClass().add("login-subtitle");
        root.add(subtitle, 0, 1, 2, 1);

        // Email field
        Label emailLabel = new Label("Email Address");
        root.add(emailLabel, 0, 2, 2, 1);
        emailTextField.setPromptText("Enter your email");
        emailTextField.setPrefWidth(350);
        root.add(emailTextField, 0, 3, 2, 1);

        // Password field
        Label passwordLabel = new Label("Password");
        root.add(passwordLabel, 0, 4, 2, 1);
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefWidth(350);
        root.add(passwordField, 0, 5, 2, 1);

        // Login button
        loginButton.setPrefWidth(350);
        loginButton.setPrefHeight(45);
        loginButton.setOnAction(e -> this.handleLogin());
        root.add(loginButton, 0, 6, 2, 1);

        // Register link
        Hyperlink registerLink = new Hyperlink("Don't have an account? Sign up");
        registerLink.setOnAction(e -> primaryStage.setScene(Register.scene));
        registerLink.setAlignment(Pos.CENTER);
        root.add(registerLink, 0, 7, 2, 1);
        GridPane.setHalignment(registerLink, javafx.geometry.HPos.CENTER);

        scene = new Scene(root, 600, 700);

        // Load CSS stylesheet
        String css = Login.class.getResource("/styles/main.css").toExternalForm();
        scene.getStylesheets().add(css);
    }

    private void handleLogin() {
        String email = emailTextField.getText();
        String password = passwordField.getText();

        // Validate input
        ValidationUtil.ValidationResult validation = ValidationUtil.validateLogin(email, password);
        if (!validation.isValid()) {
            showError(validation.getMessage());
            return;
        }

        // Query user from database using PreparedStatement (prevents SQL injection)
        String sql = "SELECT id, name, email, password, role FROM users WHERE email = ? LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                showError("Invalid email or password");
                logger.warn("Login attempt failed for email: {}", email);
                return;
            }

            String storedPasswordHash = rs.getString("password");

            // Verify password using BCrypt
            if (!PasswordUtil.verifyPassword(password, storedPasswordHash)) {
                showError("Invalid email or password");
                logger.warn("Invalid password attempt for email: {}", email);
                return;
            }

            // Successfully authenticated
            Client.currentUser = new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    storedPasswordHash,
                    rs.getString("role")
            );

            logger.info("User logged in successfully: {}", email);

            // Navigate to appropriate screen based on role
            if ("admin".equals(Client.currentUser.getRole())) {
                new ManageUsers(window);
                window.setScene(ManageUsers.scene);
            } else {
                new Chat(window);
                window.setScene(Chat.scene);
            }

        } catch (SQLException e) {
            logger.error("Database error during login", e);
            showError("An error occurred during login. Please try again.");
        }
    }

    /**
     * Shows an error alert to the user
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
