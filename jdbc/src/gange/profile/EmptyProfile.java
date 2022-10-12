package gange.profile;

import java.sql.Connection;

public class EmptyProfile extends Profile {

	public EmptyProfile() {
		super(false,false);
	}

	@Override
	public boolean connected(Connection connection) {
		return false;
	}

	@Override
	public String connectionInfo() {
		return "Not connected yet";
	}

}
