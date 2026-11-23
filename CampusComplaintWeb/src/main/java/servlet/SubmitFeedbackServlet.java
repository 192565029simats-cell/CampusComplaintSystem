
package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import database.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/submitFeedback")
public class SubmitFeedbackServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("studentId") == null) {
            response.sendRedirect("index.html");
            return;
        }

        int studentId = (Integer) session.getAttribute("studentId");

        String complaintValue = request.getParameter("complaintId");
        String ratingValue = request.getParameter("rating");
        String comments = request.getParameter("comments");

        int complaintId;
        int rating;

        try {
            complaintId = Integer.parseInt(complaintValue);
            rating = Integer.parseInt(ratingValue);
        } catch (Exception e) {
            response.sendError(400, "Invalid complaint ID or rating.");
            return;
        }

        if (complaintId <= 0 || rating < 1 || rating > 5 ||
                comments == null || comments.trim().isEmpty()) {
            response.sendError(400, "Please enter valid feedback details.");
            return;
        }

        String checkSql =
            "SELECT complaint_id FROM complaints " +
            "WHERE complaint_id = ? AND student_id = ? " +
            "AND status = 'Resolved'";

        String insertSql =
            "INSERT INTO feedback (complaint_id, rating, comments) " +
            "VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                response.sendError(500, "Database connection failed.");
                return;
            }

            try (PreparedStatement check =
                    con.prepareStatement(checkSql)) {

                check.setInt(1, complaintId);
                check.setInt(2, studentId);

                try (var rs = check.executeQuery()) {

                    if (!rs.next()) {
                        response.sendError(400,
                            "Feedback is allowed only for your resolved complaints.");
                        return;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(insertSql)) {

                ps.setInt(1, complaintId);
                ps.setInt(2, rating);
                ps.setString(3, comments.trim());

                ps.executeUpdate();
            }

            response.setContentType("text/html;charset=UTF-8");

            response.getWriter().println(
                "<h2>Feedback Submitted Successfully!</h2>" +
                "<a href='studentDashboard'>Back to Dashboard</a>"
            );

        } catch (Exception e) {
            e.printStackTrace();

            response.sendError(500,
                "Unable to submit feedback. You may have already submitted feedback for this complaint.");
        }
    }
}
