package framework;

import jakarta.servlet.http.*;
import jakarta.servlet.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.lang.reflect.Method;

public class FrontServlet extends HttpServlet {

    private List<Class<?>> controllers;
    private HashMap<String, Mapping> urls;

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
        urls = new HashMap<>();

        for(Class<?> c : controllers)
        {
        Method[] methodes =
                c.getDeclaredMethods();

        for(Method m : methodes)
        {
                if(m.isAnnotationPresent(Url.class))
                {
                Url url =
                        m.getAnnotation(
                                Url.class
                        );

                urls.put(
                        url.value(),
                        new Mapping(c, m)
                );
                }
        }
        }

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
        String url =
        req.getRequestURI();

        String contexte =
                req.getContextPath();

        url =
                url.substring(
                        contexte.length()
                );

        Mapping mapping =
        urls.get(url);


        if(mapping != null)
        {
        resp.getWriter().println(
                "Classe : "
                + mapping.getClasse().getName()
        );

        resp.getWriter().println(
                "Méthode : "
                + mapping.getMethode().getName()
        );
        }
        else
        {
        resp.getWriter().println(
                "URL introuvable.\n"
        );

        resp.getWriter().println(
                "Les URL disponibles :\n"
        );

        for(String cle : urls.keySet())
        {
                Mapping m = urls.get(cle);

                resp.getWriter().println(
                        cle
                        + " -> "
                        + m.getClasse().getName()
                        + "."
                        + m.getMethode().getName()
                );
        }
        }


    }
}