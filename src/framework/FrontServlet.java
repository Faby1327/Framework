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
    private HashMap<HttpKey, Mapping> urls;

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

        for (Class<?> c : controllers) {

            Method[] methods = c.getDeclaredMethods();

            for (Method m : methods) {

                if (m.isAnnotationPresent(Url.class)) {

                    Url url = m.getAnnotation(Url.class);

                    HttpKey key = new HttpKey(
                            url.value(),
                            url.method()
                    );

                    if (urls.containsKey(key)) {
                        throw new ServletException(
                                "URL dupliquée : "
                                + key.getMethod() + " " + key.getUrl()
                                + "\nDéjà associée à : "
                                + urls.get(key).getClasse().getName()
                                + "."
                                + urls.get(key).getMethode().getName()
                                + "\nNouvelle méthode : "
                                + c.getName()
                                + "."
                                + m.getName()
                        );
                    }

                    urls.put(
                            key,
                            new Mapping(c, m)
                    );
                }
            }
        }
    }

    private void processRequest(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/plain");

        for (Class<?> c : controllers) {
            resp.getWriter().println(c.getName());
        }

        String url = req.getRequestURI()
                .substring(req.getContextPath().length());

        String method = req.getMethod();

        HttpKey key = new HttpKey(url, method);

        Mapping mapping = urls.get(key);

        if (mapping != null) {

            resp.getWriter().println(
                    "Classe : "
                    + mapping.getClasse().getName()
            );

            resp.getWriter().println(
                    "Méthode : "
                    + mapping.getMethode().getName()
            );

        } else {

            resp.getWriter().println(
                    "URL introuvable.\n"
            );

            resp.getWriter().println(
                    "Les URL disponibles :\n"
            );

            for (HttpKey cle : urls.keySet()) {

                Mapping m = urls.get(cle);

                resp.getWriter().println(
                        cle.getMethod()
                        + " "
                        + cle.getUrl()
                        + " -> "
                        + m.getClasse().getName()
                        + "."
                        + m.getMethode().getName()
                );
            }
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        processRequest(req, resp);
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        processRequest(req, resp);
    }
}