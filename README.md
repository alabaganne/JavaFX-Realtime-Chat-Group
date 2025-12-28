# 💬 JavaFX Real-Time Group Chat Application

A modern, secure, real-time chat application built with JavaFX featuring user authentication, role-based access control, and a beautiful modern UI.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-17.0.1-blue?style=flat)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat&logo=mysql)
![Maven](https://img.shields.io/badge/Maven-3.8+-red?style=flat&logo=apache-maven)
![License](https://img.shields.io/badge/License-MIT-green?style=flat)

## ✨ Features

### 🔐 Security & Authentication
- **Secure Password Hashing**: BCrypt password hashing with salt
- **SQL Injection Protection**: Prepared statements throughout the application
- **Input Validation**: Comprehensive validation for all user inputs
- **Role-Based Access Control**: Admin and user roles with different permissions

### 💬 Real-Time Chat
- **Live Messaging**: Socket-based real-time message broadcasting
- **Message Persistence**: All messages stored in MySQL database
- **Chat History**: Automatic loading of previous messages
- **Modern Message Bubbles**: Beautiful UI with sender/receiver differentiation
- **Timestamps**: Automatic timestamp display for all messages

### 👤 User Management
- **User Registration**: Secure account creation with validation
- **User Login**: Email/password authentication
- **Admin Panel**: User management interface for administrators
- **User Deletion**: Safe user removal with confirmation dialogs

### 🎨 Modern UI/UX
- **Professional Design**: Modern indigo/purple color scheme
- **Responsive Layout**: Clean, responsive interface design
- **CSS Styling**: Comprehensive stylesheet for consistent appearance
- **Interactive Elements**: Hover effects and smooth transitions
- **Emoji Support**: Visual enhancements throughout the interface

### 🏗️ Architecture & Code Quality
- **Connection Pooling**: HikariCP for efficient database connections
- **Thread Pool**: Fixed-size thread pool for handling client connections
- **Proper Logging**: SLF4J logging throughout the application
- **MVC Pattern**: Clean separation of concerns
- **Encapsulation**: Proper use of getters/setters
- **Configuration Management**: Properties-based configuration
- **Error Handling**: Comprehensive try-catch blocks and user feedback

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher ([Download](https://www.oracle.com/java/technologies/downloads/))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **MySQL 8.0+** ([Download](https://dev.mysql.com/downloads/mysql/))

### Database Setup

1. **Start MySQL Server**
   ```bash
   # Linux/Mac
   sudo systemctl start mysql
   # or
   mysql.server start

   # Windows
   net start MySQL
   ```

2. **Create Database**
   ```sql
   CREATE DATABASE chat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   USE chat;
   ```

3. **Create Tables**
   ```sql
   CREATE TABLE users (
       id INT AUTO_INCREMENT PRIMARY KEY,
       name VARCHAR(100) NOT NULL,
       email VARCHAR(255) UNIQUE NOT NULL,
       password VARCHAR(255) NOT NULL,
       role VARCHAR(20) DEFAULT 'user',
       created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       INDEX idx_email (email),
       INDEX idx_role (role)
   );

   CREATE TABLE messages (
       id INT AUTO_INCREMENT PRIMARY KEY,
       userId INT NOT NULL,
       text TEXT NOT NULL,
       created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE,
       INDEX idx_user (userId),
       INDEX idx_created (created)
   );
   ```

4. **Create Admin User** (Password: `admin123`)
   ```sql
   INSERT INTO users (name, email, password, role) VALUES
   ('Admin User', 'admin@chat.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYNg4oMpU7i', 'admin');
   ```

### Configuration

Edit `src/main/resources/application.properties` to match your database setup:

```properties
# Database Configuration
db.url=jdbc:mysql://localhost:3306/chat?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=your_password_here

# Server Configuration
server.host=localhost
server.port=1234
```

### Building the Project

```bash
# Clone the repository
git clone https://github.com/yourusername/JavaFX-Realtime-Chat-Group.git
cd JavaFX-Realtime-Chat-Group

# Install dependencies and build
mvn clean install
```

### Running the Application

#### Option 1: Using Maven

**Start the Server:**
```bash
mvn exec:java -Dexec.mainClass="app.server.Server"
```

**Start the Client** (in a new terminal):
```bash
mvn javafx:run
```

#### Option 2: Using JAR files

**Build JAR files:**
```bash
mvn clean package
```

**Run Server:**
```bash
java -cp target/ChatRoom-1.0-SNAPSHOT.jar app.server.Server
```

**Run Client:**
```bash
java -jar target/ChatRoom-1.0-SNAPSHOT.jar
```

### First Time Setup

1. **Start the Server** first (it will listen on port 1234)
2. **Start the Client** application
3. **Register a new account** or use the admin credentials:
   - Email: `admin@chat.com`
   - Password: `admin123`

## 📁 Project Structure

```
JavaFX-Realtime-Chat-Group/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── app/
│   │   │       ├── client/           # Client-side code
│   │   │       │   ├── db/           # Database management
│   │   │       │   │   ├── DatabaseManager.java
│   │   │       │   │   └── MyConnection.java (deprecated)
│   │   │       │   ├── views/        # UI components
│   │   │       │   │   ├── Chat.java
│   │   │       │   │   ├── Login.java
│   │   │       │   │   ├── Register.java
│   │   │       │   │   ├── ManageUsers.java
│   │   │       │   │   └── MessageBubble.java
│   │   │       │   └── Client.java   # Main client entry
│   │   │       ├── server/           # Server-side code
│   │   │       │   ├── Server.java
│   │   │       │   └── ClientHandler.java
│   │   │       ├── types/            # Data models
│   │   │       │   ├── User.java
│   │   │       │   └── Message.java
│   │   │       ├── config/           # Configuration
│   │   │       │   └── Config.java
│   │   │       └── util/             # Utilities
│   │   │           ├── PasswordUtil.java
│   │   │           └── ValidationUtil.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── styles/
│   │           └── main.css          # Application styling
│   └── test/                         # Test files (future)
├── pom.xml                           # Maven configuration
├── module-info.java                  # Java module definition
└── README.md                         # This file
```

## 🔧 Technologies Used

### Core Technologies
- **Java 17**: Modern Java with latest features
- **JavaFX 17**: Rich client UI framework
- **MySQL 8.0**: Relational database
- **Maven**: Build automation and dependency management

### Libraries & Frameworks
- **HikariCP 5.0.1**: High-performance JDBC connection pool
- **jBCrypt 0.4**: Password hashing library
- **SLF4J 2.0.7**: Logging facade
- **Jackson 2.15.2**: JSON processing (for future features)
- **MySQL Connector/J 8.0.33**: MySQL JDBC driver

### Development Tools
- **JUnit 5.8.2**: Unit testing framework (configured)
- **Maven Compiler Plugin**: Java 17 compilation
- **JavaFX Maven Plugin**: JavaFX build support

## 🎯 Key Improvements Made

This project has been significantly enhanced from a basic prototype to a professional application:

### Security Enhancements
- ✅ Fixed all SQL injection vulnerabilities
- ✅ Implemented BCrypt password hashing
- ✅ Added comprehensive input validation
- ✅ Moved credentials to configuration file
- ✅ Updated to modern MySQL connector

### Architecture Improvements
- ✅ Implemented connection pooling (HikariCP)
- ✅ Added thread pool for server (50 concurrent clients)
- ✅ Proper resource management (try-with-resources)
- ✅ Added logging throughout the application
- ✅ Configuration management system
- ✅ Proper encapsulation in model classes

### UI/UX Enhancements
- ✅ Modern CSS styling system
- ✅ Message bubble interface
- ✅ Timestamp display
- ✅ Professional login/register screens
- ✅ Enhanced admin panel
- ✅ Auto-scrolling chat window
- ✅ Placeholder text in input fields

### Code Quality
- ✅ Comprehensive JavaDoc documentation
- ✅ Consistent code formatting
- ✅ Proper exception handling
- ✅ Thread-safe collections
- ✅ Synchronized critical sections

## 📝 Usage Examples

### Regular User Flow
1. Launch the client application
2. Click "Create an account" to register
3. Fill in name, email, and password (min 8 chars, letters + numbers)
4. Login with your credentials
5. Start chatting in the group chat
6. Your messages appear on the right (blue), others on the left (white)

### Admin User Flow
1. Login with admin credentials
2. Access the User Management panel
3. View all registered users
4. Delete users if needed
5. Refresh the user list

## 🔐 Security Best Practices

This application demonstrates several security best practices:

1. **Never store plain-text passwords** - Uses BCrypt hashing
2. **Prevent SQL injection** - Uses PreparedStatements exclusively
3. **Validate all inputs** - Client and server-side validation
4. **Secure configuration** - Credentials in config files, not code
5. **Proper error handling** - No sensitive information in error messages
6. **Connection security** - Connection pooling with timeout handling

## 🚧 Future Enhancements

- [ ] Private messaging between users
- [ ] User avatars and profile pictures
- [ ] Online/offline status indicators
- [ ] File sharing capability
- [ ] Message editing and deletion
- [ ] Search message history
- [ ] Multiple chat rooms/channels
- [ ] Emoji picker
- [ ] Read receipts
- [ ] Typing indicators
- [ ] SSL/TLS encryption for socket communication
- [ ] OAuth2 integration (Google, GitHub login)
- [ ] Unit and integration tests
- [ ] Docker containerization

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👨‍💻 Author

Created as a portfolio project demonstrating:
- JavaFX desktop application development
- Client-server architecture
- Real-time socket programming
- Database design and management
- Security best practices
- Modern UI/UX design
- Professional code organization

## 🙏 Acknowledgments

- JavaFX community for excellent documentation
- HikariCP for the best-in-class connection pool
- jBCrypt for secure password hashing
- All contributors and testers

---

**Note**: This is a portfolio/educational project. For production use, consider additional security measures such as SSL/TLS encryption, rate limiting, and more comprehensive testing.
