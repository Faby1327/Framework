package framework;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

@WebListener
public class ListenerFramework implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        ServletContext context = sce.getServletContext();

        String rootPath =
                context.getRealPath("/WEB-INF/classes");

        String controllerPackage =
                context.getInitParameter("controller-package");

        String scanPath =
                rootPath + File.separator + controllerPackage;

        ScannerClasse scanner =
                new ScannerClasse(rootPath);

        List<Class<?>> controllers =
                scanner.scan(
                        new File(scanPath),
                        Controller.class,
                        ScanType.CLASS
                );

        HashMap<HttpKey, Mapping> urls = new HashMap<>();

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
                        throw new RuntimeException(
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

        context.setAttribute("controllers", controllers);
        context.setAttribute("urls", urls);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

        ServletContext context = sce.getServletContext();

        context.removeAttribute("controllers");
        context.removeAttribute("urls");
    }
}
