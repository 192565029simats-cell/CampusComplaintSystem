
package servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/studentDashboard")
public class StudentDashboardServlet extends HttpServlet {

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

        String studentName =
                (String) session.getAttribute("studentName");

        if (studentName == null) {
            studentName = "Student";
        }

        String safeName = studentName
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");

        response.getWriter().println(
            "<!DOCTYPE html>" +
            "<html><head><title>Student Dashboard</title>" +
            "<style>" +
            "body{font-family:Arial;background:#f1f5f9;text-align:center;padding:40px;}" +
            ".box{background:white;padding:35px;margin:auto;max-width:600px;border-radius:12px;}" +
            "h1{color:#1d4ed8;}" +
            "a{display:inline-block;background:#2563eb;color:white;padding:12px 22px;" +
            "margin:10px;text-decoration:none;border-radius:6px;}" +
            "</style></head><body>" +
            "<div class='box'>" +
            "<h1>Welcome, " + safeName + "!</h1>" +
            "<h2>Student Dashboard</h2>" +
            "<p>Welcome to Digital Campus Complaint Tracking and Resolution System.</p>" +
            "<a href='submitComplaint.html'>Submit Complaint</a>" +
            "<a href='viewComplaints'>View Complaints</a>" +
            "<a href='trackComplaint'>Track My Complaints</a>" +
            "<a href='complaintHistory'>View Complaint History</a>" +
            "<a href='feedback.html'>Submit Feedback</a>" +
            "<a href='logout'>Logout</a>" +
            "</div></body></html>"
        );
    }
}
