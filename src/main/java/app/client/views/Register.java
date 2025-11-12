package app.client.views;

import app.client.db.DatabaseManager;
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
import java.sql.SQLException;

public class Register {
    private static final Logger logger = LoggerFactory.getLogger(Register.class);
    private Stage window;
    public static Scene scene;
    public GridPane root = new GridPane();
    private TextField nameTextField = new TextField();
    private TextField emailTextField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private Button registerButton = new Button("Register");

    public Register(Stage primaryStage) {
        this.window = primaryStage;

        // Main container with card styling
        root.getStyleClass().add("login-container");
        root.setHgap(12);
        root.setVgap(16);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setMaxWidth(450);

        // Header
        javafx.scene.text.Text header = new javafx.scene.text.Text("Create Account");
        header.getStyleClass().add("login-header");
        root.add(header, 0, 0, 2, 1);

        javafx.scene.text.Text subtitle = new javafx.scene.text.Text("Sign up to get started");
        subtitle.getStyleClass().add("login-subtitle");
        root.add(subtitle, 0, 1, 2, 1);

        // Name field
        Label nameLabel = new Label("Full Name");
        root.add(nameLabel, 0, 2, 2, 1);
        nameTextField.setPromptText("Enter your full name");
        nameTextField.setPrefWidth(350);
        root.add(nameTextField, 0, 3, 2, 1);

        // Email field
        Label emailLabel = new Label("Email Address");
        root.add(emailLabel, 0, 4, 2, 1);
        emailTextField.setPromptText("Enter your email");
        emailTextField.setPrefWidth(350);
        root.add(emailTextField, 0, 5, 2, 1);

        // Password field
        Label passwordLabel = new Label("Password");
        root.add(passwordLabel, 0, 6, 2, 1);
        passwordField.setPromptText("At least 8 characters");
        passwordField.setPrefWidth(350);
        root.add(passwordField, 0, 7, 2, 1);

        // Password hint
        javafx.scene.text.Text passwordHint = new javafx.scene.text.Text("Must be at least 8 characters with letters and numbers");
        passwordHint.setStyle("-fx-fill: #6b7280; -fx-font-size: 12px;");
        root.add(passwordHint, 0, 8, 2, 1);

        // Register button
        registerButton.setPrefWidth(350);
        registerButton.setPrefHeight(45);
        registerButton.setOnAction(e -> this.handleRegister());
        root.add(registerButton, 0, 9, 2, 1);

        // Login link
        Hyperlink loginLink = new Hyperlink("Already have an account? Sign in");
        loginLink.setOnAction(e -> primaryStage.setScene(Login.scene));
        loginLink.setAlignment(Pos.CENTER);
        root.add(loginLink, 0, 10, 2, 1);
        GridPane.setHalignment(loginLink, javafx.geometry.HPos.CENTER);

        scene = new Scene(root, 600, 750);

        // Load CSS stylesheet
        String css = Register.class.getResource("/styles/main.css").toExternalForm();
        scene.getStylesheets().add(css);
    }

    private void handleRegister() {
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        String password = passwordField.getText();

        // Validate all inputs
        ValidationUtil.ValidationResult validation = ValidationUtil.validateRegistration(name, email, password);
        if (!validation.isValid()) {
            showError(validation.getMessage());
            return;
        }

        // Hash the password using BCrypt
        String hashedPassword = PasswordUtil.hashPassword(password);

        // Insert user into database
        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, 'user')";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, hashedPassword);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1) {
                logger.info("New user registered successfully: {}", email);

                nameTextField.clear();
                emailTextField.clear();
                passwordField.clear();

                showSuccess("Account successfully created! You can now log in.");
                window.setScene(Login.scene);
            }

        } catch (SQLException e) {
            logger.error("Error during user registration", e);

            // Check if it's a duplicate email error
            if (e.getMessage().contains("Duplicate entry")) {
                showError("Email address already exists. Please use a different email.");
            } else {
                showError("An error occurred during registration. Please try again.");
            }
        }
    }

    /**
     * Shows an error alert to the user
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Registration Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a success alert to the user
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration Successful");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
