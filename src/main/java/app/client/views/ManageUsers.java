package app.client.views;

import app.client.Client;
import app.client.db.DatabaseManager;
import app.types.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ManageUsers {
    private static final Logger logger = LoggerFactory.getLogger(ManageUsers.class);
    private Stage window;
    public static Scene scene;
    private TableView<User> tableView;

    public ManageUsers(Stage window) throws SQLException {
        this.window = window;

        // Header section
        VBox headerBox = new VBox(8);
        headerBox.getStyleClass().add("chat-header");
        headerBox.setPadding(new Insets(24));

        Text headerText = new Text("👥 User Management");
        headerText.getStyleClass().add("chat-title");

        Text subtitle = new Text("Manage system users and permissions");
        subtitle.setStyle("-fx-fill: #e0e7ff; -fx-font-size: 13px;");

        headerBox.getChildren().addAll(headerText, subtitle);

        // Action buttons
        HBox buttonBox = new HBox(12);
        buttonBox.setPadding(new Insets(20));
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.getStyleClass().add("button-secondary");
        refreshButton.setOnAction(e -> {
            try {
                this.getUsers();
            } catch (SQLException ex) {
                logger.error("Error refreshing users", ex);
                showError("Failed to refresh user list.");
            }
        });

        Button deleteButton = new Button("🗑️ Delete User");
        deleteButton.getStyleClass().add("button-danger");
        deleteButton.setOnAction(e -> {
            try {
                this.handleDeleteUser();
            } catch (SQLException ex) {
                logger.error("Error deleting user", ex);
                showError("Failed to delete user.");
            }
        });

        Button logoutButton = new Button("↩️ Logout");
        logoutButton.getStyleClass().add("button-secondary");
        logoutButton.setOnAction(e -> {
            window.setScene(Login.scene);
            Client.currentUser = null;
        });

        buttonBox.getChildren().addAll(refreshButton, deleteButton, logoutButton);

        // Table view
        tableView = new TableView<>();
        tableView.setPlaceholder(new Label("No users found"));
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        TableColumn<User, String> roleCol = new TableColumn<>("Role");

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        idCol.setPrefWidth(60);
        nameCol.setPrefWidth(150);
        emailCol.setPrefWidth(200);
        roleCol.setPrefWidth(100);

        tableView.getColumns().addAll(idCol, nameCol, emailCol, roleCol);

        this.getUsers();

        // Main container
        VBox mainContainer = new VBox();
        mainContainer.getChildren().addAll(headerBox, buttonBox, tableView);
        mainContainer.setPadding(new Insets(0, 20, 20, 20));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        scene = new Scene(mainContainer, 800, 600);

        // Load CSS
        String css = ManageUsers.class.getResource("/styles/main.css").toExternalForm();
        scene.getStylesheets().add(css);
    }

    public void handleDeleteUser() throws SQLException {
        User user = tableView.getSelectionModel().getSelectedItem();
        if (user == null) {
            showWarning("Please select a user to delete.");
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete User");
        confirmAlert.setContentText("Are you sure you want to delete user: " + user.getName() + "?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM users WHERE id = ?";

                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setInt(1, user.getId());
                    int rowsAffected = ps.executeUpdate();

                    if (rowsAffected > 0) {
                        logger.info("User deleted: {} (ID: {})", user.getEmail(), user.getId());
                        showSuccess("User deleted successfully!");
                        this.getUsers();
                    }

                } catch (SQLException e) {
                    logger.error("Error deleting user", e);
                    showError("Failed to delete user. Please try again.");
                }
            }
        });
    }

    public void getUsers() throws SQLException {
        tableView.refresh();
        tableView.getItems().clear();

        // Get users from the database using PreparedStatement
        String sql = "SELECT id, name, email, password, role FROM users ORDER BY id";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tableView.getItems().add(new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role")
                ));
            }

            logger.info("Loaded {} users", tableView.getItems().size());

        } catch (SQLException e) {
            logger.error("Error loading users", e);
            showError("Failed to load users from database.");
            throw e;
        }
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
