package framework;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class FrontServlet extends HttpServlet {

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {

        String url = request.getRequestURI();

        response.setContentType("text/plain");

        response.getWriter().println(
            "URL recue : " + url
        );
    }
}