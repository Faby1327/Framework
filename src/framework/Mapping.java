package framework;

import java.lang.reflect.Method;

public class Mapping {

    private Class<?> classe;
    private Method methode;

    public Mapping(Class<?> classe, Method methode) {
        this.classe = classe;
        this.methode = methode;
    }

    public Class<?> getClasse() {
        return classe;
    }

    public Method getMethode() {
        return methode;
    }
}