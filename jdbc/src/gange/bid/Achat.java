package gange.bid;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Achat {
    private final long idProduit;
    private final long idCompte;

    public Achat(ResultSet res) throws SQLException {
        this(
                res.getLong("IdProduit"),
                res.getLong("IdCompte")
        );
    }

    public Achat(long idProduit, long idCompte) {
        this.idProduit = idProduit;
        this.idCompte = idCompte;
    }

    public long getIdCompte() {
		return idCompte;
	}

	public long getIdProduit() {
        return idProduit;
    }


    @Override
    public String toString() {
        return "Achat{" +
                "idProduit=" + idProduit +
                ", idCompte=" + idCompte +
                '}';
    }
}
