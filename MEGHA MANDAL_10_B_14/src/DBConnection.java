import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/tour_db"; //  
    private static final String USER = "root";  
    private static final String PASSWORD = "12345";  

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Driver load karna aur connection establish karna
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check your MySQL settings.");
            e.printStackTrace(); // Exception handling as per guidelines 
        }
        return conn;
    }
}