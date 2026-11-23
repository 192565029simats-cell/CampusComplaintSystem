
package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import database.DBConnection;
import util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class StudentLoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String loginId = request.getParameter("loginId");
        String password = request.getParameter("password");

        if (loginId == null || password == null ||
            loginId.isBlank() || password.isBlank()) {

            response.sendRedirect("index.html?error=empty");
            return;
        }

        String sql = "SELECT student_id, name, password_hash " +
                     "FROM students WHERE register_number = ? OR email = ?";

        try {
            Connection con = DBConnection.getConnection();

            if (con == null) {
                response.sendError(500, "Database connection failed.");
                return;
            }

            try (con;
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, loginId);
                ps.setString(2, loginId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next() &&
                        PasswordUtil.verifyPassword(
                            password, rs.getString("password_hash"))) {

                        HttpSession session = request.getSession();
                        session.setAttribute("studentId",
                                rs.getInt("student_id"));
                        session.setAttribute("studentName",
                                rs.getString("name"));

                        response.sendRedirect("studentDashboard");
                    } else {
                        response.sendRedirect("index.html?error=invalid");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Login failed. Please try again.");
        }
    }
}
