package gange.profile;

import java.sql.Connection;

public abstract class Profile {

	public final boolean admin;
	public final boolean connectedLocally;


	public Profile(boolean admin, boolean connectedLocally) {
		this.admin = admin;
		this.connectedLocally = connectedLocally;
	}

	public abstract String connectionInfo();

	public boolean connectedLocally() {
		return this.connectedLocally;
	}
	
	
	/**
	 * La différence avec connected Locally est qu'il est possible d'être connecté
	 * dans l'application, mais que le compte soit supprimé dans une autre fenêtre,
	 * alors connected Locally doit se mettre à jour sur 
	 */
	public abstract boolean connected(Connection connection);
}
