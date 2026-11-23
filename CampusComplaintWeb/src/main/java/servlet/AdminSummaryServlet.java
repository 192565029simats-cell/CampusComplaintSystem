
package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import database.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/adminSummary")
public class AdminSummaryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("adminLogin.html");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        int total = 0;
        int pending = 0;
        int assigned = 0;
        int inProgress = 0;
        int resolved = 0;
        int rejected = 0;
        int totalFeedback = 0;
        double averageRating = 0.0;

        String complaintSql =
            "SELECT COUNT(*) AS total, " +
            "SUM(status='Pending') AS pending, " +
            "SUM(status='Assigned') AS assigned, " +
            "SUM(status='In Progress') AS in_progress, " +
            "SUM(status='Resolved') AS resolved, " +
            "SUM(status='Rejected') AS rejected " +
            "FROM complaints";

        String feedbackSql =
            "SELECT COUNT(*) AS total_feedback, " +
            "AVG(rating) AS average_rating FROM feedback";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new ServletException("Database connection failed.");
            }

            try (PreparedStatement ps = con.prepareStatement(complaintSql);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    total = rs.getInt("total");
                    pending = rs.getInt("pending");
                    assigned = rs.getInt("assigned");
                    inProgress = rs.getInt("in_progress");
                    resolved = rs.getInt("resolved");
                    rejected = rs.getInt("rejected");
                }
            }

            try (PreparedStatement ps = con.prepareStatement(feedbackSql);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    totalFeedback = rs.getInt("total_feedback");
                    averageRating = rs.getDouble("average_rating");
                }
            }

        } catch (Exception e) {
            throw new ServletException("Unable to load admin summary.", e);
        }

        response.getWriter().println(
            "<!DOCTYPE html>" +
            "<html><head><title>Admin Summary</title>" +
            "<style>" +
            "body{font-family:Arial;background:#f1f5f9;text-align:center;padding:30px;}" +
            ".container{max-width:850px;margin:auto;background:white;padding:25px;border-radius:12px;}" +
            ".cards{display:grid;grid-template-columns:repeat(3,1fr);gap:15px;}" +
            ".card{background:#dbeafe;padding:20px;border-radius:10px;}" +
            ".number{font-size:30px;font-weight:bold;color:#1d4ed8;}" +
            "a{display:inline-block;background:#2563eb;color:white;padding:12px 20px;" +
            "margin:20px;text-decoration:none;border-radius:6px;}" +
            "@media(max-width:600px){.cards{grid-template-columns:repeat(2,1fr);}}" +
            "</style></head><body>" +

            "<div class='container'>" +
            "<h1>Admin Dashboard Summary</h1>" +
            "<h2>Complaint Statistics</h2>" +

            "<div class='cards'>" +

            "<div class='card'><h3>Total Complaints</h3>" +
            "<div class='number'>" + total + "</div></div>" +

            "<div class='card'><h3>Pending</h3>" +
            "<div class='number'>" + pending + "</div></div>" +

            "<div class='card'><h3>Assigned</h3>" +
            "<div class='number'>" + assigned + "</div></div>" +

            "<div class='card'><h3>In Progress</h3>" +
            "<div class='number'>" + inProgress + "</div></div>" +

            "<div class='card'><h3>Resolved</h3>" +
            "<div class='number'>" + resolved + "</div></div>" +

            "<div class='card'><h3>Rejected</h3>" +
            "<div class='number'>" + rejected + "</div></div>" +

            "<div class='card'><h3>Total Feedback</h3>" +
            "<div class='number'>" + totalFeedback + "</div></div>" +

            "<div class='card'><h3>Average Rating</h3>" +
            "<div class='number'>" +
            String.format("%.2f / 5", averageRating) +
            "</div></div>" +

            "</div>" +

            "<a href='adminDashboard'>Back to Admin Dashboard</a>" +
            "</div></body></html>"
        );
    }
}
