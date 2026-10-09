package framework;

import jakarta.servlet.http.*;
import jakarta.servlet.*;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.HashMap;
import com.google.gson.Gson;

public class FrontServlet extends HttpServlet {

    private void envoyerJson(
            Object resultat,
            HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");

        if (resultat instanceof String) {

            resp.getWriter().println(resultat);

        } else {
            Gson gson = new Gson();

            String json = gson.toJson(resultat);

            resp.getWriter().println(json);
        }
    }

    private void processRequest(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/plain");

        ServletContext ctx = getServletContext();

        List<Class<?>> controllers =
                (List<Class<?>>) ctx.getAttribute("controllers");

        HashMap<HttpKey, Mapping> urls =
                (HashMap<HttpKey, Mapping>) ctx.getAttribute("urls");

        for (Class<?> c : controllers) {
            resp.getWriter().println(c.getName());
        }

        String url = req.getRequestURI()
                .substring(req.getContextPath().length());

        String method = req.getMethod();

        HttpKey key = new HttpKey(url, method);

        Mapping mapping = urls.get(key);

        if (mapping != null) {

            try {

                Object controller =
                        mapping.getClasse()
                        .getDeclaredConstructor()
                        .newInstance();

                // Récupérer la méthode du controller
                java.lang.reflect.Method methodController =
                        mapping.getMethode();

                // Récupérer les paramètres de la méthode
                Parameter[] parameters =
                        methodController.getParameters();

                // Tableau qui contiendra les arguments
                Object[] arguments =
                        new Object[parameters.length];

                // Construire les arguments
                for (int i = 0; i < parameters.length; i++) {

                    Parameter parameter =
                            parameters[i];

                    String nomParametre =
                            parameter.getName();

                    String valeur =
                            req.getParameter(nomParametre);

                    if (parameter.getType() == String.class) {

                        arguments[i] = valeur;

                        } else if (parameter.getType() == int.class) {

                        if (valeur != null) {
                                arguments[i] = Integer.parseInt(valeur);
                        } else {
                                arguments[i] = 0;
                        }
                        }
                }

                // Appeler la méthode avec les arguments
                Object resultat =
                        methodController.invoke(
                                controller,
                                arguments
                        );

                if (methodController.isAnnotationPresent(Json.class)) {

                    envoyerJson(resultat, resp);

                } else if (resultat instanceof ModelAndView) {

                    ModelAndView mv =
                            (ModelAndView) resultat;

                    for (String key1 :
                            mv.getData().keySet()) {

                        req.setAttribute(
                                key1,
                                mv.getData().get(key1)
                        );
                    }

                    String prefix =
                            getInitParameter("view-prefix");

                    String suffix =
                            getInitParameter("view-suffix");

                    String chemin =
                            prefix
                            + mv.getView()
                            + suffix;

                    RequestDispatcher dispatcher =
                            req.getRequestDispatcher(chemin);

                    dispatcher.forward(req, resp);

                } else {

                    resp.getWriter().println(resultat);
                }

            } catch (Exception e) {

                throw new ServletException(e);
            }

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