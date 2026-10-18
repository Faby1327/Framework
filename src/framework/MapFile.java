
package framework;

public class MapFile {

    private String nom;
    private String extension;
    private byte[] contenu;

    public MapFile() {
    }

    public MapFile(String nom, String extension, byte[] contenu) {
        this.nom = nom;
        this.extension = extension;
        this.contenu = contenu;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public byte[] getContenu() {
        return contenu;
    }

    public void setContenu(byte[] contenu) {
        this.contenu = contenu;
    }
}
