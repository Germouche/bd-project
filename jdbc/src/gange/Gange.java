package gange;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.Stack;

import gange.menus.AdminMenu;
import gange.menus.CatalogMenu;
import gange.menus.Menu;
import gange.profile.AdminProfile;
import gange.profile.EmptyProfile;
import gange.profile.Profile;
import gange.profile.UserProfile;

public class Gange {
	
	private Scanner scanner;
	private Stack<Menu> menuStack = new Stack<Menu>();
	private Profile profile = new EmptyProfile();
	private Connection connection;
	
	public Gange(Connection connection) {
		this.scanner = new Scanner(System.in);
		this.connection = connection;
	}
	
	public void run() {
		while(true) {
			System.out.println("----------------------------------");
			System.out.println(this.profile.connectionInfo());
			System.out.println(this.getAvailableCommands());
			if (!this.menuStack.empty()) {
				System.out.println(this.menuStack.peek().getAvailableCommands());
			}
			if (!this.parseInput(this.scanner.nextLine())) {
				break;
			}
		}
	}
	
	private boolean parseInput(String input) {
		// Vérification de la connexion avec la db avant le traitement de la commande
		if (!this.verifProfileConnection()) {
			return true;
		}
		switch(input) {
		case "exit":
			return false;
		case "con":
			this.connect();
			return true;
		case "decon":
			this.disconnect();
			return true;
		case "res":
			this.reset();
			return true;
		case "ret":
			if (this.menuStack.size() > 1) {
				this.menuStack.pop();
			}
			return true;
		default:
			if ((!this.menuStack.empty())&this.profile.connectedLocally()) {
				return this.menuStack.peek().parseInput(input);
			} else {
				System.out.println("Cette commande n'est pas disponible, veuillez vous connecter d'abord.");
				return true;
			}
		}
	}
	
	private String getAvailableCommands() {
		return "Commandes disponibles : exit | con | decon | ret";
	}

	private void connect() {
		System.out.print("mail : ");
		String mail = this.scanner.nextLine();
		
		System.out.print("mdp : ");
		String mdp = this.scanner.nextLine();
		
		Profile newProfile = this.verifUser(mail, mdp);
		
		if (newProfile.connectedLocally()) {
			if (this.profile.connectedLocally()) {
				this.disconnect();
			}
			this.profile = newProfile;
			
			if (this.profile.admin) {
				this.menuStack.push(new AdminMenu(this.connection,this.scanner));
			} else {
				this.menuStack.push(new CatalogMenu(connection,scanner, (UserProfile) profile, this));
			}
			
			System.out.println("Connexion effectuée avec succès");
		} else {
			System.out.println("Mail ou mdp incorrect");
		}
	}
	
	private void disconnect() {
		if (this.profile.connectedLocally()) {
			this.menuStack.clear();
			this.profile = new EmptyProfile();
			System.out.println("Déconnexion effectuée avec succès");
		}
		else {
			System.out.println("Déconnexion impossible");
		}
	}
	
	private Profile verifUser(String mail, String mdp) {
		if ((mail.equals("admin"))&(mdp.equals("admin"))) {
			return new AdminProfile();
		}
        try{
            Statement carStatement = this.connection.createStatement();
            ResultSet caracSet = carStatement.executeQuery(String.format("SELECT * FROM INFOS_COMPTES WHERE Mail = '%s' AND MdP = '%s\'", mail, mdp));
            
            if (caracSet.next()) {
            	return new UserProfile(caracSet.getInt("IdInfosCompte"),caracSet.getString("Mail"),
            			caracSet.getString("Nom"),caracSet.getString("Prenom"),caracSet.getString("Adresse"));
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
		return new EmptyProfile();
	}
	
	private void reset() { // Avoir la connexion en exclusif ?
		if (!this.profile.admin) {
			System.out.println("Veuillez vous connecter en temps qu'admin.");
		} else {
			Path commandPath = Path.of("gange.sql");
			try {
				String[] commands = Files.readString(commandPath).split(";[\\r\\n]");
	            for (String command : commands) {
			        try{
						System.out.println(command);
			        	Statement carStatement = this.connection.createStatement();
			            carStatement.executeQuery(command);
			            	
			        }catch(SQLException e){
			            e.printStackTrace();
			        }
	            }
				System.out.println("La base de données a bien été réinitialisée.");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	private boolean verifProfileConnection() {
		if (this.profile.connectedLocally & !this.profile.connected(this.connection)) {
			System.out.println("Votre compte n'existe plus, déconnexion effectuée.");
			this.disconnect();
			return false;
		}
		return true;
	}
	
	public void pushMenu(Menu m) {
		this.menuStack.push(m);
	}
	
	void popMenu() {
		this.menuStack.pop();
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}
	

}
