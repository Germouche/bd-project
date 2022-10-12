package gange.menus;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class AdminMenu extends Menu {

	public AdminMenu(Connection connection, Scanner scanner) {
		super(connection,scanner);
	}

	@Override
	public boolean parseInput(String input) {
		switch(input) {
		case "users":
			this.show("INFOS_COMPTES");
			return true;
		case "products":
			this.show("PRODUITS");
			return true;
		case "offers":
			this.show("OFFRES");
			return true;
		default:
			System.out.println("Cette commande n'est pas disponible");
			return true;
		}
	}
	
	private void show(String tableName) {
        String caracString = "";
        try{
            Statement carStatement = this.connection.createStatement();
            ResultSet caracSet = carStatement.executeQuery(String.format("SELECT * FROM %s", tableName));
            
            while(caracSet.next()){
            	for (int i = 1 ; i <= caracSet.getMetaData().getColumnCount(); i++) {
                    caracString += String.format("%-15s", caracSet.getString(i));;
            	}
            	caracString += "\n";
            }
            
            System.out.println(caracString);

        }catch(SQLException e){
            e.printStackTrace();
        }
	}

	@Override
	public String getAvailableCommands() {
		return "users | products | offers";
	}

}
