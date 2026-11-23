
package servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/adminDashboard")
public class AdminDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("adminLogin.html");
            return;
        }

        String adminName =
                (String) session.getAttribute("adminUsername");

        if (adminName == null) {
            adminName = "Admin";
        }

        String safeName = adminName
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");

        response.getWriter().println(
            "<!DOCTYPE html>" +
            "<html><head><title>Admin Dashboard</title>" +
            "<style>" +
            "body{font-family:Arial;background:#f1f5f9;text-align:center;padding:40px;}" +
            ".box{background:white;padding:35px;margin:auto;max-width:650px;border-radius:12px;}" +
            "h1{color:#1d4ed8;}" +
            "a{display:inline-block;background:#2563eb;color:white;padding:12px 22px;" +
            "margin:10px;text-decoration:none;border-radius:6px;}" +
            "a:hover{background:#1e40af;}" +
            "</style></head><body>" +

            "<div class='box'>" +
            "<h1>Welcome, " + safeName + "!</h1>" +
            "<h2>Admin Dashboard</h2>" +
            "<p>Digital Campus Complaint Tracking and Resolution System</p>" +

            "<a href='allComplaints'>View All Complaints</a>" +
            "<a href='adminSummary'>View Dashboard Summary</a>" +

            "<a href='adminFeedback'>View Student Feedback</a>" +

            "<a href='adminLogout'>Logout</a>" +

            "</div></body></html>"
        );
    }
}
