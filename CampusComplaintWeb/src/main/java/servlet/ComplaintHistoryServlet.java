
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

@WebServlet("/complaintHistory")
public class ComplaintHistoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("studentId") == null) {
            response.sendRedirect("index.html");
            return;
        }

        int studentId = (Integer) session.getAttribute("studentId");

        String sql =
            "SELECT cu.complaint_id, c.title, cu.old_status, " +
            "cu.new_status, cu.updated_by, cu.remarks, cu.updated_at " +
            "FROM complaint_updates cu " +
            "JOIN complaints c ON cu.complaint_id = c.complaint_id " +
            "WHERE c.student_id = ? " +
            "ORDER BY cu.updated_at DESC";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                response.sendError(500, "Database connection failed.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, studentId);

                try (ResultSet rs = ps.executeQuery()) {

                    var out = response.getWriter();

                    out.println("<html><head><title>Complaint History</title>");
                    out.println("<style>");
                    out.println("body{font-family:Arial;background:#f1f5f9;padding:30px;}");
                    out.println(".box{background:white;padding:25px;border-radius:10px;}");
                    out.println("table{width:100%;border-collapse:collapse;}");
                    out.println("th,td{padding:12px;border:1px solid #ddd;text-align:center;}");
                    out.println("th{background:#2563eb;color:white;}");
                    out.println("a{display:inline-block;margin-top:20px;padding:10px 18px;background:#2563eb;color:white;text-decoration:none;border-radius:5px;}");
                    out.println("</style></head><body>");

                    out.println("<div class='box'>");
                    out.println("<h2>My Complaint Status History</h2>");
                    out.println("<table>");
                    out.println("<tr><th>Complaint ID</th><th>Title</th>");
                    out.println("<th>Old Status</th><th>New Status</th>");
                    out.println("<th>Updated By</th><th>Remarks</th>");
                    out.println("<th>Updated At</th></tr>");

                    boolean found = false;

                    while (rs.next()) {
                        found = true;

                        out.println("<tr>");
                        out.println("<td>" + rs.getInt("complaint_id") + "</td>");
                        out.println("<td>" + escapeHtml(rs.getString("title")) + "</td>");
                        out.println("<td>" + escapeHtml(rs.getString("old_status")) + "</td>");
                        out.println("<td>" + escapeHtml(rs.getString("new_status")) + "</td>");
                        out.println("<td>" + escapeHtml(rs.getString("updated_by")) + "</td>");
                        out.println("<td>" + escapeHtml(rs.getString("remarks")) + "</td>");
                        out.println("<td>" + rs.getTimestamp("updated_at") + "</td>");
                        out.println("</tr>");
                    }

                    if (!found) {
                        out.println("<tr><td colspan='7'>No complaint history available.</td></tr>");
                    }

                    out.println("</table>");
                    out.println("<a href='studentDashboard'>Back to Dashboard</a>");
                    out.println("</div></body></html>");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Unable to load complaint history.");
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
