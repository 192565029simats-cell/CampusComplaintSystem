
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

@WebServlet("/updateComplaintStatus")
public class UpdateComplaintStatusServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("adminLogin.html");
            return;
        }

        String idValue = request.getParameter("complaintId");
        String status = request.getParameter("status");

        if (idValue == null || status == null) {
            response.sendError(400, "Missing complaint details.");
            return;
        }

        int complaintId;

        try {
            complaintId = Integer.parseInt(idValue);
        } catch (NumberFormatException e) {
            response.sendError(400, "Invalid complaint ID.");
            return;
        }

        if (complaintId <= 0 ||
            !(status.equals("Pending") ||
              status.equals("Assigned") ||
              status.equals("In Progress") ||
              status.equals("Resolved") ||
              status.equals("Rejected"))) {

            response.sendError(400, "Invalid complaint status.");
            return;
        }

        String adminName = (String) session.getAttribute("adminUsername");

        if (adminName == null) {
            adminName = "Admin";
        }

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                response.sendError(500, "Database connection failed.");
                return;
            }

            con.setAutoCommit(false);

            try {
                String oldStatus = null;

                String selectSql =
                    "SELECT status FROM complaints " +
                    "WHERE complaint_id = ? FOR UPDATE";

                try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                    ps.setInt(1, complaintId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            oldStatus = rs.getString("status");
                        } else {
                            con.rollback();
                            response.sendError(404, "Complaint not found.");
                            return;
                        }
                    }
                }

                if (!status.equals(oldStatus)) {

                    String updateSql =
                        "UPDATE complaints SET status = ?, " +
                        "resolved_at = CASE WHEN ? = 'Resolved' " +
                        "THEN CURRENT_TIMESTAMP ELSE NULL END " +
                        "WHERE complaint_id = ?";

                    try (PreparedStatement ps =
                            con.prepareStatement(updateSql)) {

                        ps.setString(1, status);
                        ps.setString(2, status);
                        ps.setInt(3, complaintId);
                        ps.executeUpdate();
                    }

                    String historySql =
                        "INSERT INTO complaint_updates " +
                        "(complaint_id, updated_by, old_status, " +
                        "new_status, remarks) VALUES (?, ?, ?, ?, ?)";

                    try (PreparedStatement ps =
                            con.prepareStatement(historySql)) {

                        ps.setInt(1, complaintId);
                        ps.setString(2, adminName);
                        ps.setString(3, oldStatus);
                        ps.setString(4, status);
                        ps.setString(5,
                            "Complaint status changed by admin.");

                        ps.executeUpdate();
                    }
                }

                con.commit();
                response.sendRedirect("allComplaints");

            } catch (Exception e) {
                con.rollback();
                throw e;
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500,
                "Unable to update complaint status.");
        }
    }
}
