package framework;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScannerClasse {

    private String racine;
    private List<Class<?>> listeControllers;

    public ScannerClasse(String racine) {
        this.racine = racine;
        this.listeControllers = new ArrayList<>();
    }

    public List<Class<?>> scan(
        File dossier,
        Class<? extends java.lang.annotation.Annotation> annotation,
        ScanType type) {

        File[] fichiers = dossier.listFiles();

        if (fichiers == null) {
            return listeControllers;
        }

        for (File f : fichiers) {

            if (f.isDirectory()) {

                scan(f, annotation, type);

            } else if (f.getName().endsWith(".class")) {

                try {

                    String cheminComplet = f.getAbsolutePath();

                    String nomClasse =
                            cheminComplet.substring(
                                    racine.length() + 1
                            );

                    nomClasse =
                            nomClasse.replace(File.separator, ".");

                    nomClasse =
                            nomClasse.replace(".class", "");

                    Class<?> c = Class.forName(nomClasse);

                    if (c.isAnnotationPresent(Controller.class)) {

                        listeControllers.add(c);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return listeControllers;
    }
}