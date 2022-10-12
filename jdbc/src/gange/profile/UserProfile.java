package gange.profile;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserProfile extends Profile {
	public final int id;
	final String mail;
	final String lastName;
	final String firstName;
	final String adress;

	public UserProfile(int id, String mail, String lastName, String firstName, String adress) {
		super(false, true);
		this.id = id;
		this.mail = mail;
		this.lastName = lastName;
		this.firstName = firstName;
		this.adress = adress;
	}
	

	@Override
	public boolean connected(Connection connection) {
		try{
	        Statement carStatement = connection.createStatement();
	        ResultSet caracSet = carStatement.executeQuery(String.format("SELECT * FROM INFOS_COMPTES WHERE IdInfosCompte = '%s\'", this.id));

	        if (!caracSet.next()) {
	        	return false;
	        }
	        /*String caracString = "";

            while(caracSet.next()){
            	for (int i = 1 ; i <= caracSet.getMetaData().getColumnCount(); i++) {
                    caracString += String.format("%-15s", caracSet.getString(i));;
            	}
            	caracString += "\n";
            }
            
            System.out.println(caracString);*/
	        
	    }catch(SQLException e){
	        e.printStackTrace();
	    }
		return true;
	}

	@Override
	public String connectionInfo() {
		return "Connected as "+this.firstName+" "+this.lastName;
	}
	
	@Override
	public String toString() {
		return "Profile [mail=" + mail + ", lastName=" + lastName + ", firstName=" + firstName + ", adress=" + adress
				+ ", admin=" + admin + "]";
	}

}
