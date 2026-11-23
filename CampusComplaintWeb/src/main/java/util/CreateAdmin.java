
package util;

import java.sql.Connection;
import java.sql.PreparedStatement;

import database.DBConnection;

public class CreateAdmin {

    public static void main(String[] args) {

        String username = "admin";

        // Change this to your new admin password
        String password = "naveen@9696";

        try {
            String hashedPassword =
                    PasswordUtil.hashPassword(password);

            String sql =
                    "UPDATE admins SET password_hash = ? WHERE username = ?";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                if (con == null) {
                    System.out.println("Database connection failed.");
                    return;
                }

                ps.setString(1, hashedPassword);
                ps.setString(2, username);

                int result = ps.executeUpdate();

                if (result > 0) {
                    System.out.println("Admin password updated successfully!");
                } else {
                    System.out.println("Admin account not found.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
