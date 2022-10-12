package gange.menus;

import java.awt.List;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;

import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import gange.Gange;
import gange.bid.Achat;
import gange.bid.Offre;
import gange.bid.Produit;
import gange.profile.UserProfile;



public class ProductMenu extends Menu {

	private final UserProfile profile;
	private final Gange gange;
	private final int id;
	private final Map<String, String> caracteristics;
	private final String intitule;
	private final String description;

	public ProductMenu(Connection connection,Scanner scanner, UserProfile profile, Gange gange, int id,
			Map<String, String> caracteristics, String intitule, String description) {
		super(connection,scanner);
		this.profile = profile;
		this.gange = gange;
		this.id = id;
		this.caracteristics = caracteristics;
		this.intitule = intitule;
		this.description = description;
	}

	@Override
	public boolean parseInput(String input) {
		String delims = "[ ]+";
		String[] tokens = input.split(delims);
		switch(tokens[0]) {
		case "bid":
			this.bid();
			return true;
		default:
			System.out.println("Cette commande n'est pas disponible");
			return true;
		}
	}

	private void bid() {
		try{
			if (this.checkProductExist(this.connection, this.id)) {
				// Start transaction
				this.connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
	        	this.connection.setAutoCommit(false);

	            Statement carStatement = this.connection.createStatement();
	            ResultSet countSet = carStatement.executeQuery(String.format("select COUNT(*)\n" +
	            		"from PRODUITS NATURAL JOIN OFFRES\n" +
	            		"WHERE IdProduit=%s\n" +
	            		"GROUP BY IdProduit\n",String.valueOf(this.id)));
	            boolean disponible = true;
	            if(countSet.next()){
	            	int count = countSet.getInt(1);
	            	disponible = count < 5;
	            }

	            ResultSet prixSet = carStatement.executeQuery("SELECT DISTINCT PrixCourant\n" +
	            		"FROM PRODUITS\n" +
	            		"WHERE IDPRODUIT = "+String.valueOf(this.id));

	            if(prixSet.next()){
	            	float current_price = prixSet.getFloat(1);

	        		// Proposition d'une offre de la part du client en sachant qu'il puisse le faire
	                System.out.println(String.format("Prix Courant : %s \nVeuillez entrer un prix d'enchère supérieur",String.valueOf(current_price)));
	                boolean validInput = false;

	                // valide son prix que s'il est supérieur à current_price
	                float bid = 0;
	                while(!validInput) {
	                    try {
	                        bid = Float.valueOf(this.scanner.nextLine());
	                        validInput = bid > current_price;
	                    } catch(Exception e){
	                        System.out.println("Merci de bien vouloir proposer un prix d'offre supérieur au prix d'enchère actuel.");
	                    }
	                }
	                try {
	                	createOffreBetween(new Produit(this.id, current_price, this.intitule, this.description, disponible), bid);
	                } catch (Exception e) {
	                	System.out.println(e);
	                }
	                this.connection.setAutoCommit(true);
					this.connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
	            }
			} else {
				System.out.println("Ce produit n'existe pas.");
			}
        }catch(SQLException e){
            e.printStackTrace();
        }

	}

	private String infos() {
		String str = this.intitule + "\n" + this.description + "\n";
		for (Entry<String,String> carac2 : this.caracteristics.entrySet()) {
			str += carac2.getKey() + ": " + carac2.getValue() + "\n";
		}
		return str;
	}

	@Override
	public String getAvailableCommands() {
		return "                        bid"
	+ "\n" + this.infos();
	}


	// Acces du client vers la page d'information sur le produit
    public String getInfoProduit(Connection conn, int idProduit){
    String caracString = "";
    try{
        Statement carStatement = conn.createStatement();
        ResultSet caracSet = carStatement.executeQuery("SELECT * FROM PRODUITS WHERE IdProduit = " + String.valueOf(idProduit));
        while(caracSet.next()){
            caracString += String.format("%s : %s\n", caracSet.getString(1), caracSet.getString(2));
        }
    }catch(SQLException e){
        e.printStackTrace();
    }
    return caracString;
}

    public String AjoutAchat(Connection conn, int idProduit){
        String caracString = "";
        try{
            Statement carStatement = conn.createStatement();
            ResultSet caracSet = carStatement.executeQuery(String.format("INSERT INTO ACHATS (IdProduit, IdCompte) VALUES (%s, %s)", idProduit, this.profile.id));
            while(caracSet.next()){
                caracString += String.format("%s : %s\n", caracSet.getString(1), caracSet.getString(2));
            }
        }catch(SQLException e){
             e.printStackTrace();
            }
        return caracString;
    }

