package gange.menus;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

import gange.Category;
import gange.Gange;
import gange.profile.UserProfile;

public class CatalogMenu extends Menu {

	private final UserProfile profile;
	private final Gange gange;
	private final Stack<Category> categories = new Stack<Category>();

	public CatalogMenu(Connection connection,Scanner scanner, UserProfile profile, Gange gange) {
		super(connection,scanner);
		this.profile = profile;
		this.gange = gange;
		this.categories.add(new Category("", null, true));
	}

    /**
     * Supprime les données personnelles d'un idAccount
     * @param conn Connection SQL à la BDD ouverte au préalable
     * @param idAccount identifiant du compte (idAccount dans la relation COMPTES)
     * @return Booléen confirmant la suppression des infos personnelles
     */
    public boolean deleteAccount(Connection conn, int idAccount){
        boolean status = false;
        try{
            Statement deleteStatement = conn.createStatement();
            deleteStatement.executeQuery(String.format("DELETE FROM \"INFOS_COMPTES\" WHERE \"IDINFOSCOMPTE\"='%d'", idAccount));

            // Comme on a pas de retour, on vérifie que la moditification a bien été opérée
            Statement deleteCheckStatement = conn.createStatement();
            ResultSet deleteCheckSet = deleteCheckStatement.executeQuery("SELECT COUNT(*) FROM INFOS_COMPTES WHERE IDINFOSCOMPTE=" + String.valueOf(idAccount));

            if(deleteCheckSet.next() && deleteCheckSet.getInt(1) == 0){
                status = true;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }

        return status;
    }



    /**
     * Récupère les caractéristiques d'un produit et en retourne une chaine formatée
     * @param conn Connection SQL ouverte précédement
     * @param idProduit id du produit
     * @return Chaine formatée des caractéristiques du produit et leur valeur
     */
    public Map<String, String> getCaracteristics(Connection conn, int idProduit){
    	Map<String, String> caracteristics = new HashMap<String, String>();
        try{
            Statement carStatement = conn.createStatement();
            ResultSet caracSet = carStatement.executeQuery("SELECT CAR_PRODUITS.NomCar, CAR_PRODUITS.ValCar, PRODUITS.Intitule FROM CAR_PRODUITS JOIN PRODUITS ON CAR_PRODUITS.IdProduit = PRODUITS.IdProduit WHERE CAR_PRODUITS.IdProduit = " + String.valueOf(idProduit));

            while(caracSet.next()){
                caracteristics.put(caracSet.getString(1), caracSet.getString(2));
            }

        }catch(SQLException e){
            e.printStackTrace();
        }

        return caracteristics;
    }



    /**
     * Renvoie la liste des catégorie recommandées
     * Les MAX_PERSONNAL_CAT_RECOMMANDATIONS premières étant celles des achats ratés et les suivantes des catégories
     * les plus populaires. Au maximum, on affichera MAX_OVERALL_CAT_RECOMMANDATIONS catégories en tout.
     * @param conn Connection SQL à la BDD ouverte au préalable
     * @param idAccount identifiant du compte pour lequel faire des recommandations (idAccount dans la relation COMPTES)
     * @return String avec retours à la ligne pour chaque catégories ordonnées
     */
    public String getRecommendations(Connection conn, int idAccount){
        ArrayList<String> catList = new ArrayList<String>();
        int MAX_PERSONNAL_CAT_RECOMMANDATIONS = 3;
        int MAX_OVERALL_CAT_RECOMMANDATIONS = 7;

        try{
            Statement failedOfferStatement = conn.createStatement();
            ResultSet failedOfferSet = failedOfferStatement.executeQuery(String.format("SELECT PRODUITS.Categorie FROM OFFRES JOIN PRODUITS ON OFFRES.IdProduit = PRODUITS.IdProduit WHERE IdCompte=%d AND NOT EXISTS (SELECT * FROM ACHATS WHERE ACHATS.IdCompte=PRODUITS.IdProduit AND ACHATS.IdProduit=OFFRES.IdCompte) GROUP BY PRODUITS.Categorie ORDER BY COUNT(PRODUITS.IdProduit) DESC OFFSET 0 ROWS FETCH NEXT %d ROWS ONLY", idAccount, MAX_PERSONNAL_CAT_RECOMMANDATIONS));

            Statement offersStatement = conn.createStatement();
            ResultSet offersSet = offersStatement.executeQuery("SELECT PRODUITS.Categorie FROM OFFRES FULL JOIN PRODUITS ON OFFRES.IdProduit = PRODUITS.IdProduit GROUP BY PRODUITS.Categorie ORDER BY COUNT(PRODUITS.IdProduit) DESC");

            // Récupération des catégories par offres ratées
            int recomCpt = 0;
            while(failedOfferSet.next()){
                catList.add(failedOfferSet.getString(1));
                recomCpt += 1;
            }

            // Catégories les plus populaires
            while(offersSet.next() && recomCpt < MAX_OVERALL_CAT_RECOMMANDATIONS){
                String currCat = offersSet.getString(1);
                if(!catList.contains(currCat)){
                    catList.add(currCat);
                    recomCpt += 1;
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }

        return String.join("  ", catList);
    }

    private ArrayList<String> getRecursiveProd(Connection conn, String headCatName){
        ArrayList<String> prodList = new ArrayList<String>();

        try{
            Statement childCatStatement = conn.createStatement();
            ResultSet childCatSet = childCatStatement.executeQuery("SELECT NomCatFille FROM CAT_HERIT WHERE NomCatMere = '" + headCatName + "'");

            while(childCatSet.next()){
                prodList.addAll(getProductByCat(conn, childCatSet.getString(1)));
            }
        }catch(SQLException e){
            e.printStackTrace();
        }

        return prodList;
    }

    /**
     * Vérifie qu'une catégorie existe dans la base de données
     * @param conn Connection SQL ouverte précédement
     * @param idProduit id de la catégorie
     * @return Booléen d'existance de la catégorie
     */
    public boolean checkCatExist(Connection conn, String catName){
        boolean exists = false;
        try{
            Statement catCheckStatement = conn.createStatement();
            ResultSet catCheckSet = catCheckStatement.executeQuery(String.format("SELECT * FROM CATEGORIES WHERE NomCat = '%s'",catName));

            exists = catCheckSet.next();
        }catch(SQLException e){
            e.printStackTrace();
        }

        return exists;
    }

	@Override
	public boolean parseInput(String input) {
		if(input.equals("del")) {
			this.deleteAccount(this.connection,this.profile.id);
			System.out.println("Supression effectuée");
			return true;
		}
		String delims = "[ ]+";
		String[] tokens = input.split(delims, 2);
		switch(tokens[0]) {
		case "show":
			System.out.println(this.showProduct(tokens[1]));
			return true;
		case "cc": // Change category
			this.changeCategory(tokens[1]);
			return true;
		case "cp": // Choose product
			try {
				int id = Integer.parseInt(tokens[1]);
				this.chooseProduct(id);
			} catch (Exception e) {
				System.out.println(e);
			}
			return true;
		case "parent": // -> parent category
			this.parentCategory();
			return true;
		case "achats": // Affiche les achats remportés
			System.out.println(this.achats());
			return true;
		default:
			System.out.println("Cette commande n'est pas disponible");
			return true;
		}
	}

	private String showProduct(String productName) {
        String caracString = "";
        try{
            Statement carStatement = this.connection.createStatement();
            ResultSet caracSet = carStatement.executeQuery(String.format("SELECT *\n" +
            		"FROM PRODUITS\n" +
            		"WHERE Intitule='%s'",productName));

            while(caracSet.next()){
                caracString += String.format("%s  %s  %s  %s  %s\n", caracSet.getString(1), caracSet.getString(2),
                		caracSet.getString(3), caracSet.getString(4), caracSet.getString(5));
            }

        }catch(SQLException e){
            e.printStackTrace();
        }

        return caracString;
	}

	private void parentCategory() {
		if (!this.categories.peek().isFirst()) {
			this.categories.pop();
		}
	}

	private String achats() {
        String caracString = "Liste des achats : ";
        try{
            Statement carStatement = this.connection.createStatement();
            ResultSet caracSet = carStatement.executeQuery(String.format("SELECT Intitule\n" +
            		"FROM ACHATS NATURAL JOIN PRODUITS\n" +
            		"WHERE IdCompte = '%s'",this.profile.id));

            while(caracSet.next()){
                caracString += String.format("%s\n", caracSet.getString(1));
            }

        }catch(SQLException e){
            e.printStackTrace();
        }

        return caracString;
	}

	private void chooseProduct(int idProduct) {
        try{
            Statement carStatement = this.connection.createStatement();
            ResultSet caracSet = carStatement.executeQuery("SELECT IDPRODUIT,INTITULE,DESCRIPTION,CATEGORIE\n" +
            		"FROM PRODUITS\n" +
            		"WHERE IDPRODUIT="+String.valueOf(idProduct));

            if(caracSet.next()){
            	this.gange.pushMenu(new ProductMenu(this.connection,this.scanner,this.profile,this.gange,
            			caracSet.getInt(1),this.getCaracteristics(this.connection, idProduct),
            			caracSet.getString(2),caracSet.getString(3)));
            } else {
            	System.out.println("Cet id ne correspond pas à un produit.");
            }

        }catch(SQLException e){
            e.printStackTrace();
        }
	}

	private void changeCategory(String catName) {
		Category catActuelle = this.categories.peek();
		if (catActuelle.containsChild(catName)) {
			this.categories.add(catActuelle.getChild(catName));
		} else {

	        if (this.checkCatExist(this.connection, catName)) {
	            	this.rebuildStack(catName);
            } else {
            	System.out.println("Cette catégorie n'existe pas.");
            	}
	        }
		}

	private void rebuildStack(String catName) {

        this.categories.clear();
        Deque<String> catNames = new LinkedList<String>();
        String currentCatName = getParentCategory(catName);
        while (!currentCatName.equals("")) {
        	catNames.addFirst(currentCatName);
        	currentCatName = getParentCategory(currentCatName);
        }
		this.categories.add(new Category("", null, true));
		Category parent;
        for (String cat : catNames){
        	parent = this.categories.peek();
            this.categories.add(new Category(cat, parent, false));
        }
    	parent = this.categories.peek();
    	this.categories.add(new Category(catName, parent, false));

	}

	private String getParentCategory(String childCatName) {
		String parentCatName = "";
		try{
            Statement carStatement = this.connection.createStatement();
            ResultSet caracSet = carStatement.executeQuery(String.format("SELECT UNIQUE NomCatMere\n" +
            		"  FROM CAT_HERIT\n" +
            		"  WHERE NomCatFille = '%s'\n" +
            		"", childCatName));
            if(caracSet.next()){
                parentCatName += caracSet.getString(1);
            }

        }catch(SQLException e){
            e.printStackTrace();
        }
		return parentCatName;
	}


	private String infos() {
		Category cat = this.categories.peek();
		if (!cat.hasInfos()) {
			if (cat.isFirst()) {
				this.fillFirstCategory(cat);
			} else {
				this.fillCategory(cat);
			}
		}
		return cat.categoryInfos()+ "Recommandations : " + this.getRecommendations(this.connection, this.profile.id)
		+ "\n" + cat.ProductInfos();
	}

	private void fillFirstCategory(Category cat) {
        try{
            Statement carStatement = this.connection.createStatement();
            ResultSet caracSet = carStatement.executeQuery("SELECT DISTINCT NomCat\n" +
            		"FROM CATEGORIES\n" +
            		"MINUS\n" +
            		"SELECT DISTINCT NomCatFille\n" +
            		"FROM CAT_HERIT");
            Set<Category> childen = new HashSet<Category>();
            while(caracSet.next()){
                childen.add(new Category(caracSet.getString(1), cat, false));
            }
            cat.fillCategory(childen.iterator());

        }catch(SQLException e){
            e.printStackTrace();
        }
	}

	private void fillCategory(Category cat) {
        try{
            Statement carStatement = this.connection.createStatement();
            ResultSet caracSet = carStatement.executeQuery(String.format("SELECT Distinct NomCatFille\n" +
            		"  FROM CAT_HERIT\n" +
            		"    where NomCatMere = '%s'", cat.getName()));
            Set<Category> childen = new HashSet<Category>();
            while(caracSet.next()){
                childen.add(new Category(caracSet.getString(1), cat, false));
            }
            cat.fillCategory(childen.iterator());

        }catch(SQLException e){
            e.printStackTrace();
        }

        cat.fillProducts(this.getRecursiveProd(this.connection, cat.getName()).iterator());

	}


    /**
     * Récupère tout les produits d'une catégorie
     * @param conn Connection SQL à la BDD ouverte au préalable
     * @param catString Nom de la catégorie (constituant son identifiant dans la relation PRODUITS)
     * @return ArrayList<String> sous la forme "id. intitule"
     */
    public ArrayList<String> getProductByCat(Connection conn, String catString){
    	ArrayList<String> prodString = new ArrayList<String>();
        try{
            Statement prodByCatStatement = conn.createStatement();
            ResultSet prodByCatSet = prodByCatStatement.executeQuery(String.format("select PRODUITS.IdProduit, PRODUITS.Intitule\n" +
            		"from PRODUITS JOIN OFFRES on PRODUITS.IdProduit = OFFRES.IdProduit\n" +
            		"WHERE PRODUITS.categorie = '%s'\n" +
                    "AND NOT EXISTS (SELECT * FROM ACHATS WHERE ACHATS.IdProduit = PRODUITS.IdProduit)\n" +
            		"GROUP BY PRODUITS.IdProduit,PRODUITS.Intitule\n" +
            		"ORDER BY COUNT(*) DESC,PRODUITS.Intitule", catString));

            while(prodByCatSet.next()){
                prodString.add(String.format("%d. %s", prodByCatSet.getInt(1), prodByCatSet.getString(2)));
            }

            prodByCatSet = prodByCatStatement.executeQuery(String.format("SELECT IdProduit, Intitule\n" +
            "FROM PRODUITS WHERE categorie = '%s'\n" +
            "AND NOT EXISTS (SELECT * FROM OFFRES WHERE OFFRES.IdProduit=PRODUITS.IdProduit)\n" +
            "AND NOT EXISTS (SELECT * FROM ACHATS WHERE ACHATS.IdProduit=PRODUITS.IdProduit)\n" +
            "ORDER BY intitule", catString));

            while(prodByCatSet.next()){
                prodString.add(String.format("%d. %s", prodByCatSet.getInt(1), prodByCatSet.getString(2)));
            }

        }catch(SQLException e){
            e.printStackTrace();
        }

        return prodString;
    }

	@Override
	public String getAvailableCommands() {
		return "                        del | show <nameProduct> | cc <Category> | cp <idProduct> | parent | achats"
	+ "\n" + this.infos();
	}

}
