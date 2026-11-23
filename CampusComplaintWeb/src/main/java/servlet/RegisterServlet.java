
package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import database.DBConnection;
import util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String name = request.getParameter("name");
        String registerNumber = request.getParameter("registerNumber");
        String department = request.getParameter("department");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");

        if (name == null || registerNumber == null ||
            email == null || password == null ||
            name.isBlank() || registerNumber.isBlank() ||
            email.isBlank() || password.isBlank()) {

            response.getWriter().println("Please fill all required fields.");
            return;
        }

        String sql = "INSERT INTO students " +
            "(name, register_number, department, email, phone, password_hash) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            String hashedPassword = PasswordUtil.hashPassword(password);

            Connection con = DBConnection.getConnection();

            if (con == null) {
                response.getWriter().println("Database connection failed.");
                return;
            }

            try (con;
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, name);
                ps.setString(2, registerNumber);
                ps.setString(3, department);
                ps.setString(4, email);
                ps.setString(5, phone);
                ps.setString(6, hashedPassword);

                ps.executeUpdate();

                response.getWriter().println(
                    "<h2>Registration Successful!</h2>" +
                    "<a href='index.html'>Go to Login</a>"
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();

            if ("23000".equals(e.getSQLState())) {
                response.getWriter().println(
                    "Register number or email already exists."
                );
            } else {
                response.getWriter().println(
                    "Registration failed. Please try again."
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println(
                "Registration failed. Please try again."
            );
        }
    }
}
