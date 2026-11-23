
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

@WebServlet("/adminFeedback")
public class AdminFeedbackServlet extends HttpServlet {

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

        String sql =
            "SELECT f.feedback_id, f.complaint_id, s.name, " +
            "s.register_number, f.rating, f.comments, f.feedback_date " +
            "FROM feedback f " +
            "JOIN complaints c ON f.complaint_id = c.complaint_id " +
            "JOIN students s ON c.student_id = s.student_id " +
            "ORDER BY f.feedback_date DESC";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                response.sendError(500, "Database connection failed.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                var out = response.getWriter();

                out.println("<html><head><title>Student Feedback</title>");
                out.println("<style>");
                out.println("body{font-family:Arial;background:#f1f5f9;padding:30px;}");
                out.println(".box{background:white;padding:25px;border-radius:10px;}");
                out.println("table{width:100%;border-collapse:collapse;}");
                out.println("th,td{padding:12px;border:1px solid #ddd;text-align:center;}");
                out.println("th{background:#2563eb;color:white;}");
                out.println("a{display:inline-block;margin-top:20px;padding:10px 18px;background:#2563eb;color:white;text-decoration:none;border-radius:5px;}");
                out.println("</style></head><body>");

                out.println("<div class='box'>");
                out.println("<h2>Student Feedback Details</h2>");
                out.println("<table>");

                out.println("<tr><th>Feedback ID</th>");
                out.println("<th>Complaint ID</th><th>Student Name</th>");
                out.println("<th>Register Number</th><th>Rating</th>");
                out.println("<th>Comments</th><th>Feedback Date</th></tr>");

                boolean found = false;

                while (rs.next()) {
                    found = true;

                    out.println("<tr>");
                    out.println("<td>" + rs.getInt("feedback_id") + "</td>");
                    out.println("<td>" + rs.getInt("complaint_id") + "</td>");
                    out.println("<td>" + escapeHtml(rs.getString("name")) + "</td>");
                    out.println("<td>" + escapeHtml(rs.getString("register_number")) + "</td>");
                    out.println("<td>" + rs.getInt("rating") + "/5</td>");
                    out.println("<td>" + escapeHtml(rs.getString("comments")) + "</td>");
                    out.println("<td>" + rs.getTimestamp("feedback_date") + "</td>");
                    out.println("</tr>");
                }

                if (!found) {
                    out.println("<tr><td colspan='7'>No feedback available.</td></tr>");
                }

                out.println("</table>");
                out.println("<a href='adminDashboard'>Back to Dashboard</a>");
                out.println("</div></body></html>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Unable to load feedback.");
        }
    }

    private String escapeHtml(String value) {
        if (value == null) return "-";

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
