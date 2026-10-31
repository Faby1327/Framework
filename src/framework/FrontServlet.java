
package framework;

import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Parameter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.HashMap;

import com.google.gson.Gson;

@MultipartConfig
public class FrontServlet extends HttpServlet {

    private void envoyerJson(
            Object resultat,
            HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

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

        resp.setContentType("text/plain;charset=UTF-8");

        ServletContext ctx = getServletContext();

        List<Class<?>> controllers =
                (List<Class<?>>) ctx.getAttribute("controllers");

        HashMap<HttpKey, Mapping> urls =
                (HashMap<HttpKey, Mapping>) ctx.getAttribute("urls");

        String url = req.getRequestURI()
                .substring(req.getContextPath().length());

        String method = req.getMethod();

        HttpKey key = new HttpKey(url, method);

        Mapping mapping = urls.get(key);

        if (mapping != null) {

            try {

                // 1. Instancier le controller
                Object controller =
                        mapping.getClasse()
                                .getDeclaredConstructor()
                                .newInstance();

                // 2. Récupérer la méthode du controller
                java.lang.reflect.Method methodController =
                        mapping.getMethode();

                // 3. Récupérer les paramètres de la méthode
                Parameter[] parameters =
                        methodController.getParameters();

                // 4. Tableau qui contiendra les arguments
                Object[] arguments =
                        new Object[parameters.length];

                // 5. Construire les arguments
                for (int i = 0; i < parameters.length; i++) {

                    Parameter parameter = parameters[i];

                    String nomParametre = parameter.getName();

                    Class<?> typeParametre = parameter.getType();

                    // ------------------------------------
                    // PARAMETRE SESSION
                    // ------------------------------------
                    if (typeParametre == Session.class) {

                        // Récupérer la véritable session HTTP
                        HttpSession httpSession = req.getSession();

                        // Chercher notre objet Session
                        Session session =
                                (Session) httpSession.getAttribute("maSession");

                        // Si aucun objet Session n'existe
                        if (session == null) {

                            // Créer notre objet personnalisé
                            session = new Session();

                            // Le conserver dans la session HTTP
                            httpSession.setAttribute("maSession", session);
                        }

                        // Donner l'objet Session au controller
                        arguments[i] = session;
                    }

                    // ------------------------------------
                    // PARAMETRE MAPFILE : FICHIER
                    // ------------------------------------
                    else if (typeParametre == MapFile.class) {

                        Part part = req.getPart(nomParametre);

                        if (part == null || part.getSize() == 0) {

                            arguments[i] = null;

                        } else {

                            String nomFichier =
                                    part.getSubmittedFileName();

                            if (nomFichier != null) {

                                nomFichier =
                                        nomFichier.replace('\\', '/');

                                nomFichier =
                                        nomFichier.substring(
                                                nomFichier.lastIndexOf('/') + 1
                                        );

                            } else {

                                nomFichier = "fichier";
                            }

                            int positionPoint =
                                    nomFichier.lastIndexOf('.');

                            String nom;
                            String extension;

                            if (positionPoint > 0) {

                                nom = nomFichier.substring(
                                        0, positionPoint
                                );

                                extension = nomFichier.substring(
                                        positionPoint
                                );

                            } else {

                                nom = nomFichier;
                                extension = "";
                            }

                            byte[] contenu;

                            try (InputStream input =
                                         part.getInputStream()) {

                                contenu = input.readAllBytes();
                            }

                            MapFile fichier = new MapFile(
                                    nom,
                                    extension,
                                    contenu
                            );

                            arguments[i] = fichier;
                        }
                    }

                    // ------------------------------------
                    // PARAMETRE STRING
                    // ------------------------------------
                    else if (typeParametre == String.class) {

                        arguments[i] =
                                req.getParameter(nomParametre);
                    }

                    // ------------------------------------
                    // PARAMETRE INT
                    // ------------------------------------
                    else if (typeParametre == int.class) {

                        String valeur =
                                req.getParameter(nomParametre);

                        if (valeur != null && !valeur.isEmpty()) {

                            arguments[i] =
                                    Integer.parseInt(valeur);

                        } else {

                            arguments[i] = 0;
                        }
                    }

                    // ------------------------------------
                    // PARAMETRE OBJET
                    // ------------------------------------
                    else {

                        Object objet =
                                typeParametre
                                        .getDeclaredConstructor()
                                        .newInstance();

                        Field[] champs =
                                typeParametre.getDeclaredFields();

                        for (Field champ : champs) {

                            String nomChamp = champ.getName();

                            Class<?> typeChamp = champ.getType();

                            String valeur =
                                    req.getParameter(nomChamp);

                            if (valeur == null || valeur.isEmpty()) {
                                continue;
                            }

                            Object valeurConvertie;

                            if (typeChamp == String.class) {

                                valeurConvertie = valeur;

                            } else if (typeChamp == int.class) {

                                valeurConvertie =
                                        Integer.parseInt(valeur);

                            } else {

                                continue;
                            }

                            String nomSetter =
                                    "set"
                                    + Character.toUpperCase(
                                            nomChamp.charAt(0))
                                    + nomChamp.substring(1);

                            java.lang.reflect.Method setter =
                                    typeParametre.getMethod(
                                            nomSetter,
                                            typeChamp
                                    );

                            setter.invoke(objet, valeurConvertie);
                        }

                        arguments[i] = objet;
                    }
                }

                // 6. Appeler la méthode du controller
                Object resultat =
                        methodController.invoke(
                                controller,
                                arguments
                        );

                // 7. Traiter le résultat
                if (methodController.isAnnotationPresent(Json.class)) {

                    envoyerJson(resultat, resp);

                } else if (resultat instanceof ModelAndView) {

                    ModelAndView mv =
                            (ModelAndView) resultat;

                    for (String key1 : mv.getData().keySet()) {

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
                            prefix + mv.getView() + suffix;

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

            resp.getWriter().println("URL introuvable.\n");

            resp.getWriter().println("Les URL disponibles :\n");

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
