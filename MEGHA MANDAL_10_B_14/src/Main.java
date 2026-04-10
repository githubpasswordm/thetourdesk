import java.util.Scanner;
import java.sql.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice;

        while (true) {
            // Menu-driven interface as per guidelines [cite: 57, 62-67]
            System.out.println("\n===== Tour Booking System =====");
            System.out.println("1. Add Tour Package");
            System.out.println("2. Book Seats");
            System.out.println("3. Cancel Booking");
            System.out.println("4. View Available Tours");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");
            
            if (!sc.hasNextInt()) {
                System.out.println("Please enter a valid number.");
                sc.next(); 
                continue;
            }

            choice = sc.nextInt();
            sc.nextLine(); // Buffer clear

            switch (choice) {
                case 1: addPackage(sc); break;
                case 2: bookSeats(sc); break;
                case 3: cancelBooking(sc); break;
                case 4: viewTours(); break;
                case 5: 
                    System.out.println("Exiting... Thank you!");
                    System.exit(0);
                default: System.out.println("Invalid choice! Try again.");
            }
        }
    }

    // 1. ADD TOUR PACKAGE [cite: 54]
    public static void addPackage(Scanner sc) {
        System.out.print("Enter Destination: ");
        String dest = sc.nextLine();
        System.out.print("Enter Departure Date (YYYY-MM-DD): ");
        String date = sc.next();
        System.out.print("Enter Total Seats: ");
        int seats = sc.nextInt();
        System.out.print("Enter Price per Person: ");
        double price = sc.nextDouble();

        String sql = "INSERT INTO packages (destination, depart_date, total_seats, price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dest);
            pstmt.setString(2, date);
            pstmt.setInt(3, seats);
            pstmt.setDouble(4, price);
            pstmt.executeUpdate();
            System.out.println("Tour Package added successfully!");
        } catch (SQLException e) { 
            System.out.println("Error adding package.");
            e.printStackTrace(); 
        }
    }

    // 2. BOOK SEATS (With Availability Check) [cite: 54]
    public static void bookSeats(Scanner sc) {
        System.out.print("Package ID: ");
        int pkgId = sc.nextInt();
        System.out.print("Customer Name: ");
        sc.nextLine(); 
        String customer = sc.nextLine();
        System.out.print("Seats: ");
        int seatsToBook = sc.nextInt();

        try (Connection conn = DBConnection.getConnection()) {
            // Check availability: booked_seats + seats <= total_seats [cite: 54]
            String checkSql = "SELECT total_seats, booked_seats, price FROM packages WHERE pkg_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, pkgId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total_seats");
                int booked = rs.getInt("booked_seats");
                double price = rs.getDouble("price");

                if (booked + seatsToBook <= total) {
                    // Record booking
                    String bookSql = "INSERT INTO bookings (pkg_id, customer, seats) VALUES (?, ?, ?)";
                    PreparedStatement bookStmt = conn.prepareStatement(bookSql);
                    bookStmt.setInt(1, pkgId);
                    bookStmt.setString(2, customer);
                    bookStmt.setInt(3, seatsToBook);
                    bookStmt.executeUpdate();

                    // Update package seat count
                    String updateSql = "UPDATE packages SET booked_seats = booked_seats + ? WHERE pkg_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setInt(1, seatsToBook);
                    updateStmt.setInt(2, pkgId);
                    updateStmt.executeUpdate();

                    System.out.println("Booking confirmed! Total: Rs " + (seatsToBook * price));
                } else {
                    System.out.println("Not enough seats available!");
                }
            } else {
                System.out.println("Package ID not found!");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 3. CANCEL BOOKING [cite: 54]
    public static void cancelBooking(Scanner sc) {
        System.out.print("Enter Booking ID to cancel: ");
        int bookId = sc.nextInt();

        try (Connection conn = DBConnection.getConnection()) {
            String findSql = "SELECT pkg_id, seats FROM bookings WHERE book_id = ? AND cancelled = FALSE";
            PreparedStatement findStmt = conn.prepareStatement(findSql);
            findStmt.setInt(1, bookId);
            ResultSet rs = findStmt.executeQuery();

            if (rs.next()) {
                int pkgId = rs.getInt("pkg_id");
                int seatsToRestore = rs.getInt("seats");

                // Update booking status to cancelled [cite: 54]
                String cancelSql = "UPDATE bookings SET cancelled = TRUE WHERE book_id = ?";
                PreparedStatement cancelStmt = conn.prepareStatement(cancelSql);
                cancelStmt.setInt(1, bookId);
                cancelStmt.executeUpdate();

                // Restore seats in packages table [cite: 54]
                String restoreSql = "UPDATE packages SET booked_seats = booked_seats - ? WHERE pkg_id = ?";
                PreparedStatement restoreStmt = conn.prepareStatement(restoreSql);
                restoreStmt.setInt(1, seatsToRestore);
                restoreStmt.setInt(2, pkgId);
                restoreStmt.executeUpdate();

                System.out.println("Booking cancelled and seats restored!");
            } else {
                System.out.println("Invalid Booking ID or already cancelled.");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 4. VIEW AVAILABLE TOURS [cite: 54]
    public static void viewTours() {
        // Only show upcoming tours where seats are available [cite: 54]
        String sql = "SELECT * FROM packages WHERE depart_date >= CURDATE() AND booked_seats < total_seats";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\nID | Destination | Date | Seats (Total/Booked) | Price");
            while (rs.next()) {
                System.out.println(rs.getInt("pkg_id") + " | " + rs.getString("destination") + " | " + 
                    rs.getDate("depart_date") + " | " + rs.getInt("total_seats") + "/" + 
                    rs.getInt("booked_seats") + " | Rs " + rs.getDouble("price"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}