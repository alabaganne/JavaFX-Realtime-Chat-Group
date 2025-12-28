package app.types;

/**
 * User model representing a system user
 * Encapsulates user data with proper getter/setter methods
 */
public class User {
    private int id;
    private String name;
    private String email;
    private String password; // Stores hashed password
    private String role;

    /**
     * Default constructor for JavaFX TableView
     */
    public User() {}

    /**
     * Full constructor
     * @param id User ID
     * @param name User's full name
     * @param email User's email address
     * @param password User's hashed password
     * @param role User's role (admin or user)
     */
    public User(int id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Checks if user has admin role
     * @return true if user is admin
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
