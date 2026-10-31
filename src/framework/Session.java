
package framework;

import java.util.HashMap;
import java.util.Map;

public class Session {

    private Map<String, Object> donnees = new HashMap<>();

    // Ajouter ou modifier une donnée
    public void set(String nom, Object valeur) {
        donnees.put(nom, valeur);
    }

    // Récupérer une donnée
    public Object get(String nom) {
        return donnees.get(nom);
    }

    // Vérifier si une donnée existe
    public boolean contient(String nom) {
        return donnees.containsKey(nom);
    }

    // Supprimer une donnée
    public void remove(String nom) {
        donnees.remove(nom);
    }

    // Vider toutes les données
    public void vider() {
        donnees.clear();
    }
}
