package Controller.Database;

import Model.UserData;
import java.io.File;
import java.sql.*;

public class DBManager {
    private static Connection con;
    private static File dbPath = new File("data/");
    private static File db = new File(dbPath+"db.mv.db");

   private static void startDB() {
        try
        {
            if( !db.isFile() ) {
                setup();
            }

            Class.forName("org.h2.Driver");
            con = DriverManager.getConnection("jdbc:h2:./data/db", "RAMT", "" );
        }
        catch( Exception e )
        {
            System.out.println( e.getMessage() );
        }
    }

    private static void stopDB() throws SQLException {
        con.close();
        con = null;
    }

    public static void setup() throws SQLException {
        con = DriverManager.getConnection("jdbc:h2:./data/db", "RAMT", "" );
        Statement stmt = con.createStatement();
        stmt.executeUpdate( "create table users (id uuid DEFAULT random_uuid() NOT NULL PRIMARY KEY,\n" +
                "                    username varchar(64) NOT NULL,\n" +
                "                    password varchar(512) NOT NULL,\n" +
                "                    user_group varchar(64) DEFAULT 'Default' NOT NULL,\n" +
                "                    UNIQUE (username)" +
                ");" );
        stmt.executeUpdate( "create table groups (user_group varchar(64) NOT NULL primary key,\n" +
                "                    administrator boolean(0) default false NOT NULL,\n" +
                "                    monitoring boolean(0) default false NOT NULL,\n" +
                "                    processes boolean(0) default false NOT NULL,\n" +
                "                    general boolean(0) default false NOT NULL,\n" +
                "                    power boolean(0) default false NOT NULL\n" +
                ");" );
        stmt.executeUpdate( "create table settings (setting varchar(128) NOT NULL primary key,\n" +
                "                     value varchar(512) default false NOT NULL\n" +
                ");" );
        stmt.executeUpdate("ALTER TABLE users ADD FOREIGN KEY (user_group) REFERENCES groups(user_group);");
        stmt.executeUpdate("INSERT INTO groups(user_group) VALUES ('Default');");

        stmt.close();
        con.close();

        //Insert server settings using method
        //Security method,
    }

    /*> Queries <*/

    public static int insertUser(UserData user) throws SQLException {
        startDB();

        Statement stmt = con.createStatement();
        int result = stmt.executeUpdate( "INSERT INTO users(username, password, user_group) VALUES ('" + user.getUsername() +"', '" + user.getPassword() +"', '" + user.getGroup() + "');" );
        stmt.close();

        stopDB();

        return result;
    }

    public static int deleteUser(UserData user) throws SQLException {
        startDB();

        Statement stmt = con.createStatement();
        int result = stmt.executeUpdate( "DELETE FROM users WHERE username='" + user.getUsername() + "';");
        stmt.close();

        stopDB();

        return result;
    }

    public static int updateUsername(UserData user, String newName) throws SQLException {
        startDB();

        Statement stmt = con.createStatement();
        int result = stmt.executeUpdate( "UPDATE users SET username='" + newName + "' WHERE username='" + user.getUsername() + "';");
        stmt.close();

        stopDB();

        return result;
    }

    public static int updatePassword(UserData user, String newPassword) throws SQLException {
        startDB();

        Statement stmt = con.createStatement();
        int result = stmt.executeUpdate( "UPDATE users SET password='" + newPassword + "' WHERE username='" + user.getUsername() + "';");
        stmt.close();

        stopDB();

        return result;
    }

    public static int updateGroup(UserData user, String newGroup) throws SQLException {
        startDB();

        Statement stmt = con.createStatement();
        int result = stmt.executeUpdate( "UPDATE users SET user_group='" + newGroup + "' WHERE username='" + user.getUsername() + "';");
        stmt.close();

        stopDB();

        return result;
    }

    public static int addGroup(String user_group, Boolean administrator, Boolean monitoring, Boolean processes, Boolean general, Boolean power) throws SQLException {
       startDB();

       Statement stmt = con.createStatement();
        int result = stmt.executeUpdate("INSERT INTO groups(user_group, administator, monitoring, processes, general, power) VALUES ('"+user_group+"', '"+administrator+"', '"+monitoring+"', '"+processes+"', '"+general+"', '"+power+"');");
       stmt.close();

       stopDB();

       return result;
    }

    public static int updatePermissions(String user_group, Boolean administrator, Boolean monitoring, Boolean processes, Boolean general, Boolean power) throws SQLException, IllegalArgumentException {
        if (user_group == "Default") {
            throw new IllegalArgumentException("Default user modify attempt. Default user is reserved to assign zero permissions and is therefore declared unmodifiable.");
        }

        startDB();

        Statement stmt = con.createStatement();
        int result = stmt.executeUpdate("UPDATE groups SET administator='"+administrator+"', monitoring='"+monitoring+"', processes='"+processes+"', general='"+processes+"', power='"+power+"' WHERE user_group='"+user_group+"';");
        stmt.close();

        stopDB();

        return result;
    }

    // Because deleting groups is unsupported (due to foreign key) this is provided instead for convenience.
    public static int resetPermissions(String user_group) throws SQLException, IllegalArgumentException {
        if (user_group == "Default") {
            throw new IllegalArgumentException("Default user modify attempt. Default user is reserved to assign zero permissions and is therefore declared unmodifiable.");
        }

        startDB();

        Statement stmt = con.createStatement();
        int result = stmt.executeUpdate("UPDATE groups SET administator=false, monitoring=false, processes=false, general=false, power=false WHERE user_group='"+user_group+"';");
        stmt.close();

        stopDB();

        return result;
    }

    public static int addSetting(String setting, String value) throws SQLException {
        startDB();

        Statement stmt = con.createStatement();
        int result = stmt.executeUpdate("INSERT INTO settings(setting, value) VALUES ('"+setting+"', '"+value+"');");
        stmt.close();

        stopDB();

        return result;
    } //get set

    public static String getSetting(String setting) throws SQLException {
        startDB();

        Statement stmt = con.createStatement();
        stmt.execute("SELECT * FROM settings WHERE = setting='"+setting+"'");
        ResultSet results = stmt.getResultSet();
        stmt.close();

        stopDB();

        return results.getString(0);
    }

    public static int setSettings(String setting, String value) throws SQLException {
        startDB();

        Statement stmt = con.createStatement();
        int result = stmt.executeUpdate("UPDATE settings SET value='" + value + "' WHERE setting='" + setting + "';");
        stmt.close();

        stopDB();

        return result;
    }

    /**
     * This method of wiping the database involves fully deleting the data folder and its contents
     * recursively. After this, the server will need to be restarted for the effects to be registed.
     * If processed correctly, the setup screen should appear on next launch.
     * @return TRUE for successful wipe, FALSE otherwise.
     */
    public static boolean wipeDatabase() {
        return recursiveDelete(dbPath);
    }

    // Developer note: This and wipeDatabase are two methods to encapsulate dbPath.
    private static boolean recursiveDelete(File dir) {
        File[] allContents = dir.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                recursiveDelete(file);
            }
        }
        return dir.delete();
    }
}
