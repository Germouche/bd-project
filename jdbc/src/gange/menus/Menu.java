package gange.menus;
import java.sql.Connection;
import java.util.Scanner;

public abstract class Menu {
	
	protected final Connection connection;
	protected final Scanner scanner;

	public Menu(Connection connection, Scanner scanner) {
		this.connection = connection;
		this.scanner = scanner;
	}
	
	public abstract boolean parseInput(String input);
	
	/**
	 * Liste des commandes disponibles
	 */
	public abstract String getAvailableCommands();


}
