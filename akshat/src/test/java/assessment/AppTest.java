package assessment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.DriverManager;
    import java.sql.SQLException;

public class AppTest {

    
        @Test
       public void flagshoudbetrue(){
        App alert = new App();
        try {
            assertNotNull(alert.create(DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/testdb", "SA", "")),"unable to create table, connection problem");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
    
    }

    
    
}