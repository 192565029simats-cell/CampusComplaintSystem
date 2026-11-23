
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

@WebServlet("/trackComplaint")
public class TrackComplaintServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("studentId") == null) {
            response.sendRedirect("index.html");
            return;
        }

        int studentId = (Integer) session.getAttribute("studentId");

        String sql = "SELECT complaint_id, title, category, " +
                     "assigned_department, status, created_at, resolved_at " +
                     "FROM complaints WHERE student_id = ? " +
                     "ORDER BY created_at DESC";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                response.sendError(500, "Database connection failed.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, studentId);

                try (ResultSet rs = ps.executeQuery()) {

                    var out = response.getWriter();

                    out.println("<html><head><title>Track Complaint</title>");
                    out.println("<style>");
                    out.println("body{font-family:Arial;background:#f1f5f9;padding:25px;}");
                    out.println("table{width:100%;background:white;border-collapse:collapse;}");
                    out.println("th,td{padding:12px;border:1px solid #ddd;text-align:left;}");
                    out.println("th{background:#2563eb;color:white;}");
                    out.println("h2{color:#1d4ed8;}");
                    out.println("</style></head><body>");

                    out.println("<h2>Track My Complaints</h2>");
                    out.println("<table>");
                    out.println("<tr><th>ID</th><th>Title</th>" +
                        "<th>Category</th><th>Department</th>" +
                        "<th>Status</th><th>Submitted</th>" +
                        "<th>Resolved</th></tr>");

                    boolean found = false;

                    while (rs.next()) {
                        found = true;

                        out.println("<tr>");
                        out.println("<td>" + rs.getInt("complaint_id") + "</td>");
                        out.println("<td>" + escapeHtml(rs.getString("title")) + "</td>");
                        out.println("<td>" + escapeHtml(rs.getString("category")) + "</td>");
                        out.println("<td>" + escapeHtml(rs.getString("assigned_department")) + "</td>");
                        out.println("<td>" + escapeHtml(rs.getString("status")) + "</td>");
                        out.println("<td>" + rs.getTimestamp("created_at") + "</td>");
                        out.println("<td>" + rs.getTimestamp("resolved_at") + "</td>");
                        out.println("</tr>");
                    }

                    if (!found) {
                        out.println("<tr><td colspan='7'>No complaints found.</td></tr>");
                    }

                    out.println("</table>");
                    out.println("<p><a href='studentDashboard'>Back to Dashboard</a></p>");
                    out.println("</body></html>");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Unable to track complaints.");
        }
    }

    private String escapeHtml(String value) {
        if (value == null) return "-";

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
}
