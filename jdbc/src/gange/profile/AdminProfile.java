package gange.profile;

import java.sql.Connection;

public class AdminProfile extends Profile {

	public AdminProfile() {
		super(true,true);
	}

	@Override
	public String connectionInfo() {
		return "Connected as admin";
	}

	@Override
	public boolean connected(Connection connection) {
		return true;
	}
	
	@Override
	public boolean connectedLocally() {
		return true;
	}

}
