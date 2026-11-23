
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

@WebServlet("/adminLogin")
public class AdminLoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        String sql = "SELECT admin_id, username, password_hash " +
                     "FROM admins WHERE username = ?";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                response.sendError(500, "Database connection failed.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next() && PasswordUtil.verifyPassword(
                            password, rs.getString("password_hash"))) {

                        HttpSession session = request.getSession(true);

                        session.setAttribute("adminId",
                                rs.getInt("admin_id"));

                        session.setAttribute("adminUsername",
                                rs.getString("username"));

                        response.sendRedirect("adminDashboard");

                    } else {
                        response.sendRedirect(
                                "adminLogin.html?error=invalid");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Admin login failed.");
        }
    }
}

