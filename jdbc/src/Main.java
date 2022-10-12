
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;

import gange.Gange;

public class Main {
    public static void main(String[] args) throws Exception {
        // System.out.println("Hello, World!");
        Locale.setDefault(Locale.US);

        // Driver
        try{
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        } catch (SQLException e){
            e.printStackTrace();
        }

        // Connexion à la BDD
        try{
            String url = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";
            String user = "chouitis";
            String pass = "1234";

            Connection conn = DriverManager.getConnection(url, user, pass);
            // System.out.println("Connexion établie avec la base de données");

            Gange gange = new Gange(conn);
            try {
                gange.run();
            } catch(Exception e) {
            	 e.printStackTrace();
                 conn.close();
                 System.out.println("on close");
            }

            conn.close();

        } catch(SQLException e){
            e.printStackTrace();
        }

    }
}
