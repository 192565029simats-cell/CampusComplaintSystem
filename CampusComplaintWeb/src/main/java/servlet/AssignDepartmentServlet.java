
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

@WebServlet("/assignDepartment")
public class AssignDepartmentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("adminId") == null) {
            response.sendRedirect("adminLogin.html");
            return;
        }

        String complaintValue = request.getParameter("complaintId");
        String department = request.getParameter("department");

        if (complaintValue == null || department == null) {
            response.sendError(400, "Missing details.");
            return;
        }

        int complaintId;

        try {
            complaintId = Integer.parseInt(complaintValue);
        } catch (NumberFormatException e) {
            response.sendError(400, "Invalid complaint ID.");
            return;
        }

        String sql = "UPDATE complaints SET assigned_department = ?, " +
                     "department_id = ?, status = 'Assigned' " +
                     "WHERE complaint_id = ?";

        String findDepartment =
                "SELECT department_id FROM departments " +
                "WHERE department_name = ?";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                response.sendError(500, "Database connection failed.");
                return;
            }

            try (PreparedStatement find =
                    con.prepareStatement(findDepartment)) {

                find.setString(1, department);

                try (var rs = find.executeQuery()) {

                    if (!rs.next()) {
                        response.sendError(400, "Invalid department.");
                        return;
                    }

                    int departmentId = rs.getInt("department_id");

                    try (PreparedStatement ps =
                            con.prepareStatement(sql)) {

                        ps.setString(1, department);
                        ps.setInt(2, departmentId);
                        ps.setInt(3, complaintId);

                        int result = ps.executeUpdate();

                        if (result > 0) {
                            response.sendRedirect("allComplaints");
                        } else {
                            response.sendError(404, "Complaint not found.");
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Unable to assign department.");
        }
    }
}
