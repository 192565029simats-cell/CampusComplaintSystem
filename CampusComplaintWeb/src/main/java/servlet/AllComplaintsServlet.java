
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

@WebServlet("/allComplaints")
public class AllComplaintsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("adminId") == null) {
            response.sendRedirect("adminLogin.html");
            return;
        }

        String sql =
            "SELECT c.complaint_id, s.name, s.register_number, " +
            "c.title, c.category, c.priority, c.status, c.created_at " +
            "FROM complaints c " +
            "JOIN students s ON c.student_id = s.student_id " +
            "ORDER BY c.created_at DESC";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                response.sendError(500, "Database connection failed.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                var out = response.getWriter();

                out.println("<html><head><title>All Complaints</title>");
                out.println("<style>");
                out.println("body{font-family:Arial;background:#f1f5f9;padding:25px;}");
                out.println("table{width:100%;background:white;border-collapse:collapse;}");
                out.println("th,td{padding:12px;border:1px solid #ddd;text-align:left;}");
                out.println("th{background:#2563eb;color:white;}");
                out.println("h2{color:#1d4ed8;}");
                out.println("</style></head><body>");

                out.println("<h2>All Student Complaints</h2>");
                out.println("<table>");
                out.println("<tr><th>ID</th><th>Student</th>" +
                    "<th>Register Number</th><th>Title</th>" +
                    "<th>Category</th><th>Priority</th>" +
                    "<th>Status</th><th>Date</th></tr>");

                boolean found = false;

                while (rs.next()) {
                    found = true;

                    out.println("<tr>");
                    out.println("<td>" + rs.getInt("complaint_id") + "</td>");
                    out.println("<td>" + escapeHtml(rs.getString("name")) + "</td>");
                    out.println("<td>" + escapeHtml(rs.getString("register_number")) + "</td>");
                    out.println("<td>" + escapeHtml(rs.getString("title")) + "</td>");
                    out.println("<td>" + escapeHtml(rs.getString("category")) + "</td>");
                    out.println("<td>" + escapeHtml(rs.getString("priority")) + "</td>");
                    out.println("<td>");

                    out.println("<form action='updateComplaintStatus' method='post'>");

                    out.println("<input type='hidden' name='complaintId' value='" +
                        rs.getInt("complaint_id") + "'>");

                    out.println("<select name='status'>");

                    String currentStatus = rs.getString("status");

                    String[] statuses = {
                        "Pending", "Assigned", "In Progress", "Resolved", "Rejected"
                    };

                    for (String s : statuses) {
                        out.println("<option value='" + s + "' " +
                            (s.equals(currentStatus) ? "selected" : "") +
                            ">" + s + "</option>");
                    }

                    out.println("</select>");

                    out.println("<button type='submit'>Update</button>");

                    out.println("</form>");

                    out.println("</td>");
                    out.println("<td>" + rs.getTimestamp("created_at") + "</td>");
                    out.println("</tr>");
                }

                if (!found) {
                    out.println("<tr><td colspan='8'>No complaints found.</td></tr>");
                }

out.println("<tr><td colspan='8'>");

out.println("<form action='assignDepartment' method='post'>");

out.println("Complaint ID: ");
out.println("<input type='number' name='complaintId' required>");

out.println("Department: ");
out.println("<select name='department' required>");

out.println("<option value=''>Select Department</option>");
out.println("<option>Computer Science</option>");
out.println("<option>Electrical Maintenance</option>");
out.println("<option>Civil Maintenance</option>");
out.println("<option>Hostel Management</option>");
out.println("<option>Transport</option>");
out.println("<option>General Administration</option>");

out.println("</select>");

out.println("<button type='submit'>Assign Department</button>");

out.println("</form>");
out.println("</td></tr>");

                out.println("</table>");
                out.println("<p><a href='adminDashboard'>Back to Dashboard</a></p>");
                out.println("</body></html>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Unable to retrieve complaints.");
        }
    }

    private String escapeHtml(String value) {
        if (value == null) return "";

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
}
