package gange.bid;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Offre {
    private final int idProduit;
    private final Timestamp dateOffre;
    private final double montant;
    private final int idCompte;

    public Offre(ResultSet res) throws SQLException {
        this(   
                res.getInt("IDPRODUIT"),
                res.getTimestamp("DATEHEURE"),
                res.getDouble("MONTANT"),
                res.getInt("IDCOMPTE")
        );
    }

    public Offre(int idProduit, Timestamp dateOffre, double montant, int idCompte) {
        this.dateOffre = dateOffre;
        this.montant = montant;
        this.idProduit = idProduit;
        this.idCompte = idCompte;
    }

    public long getIdProduit() {
        return idProduit;
    }

    public Timestamp getDateOffre() {
        return dateOffre;
    }

    public long getIdClient() {
        return idCompte;
    }

    public double getPrix() {
        return montant;
    }


    @Override
    public String toString() {
        return "Offre{" +
                ", dateOffre=" + dateOffre +
                ", montant=" + montant +
                "idProduit=" + idProduit +
                ", idCompte=" + idCompte +
                '}';
    }
}
