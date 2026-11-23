
package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import database.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/submitComplaint")
public class SubmitComplaintServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("studentId") == null) {

            response.sendRedirect("index.html");
            return;
        }

        int studentId = (Integer) session.getAttribute("studentId");

        String title = request.getParameter("title");
        String category = request.getParameter("category");
        String description = request.getParameter("description");
        String location = request.getParameter("location");
        String priority = request.getParameter("priority");

        if (title == null || category == null ||
            description == null || title.isBlank() ||
            category.isBlank() || description.isBlank()) {

            response.sendError(400, "Please complete all required fields.");
            return;
        }

        if (title.length() > 150 || description.length() > 5000 ||
            (location != null && location.length() > 150)) {

            response.sendError(400, "Input is too long.");
            return;
        }

        if (!priority.equals("Low") &&
            !priority.equals("Medium") &&
            !priority.equals("High") &&
            !priority.equals("Urgent")) {

            response.sendError(400, "Invalid priority.");
            return;
        }

        String sql = "INSERT INTO complaints " +
            "(student_id, title, category, description, location, priority) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection con = DBConnection.getConnection();

            if (con == null) {
                response.sendError(500, "Database connection failed.");
                return;
            }

            try (con;
                 PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, studentId);
                ps.setString(2, title.trim());
                ps.setString(3, category);
                ps.setString(4, description.trim());
                ps.setString(5,
                    location == null || location.isBlank()
                        ? null : location.trim());
                ps.setString(6, priority);

                int result = ps.executeUpdate();

                if (result > 0) {
                    response.setContentType("text/html;charset=UTF-8");

                    response.getWriter().println(
                        "<h2>Complaint Submitted Successfully!</h2>" +
                        "<p>Your complaint has been recorded.</p>" +
                        "<a href='studentDashboard'>Back to Dashboard</a>"
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500,
                "Unable to submit complaint. Please try again.");
        }
    }
}