    // Méthode qui renvoie les offres d'un certain produit enchéri
    public ArrayList<Offre> getOffreForProduit(Produit produit) throws SQLException {
        try (var stmt = this.connection.prepareStatement("SELECT * FROM OFFRES WHERE IdProduit = ?")) {
            stmt.setLong(1, produit.getId());
            var res = stmt.executeQuery();

            var offres = new ArrayList<Offre>();
            while (res.next()) {
                offres.add(new Offre(res));
            }

            return offres;
        }
    }


    public Achat findAchatForProduit(long produitId) throws SQLException {
    try (var stmt = this.connection.prepareStatement("SELECT * FROM achat WHERE IdProduit = ?")) {
        stmt.setLong(1, produitId);
        var res = stmt.executeQuery();

        if (res.next()) {
            return new Achat(res);
        } else {
            return null;
        }
    }
}

    private void insertOffre(Offre offre) throws SQLException {
        try (var stmt = this.connection.prepareStatement(String.format("INSERT INTO OFFRES(IdProduit, IdCompte, DateHeure, Montant) VALUES (?, ?, ?, %.2f)", offre.getPrix()))) {
            stmt.setLong(1, offre.getIdProduit());
            stmt.setLong(2, offre.getIdClient());
            stmt.setTimestamp(3, offre.getDateOffre());

            stmt.executeUpdate();
        }
    }

    private void updateCurrentPrice(Offre offre) throws SQLException {
        try (var stmt = this.connection.prepareStatement(String.format("UPDATE \"PRODUITS\" SET\n" +
        		"\"PRIXCOURANT\" = '%s'\n" +
        		"WHERE \"IDPRODUIT\" = '%s'",offre.getPrix(),offre.getIdProduit()))) {

            stmt.executeUpdate();
        }
    }

    private void insertAchat(Achat achat) throws SQLException {
        try (var stmt = this.connection.prepareStatement("INSERT INTO ACHATS(IdProduit, IdCompte) VALUES (?, ?)")) {
            stmt.setLong(1, achat.getIdProduit());
            stmt.setLong(2, achat.getIdCompte());

            stmt.executeUpdate();
        }
    }

    public Offre createOffreBetween(Produit produit, float prix) throws SQLException {
        if (!produit.isAvailable()) {
            throw new IllegalStateException("Ce produit n'est plus disponible !");
        }

        if (prix <=  produit.getPrix()) {
            throw new IllegalStateException("L'offre doit être supérieure à " + produit.getPrix() + " !");
        }

        try {
            var offers = getOffreForProduit(produit);

            if (offers.size() >= 5) {
                this.connection.rollback();
                throw new IllegalStateException("Ce produit n'est plus disponible. Il y a déjà 5 offres pour ce produit !");
            }

            // Check offre est superieur
            var max = offers.stream().max(Comparator.comparingDouble(Offre::getPrix)).map(Offre::getPrix).orElse(0.0);
            if (prix <= max) {
                this.connection.rollback();
                throw new IllegalStateException("La nouvelle offre doit être supérieur a la première !");
            }
            var date_offre = new Timestamp(System.currentTimeMillis());
            // System.out.println(date_offre);

            var offer = new Offre(produit.getId(), date_offre, prix, this.profile.id);
            insertOffre(offer);
            updateCurrentPrice(offer);

            boolean achete = false;
            if (offers.size() == 5 - 1) {
            	// L'achat est conclue sur cette dernière offre et le produit devient indisponible. On l'ajoute dans la table ACHATS
                var achat = new Achat(produit.getId(), this.profile.id);
                insertAchat(achat);
                achete = true;
            }

            // End transaction
            this.connection.commit();
            System.out.println("Offre bien effectuée");
            if (achete) {
                System.out.println("Vous avez remporté l'enchère !");
            }

            return offer;
        } catch (SQLException e) {
            System.out.println("[CreateOffreBetween] Failure, rolling back transaction " + e);
            this.connection.rollback();
            return null;
        } finally {
            this.connection.setAutoCommit(true);
        }
    }


    /**
     * Vérifie qu'un produit existe dans la base de données
     * @param conn Connection SQL ouverte précédement
     * @param idProduit id du produit
     * @return Booléen d'existance du produit
     */
    public boolean checkProductExist(Connection conn, int idProduit){
        boolean exists = false;
        try{
            Statement productCheckStatement = conn.createStatement();
            ResultSet productCheckSet = productCheckStatement.executeQuery(String.format("SELECT * FROM PRODUITS WHERE IdProduit = %d", idProduit));

            exists = productCheckSet.next();
        }catch(SQLException e){
            e.printStackTrace();
        }

        return exists;
    }


}
