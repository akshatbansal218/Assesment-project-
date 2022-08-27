package assessment;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public final class App {
   
    public static void main(String[] args) { 
        Connection con = connect() ;   // generating connection with database
        tablecheck(con);
        Statement stmt = create(con);  // creating database server and final

        JSONParser jsonParser = new JSONParser();  //JSON parser object to parse read file
         
        try (FileReader log = new FileReader("logfile.txt"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(log);
 
            JSONArray server_log = (JSONArray) obj;
            try{
                PreparedStatement pstmt = con.prepareStatement("INSERT INTO server values (?, ?, ?, ?, ?, ?)");
                stmt = con.createStatement();
                long i = 0;
                for(Object object : server_log) {
                    i = i+1;
                    JSONObject record = (JSONObject)object;

                    String id = (String.valueOf( record.get("id")));
                    pstmt.setString(1, id);                

                    String state = (String.valueOf(record.get("state")));
                    pstmt.setString(2, state);

                    if(record.containsKey("type")){
                        String type = (String.valueOf(record.get("type")));
                        pstmt.setString(3, type);}
                    else{
                        pstmt.setString(3, " ");}
                    
                    if(record.containsKey("host")){
                        String host = (String.valueOf(record.get("host")));
                        pstmt.setString(4, host);}
                    else{pstmt.setString(4, " ");}

                    Long timestamp = Long.parseLong(String.valueOf(record.get("timestamp")));
                    pstmt.setLong(5, timestamp);

                    pstmt.setLong(6,i);
                    pstmt.executeUpdate();

                    if(i>1){  //check if we have atleast 1 table entity
                        ResultSet qresult = null;
                        try {                         
                            PreparedStatement qstmt = con.prepareStatement("SELECT * FROM server WHERE id = ?");
                            qstmt.setString(1, id);
                            qresult = qstmt.executeQuery ( );
                            
                            while(qresult.next()){
                                Integer user1 = qresult.getInt("user");
                                if(user1 != i){                               
                                    Integer time = (int) (timestamp - qresult.getLong("timestamp"));
                                    if(time<0){time = -time; };
                                    // alert
                                    boolean flag = false;
                                    if(time >4){
                                        flag = true;
                                    }

                                    // insert for second table

                                    PreparedStatement wstmt = con.prepareStatement("INSERT INTO final values (?, ?, ?, ?, ?)");

                                    wstmt.setString(1, id);
                                    
                                    Integer duration = time;
                                    wstmt.setInt(2,duration);

                                    if(record.containsKey("type")){
                                        String type = (String.valueOf(record.get("type")));
                                        wstmt.setString(3, type);}
                                    else{wstmt.setString(3, " ");}
                                    
                                    if(record.containsKey("host")){
                                        String host = (String.valueOf(record.get("host")));
                                        wstmt.setString(4, host);}
                                    else{wstmt.setString(4, " ");}

                                    wstmt.setBoolean(5,flag);

                                    wstmt.executeUpdate();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace(System.out);
                        }  
                }  
             }  
            
            System.out.println("record inserted");
            }catch (Exception e) {
                e.printStackTrace();
            }
 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }  
        
        
    }





    private static void tablecheck(Connection con) {
        ResultSet eresult = null;
        try {                         
            PreparedStatement estmt = con.prepareStatement("SELECT * FROM server");
            
            eresult = estmt.executeQuery ( );
            
            if(eresult.next()){
                Statement stmt = null;
                int result = 0;
                
                try {
                    Statement rstmt = null;
                    Class.forName("org.hsqldb.jdbc.JDBCDriver");
                    rstmt = con.createStatement();
                    result = rstmt.executeUpdate("DROP TABLE server");
                }catch (Exception e) {
                    e.printStackTrace(System.out);
                }
                
                System.out.println("server Already present table,.....Table dropped successfully");
             }
        } catch (Exception e) {
            e.printStackTrace(System.out);        
        }

        try {                         
            PreparedStatement estmt = con.prepareStatement("SELECT * FROM final");
            
            eresult = estmt.executeQuery ( );
            
            if(eresult.next()){
                Statement stmt = null;
                int result = 0;
                
                try {
                    Statement rstmt = null;
                    Class.forName("org.hsqldb.jdbc.JDBCDriver");
                    rstmt = con.createStatement();
                    result = rstmt.executeUpdate("DROP TABLE final");
                }catch (Exception e) {
                    e.printStackTrace(System.out);
                }
                
                System.out.println("final Already present table,.....Table dropped successfully");
             }
        } catch (Exception e) {
            e.printStackTrace(System.out);        }
    
    }





    public static Statement create(Connection con) {
        Statement stmt = null;
        int result = 0;
      try {
         stmt = con.createStatement();
         result = stmt.executeUpdate("CREATE TABLE server ("
           +" id VARCHAR(20) NOT NULL," 
           +"state VARCHAR(20) NOT NULL,"
           +" type VARCHAR(20) ,"
           +"host VARCHAR(20), "
           +"timestamp VARCHAR(50),"
           +"user VARCHAR(50),"
           +" PRIMARY KEY (user));");

        result = stmt.executeUpdate("CREATE TABLE final ("
           +" id VARCHAR(20) NOT NULL," 
           +"duration VARCHAR(20),"
           +" type VARCHAR(20) ,"
           +"host VARCHAR(20), "
           +"alert BOOLEAN NOT NULL," 
           +" PRIMARY KEY (id));");
			
      }  catch (Exception e) {
         e.printStackTrace(System.out);
      }
      System.out.println("Table created successfully");
    return stmt;
    }






    private static Connection connect() {
        Connection con = null;
        
        try {
           //Registering the HSQLDB JDBC driver
           Class.forName("org.hsqldb.jdbc.JDBCDriver");
           //Creating the connection with HSQLDB
           con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/testdb", "SA", "");
            if (con!= null){
              System.out.println("Connection created successfully");
              
           }else{
              System.out.println("Problem with creating connection");
           }
        
        }  catch (Exception e) {
           e.printStackTrace(System.out);
        }
     
        
        return con;
        
    }


}
