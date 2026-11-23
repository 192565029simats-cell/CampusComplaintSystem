
package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import database.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/dbtest")
public class DatabaseTestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        try (Connection con = DBConnection.getConnection()) {

            if (con != null) {
                out.println("<h2>MySQL Connected Successfully!</h2>");
                out.println("<p>Campus Complaint Database is working.</p>");
            } else {
                out.println("<h2>Database Connection Failed!</h2>");
            }

        } catch (Exception e) {
            out.println("<h2>Database Error</h2>");
            out.println("<p>" + e.getMessage() + "</p>");
            e.printStackTrace();
        }
    }
}
