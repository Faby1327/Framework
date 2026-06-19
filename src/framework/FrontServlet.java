package framework;

import jakarta.servlet.http.*;
import jakarta.servlet.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FrontServlet extends HttpServlet {

    private List<Class<?>> controllers;

    @Override
    public void init() throws ServletException {

        String rootPath =
                getServletContext()
                        .getRealPath("/WEB-INF/classes");

        String controllerPackage =
                getInitParameter("controller-package");

        String scanPath =
                rootPath + File.separator + controllerPackage;

        ScannerClasse scanner =
                new ScannerClasse(rootPath);

        controllers =
                scanner.scan(
                        new File(scanPath),
                        Controller.class,
                        ScanType.CLASS
                );

    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/plain");

        for (Class<?> c : controllers) {
            resp.getWriter().println(c.getName());
        }
    }
}