package gange.bid;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Produit {
    private final int IdProduit;
    private final String Intitule;
    private final double PrixCourant;
    private final String description;
    private final boolean available;

    public Produit(int IdProduit, double PrixCourant, String Intitule, String description, boolean available) {
        this.IdProduit = IdProduit;
        this.PrixCourant = PrixCourant;
        this.Intitule = Intitule;
        this.description = description;
        this.available = available;
    }

    // Setters & getters 
    public int getId() {
        return IdProduit;
    }

    public double getPrix() {
        return PrixCourant;
    }

    public String getIntitule() {
        return Intitule;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAvailable() {
        return available;
    }


    // affiche les caractérisations du produit
    @Override
    public String toString() {
        return "Produit{" +
                "IdProduit=" + IdProduit +
                ", intitulé='" + Intitule + '\'' +
                ", PrixCourant=" + PrixCourant +
                ", description='" + description + '\'' +
                '}';
    }
}
