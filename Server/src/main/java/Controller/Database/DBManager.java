package Controller.Database;

import Controller.CryptographyToolbox;
import Model.UserData;
import Model.UserGroup;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;

/** An (error) code referring to the success of the operation. The codes are as follows:
 * - 0 - Success without issue.
 * - 1 - Generic error where the request couldn't be handled or processed as expected.
 * - 10 - Username not found.
 * - 11 - Username found but incorrect password.
 * - 12 - User details verified but account is suspended
 * - 19 - User details verified but permissions are not satisfied.
 * - 20 - An SQL exception was thrown that wasn't handled.
 * - 21 - Duplicate SQL error. When a value given would of violated a unique column for example.
 * - 44 - Data given couldn't be found within the request i.e. row not found when updating a line in the database.
 */
public class DBManager {
    private static Connection con;
    private static final File dbPath = new File("data/");
    private static final File db = new File(dbPath+"/db.mv.db");

    private static final String dbName = "RAMT";
    private static final String dbPassword = "BAdgAvHML'5qK+_S";


    public static boolean isSetup() {
        if( db.isFile() ) {
            return true;
        } else  {
            return false;
        }
    }

    public static boolean setup(String username, String password, Boolean secure, int port) throws SQLException {
        try {

            if( !isSetup() ) {
                System.out.println("No DB detected, starting setup.");
                setupDB();
                setupData(username, password, secure, port);
            } else  {
                throw new IllegalStateException("Database already exists. Please wipe it before trying to setup.");
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Starts the database while also checking for an errors. If the database hasn't been setup then it will attempt to
     * do so. As it was last knowingly configured. The user data wasn't setup too. This must be done manually later
     * while prompting the user to enter the required data.
     */
    private static boolean startDB() {
        try
        {
            Class.forName("org.h2.Driver");
            con = DriverManager.getConnection("jdbc:h2:./data/db", dbName, dbPassword );
            return true;
        }
        catch( Exception e )
        {
            System.out.println( e.getMessage() );
        } finally {
            return false; //To make compiler happy.
        }
    }

    private static void stopDB() throws SQLException {
        if (con != null) {
            con.close();
            con = null;
        }
    }

    private static void setupDB() throws SQLException {
        con = DriverManager.getConnection("jdbc:h2:./data/db",  dbName, dbPassword);
        Statement stmt = con.createStatement();
        stmt.executeUpdate( "create table users (id uuid DEFAULT random_uuid() NOT NULL PRIMARY KEY," +
                "                    username varchar(64) NOT NULL," +
                "                    password varchar(512) NOT NULL," +
                "                    user_group varchar(64) DEFAULT 'Default' NOT NULL," +
                "                    UNIQUE (username));");
        stmt.executeUpdate( "create table groups (user_group varchar(64) NOT NULL primary key," +
                "                    administrator boolean(0) default false NOT NULL, " +
                "                    monitoring boolean(0) default false NOT NULL, " +
                "                    processes boolean(0) default false NOT NULL, " +
                "                    general boolean(0) default false NOT NULL, " +
                "                    power boolean(0) default false NOT NULL);" );

        stmt.executeUpdate( "create table settings (setting varchar(128) NOT NULL primary key," +
                "                     value varchar(512) default false NOT NULL);" );

        stmt.executeUpdate("ALTER TABLE users ADD FOREIGN KEY (user_group) REFERENCES groups(user_group);");

        stmt.executeUpdate("INSERT INTO groups(user_group) VALUES ('Default');");

        stmt.executeUpdate("INSERT INTO groups(user_group, administrator, monitoring, processes, general, power) " +
                "                   VALUES ('Administrator', true, true, true, true, true);");

        stmt.close();
        con.close();
    }

    private static void setupData(String username, String password, Boolean secure, int port) throws SQLException {
        con = DriverManager.getConnection("jdbc:h2:./data/db",  dbName, dbPassword);
        Statement stmt = con.createStatement();

        stmt.executeUpdate( "INSERT INTO users(ID, username, password, user_group) VALUES ('0', '" + username +"', '" + password +"', 'Administrator');" );

        addSetting("Port", String.valueOf(port));
        addSetting("Security",  secure.toString());

        stmt.close();

        if (con != null) {
            con.close();
        }
    }

    /*> Queries <*/

    public static int addUser(UserData user) throws SQLException {
        startDB();

        Statement stmt = con.createStatement();

        int result;
        try {
            result = stmt.executeUpdate("INSERT INTO users(username, password, user_group) VALUES ('" +
                    user.getUsername() + "', '" +
                    user.getPassword() + "', '" +
                    user.getGroup() + "');");

        } catch (JdbcSQLIntegrityConstraintViolationException e) {
            result = 21;
        }

        result = result > 0 ? result : 1; // Because if result was 0 that means no rows effects,
        // so error 1 to reflect something went wrong.

        stmt.close();
        stopDB();

        return result;
    }

    public static UserData getUser(String username) throws SQLException {
        startDB();

        Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        stmt.execute("SELECT * FROM users WHERE username='"+username+"'");
        return parseUserDataFromDB(stmt);
    }

    public static UserData getUserByID(String id) throws SQLException {
        startDB();

        Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        stmt.execute("SELECT * FROM users WHERE ID='"+id+"'");
        return parseUserDataFromDB(stmt);
    }

    public static ArrayList<UserData> getAllUsers() throws SQLException {
        startDB();

        Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT * FROM users");

        ArrayList<UserData> users = resultSetToUserDataList(rs);;

        stmt.close();
        stopDB();



        return users;
    }

    /**
     * This method parses the data from the database into a userdata object. It will also close the stmt and database
     * connection when finished. If this is undesirable then an alternate method is available where you pass the result
     * set which can't close the database.
     * @param stmt the Statement object which executed the query for userdata.
     * @return A UserData object.
     * @throws SQLException Thrown when SQL exceptions occur. Please ensure valid queries are executed to avoid this,
     * please ensure queries are correct and not malformed.
     */
    private static UserData parseUserDataFromDB(Statement stmt) throws SQLException {
        ResultSet rs = stmt.getResultSet();
        UserData user = resultSetToUserData(rs);

        stmt.close();
        stopDB();

        return user;
    }

    /**
     * This method will attempt to build the UserData object from the first result in the database and the server
     * itself. For example, the host and port will be attempted to be filled by the server application but if there is
     * an exception then this information should be null. To avoid running into problems by possible null values, it is
     * recommended to not use this information in the clientside. Instead, stick to using the User's data portion of the
     * UserData.
     * @param rs The result to be converted to UserData.
     * @return The UserData object converted from the UserData with information missing being filled by the server where
     * possible. Read general description for more information.
     * @throws SQLException Thrown when SQL exceptions occur. Please ensure valid queries are executed to avoid this,
     * please ensure queries are correct and not malformed.
     */
    private static UserData resultSetToUserData(ResultSet rs) throws SQLException {
        rs.beforeFirst();

        int rowCount = rs.last() ? rs.getRow() : 0;

        UserData results = null;
        if (rowCount > 0) {
            rs.first();

            String host = null;

            try {
                host = InetAddress.getLocalHost().toString();
            } catch (UnknownHostException e) {
                System.out.println("resultSetToUserData: Couldn't retrieving host so leaving value null.");
            }

            int port = Integer.parseInt(getSetting("Port"));

            results = new UserData(
                    host,
                    port,
                    rs.getString(1),
                    rs.getString(2),
                    null,
                    rs.getString(4));
        }
        return results;
    }

    /**
     * This method will attempt to build the UserData object from information in the database and the server itself. For
     * example, the host and port will be attempted to be filled by the server application but if there is an exception
     * then this information should be null. To avoid running into problems by possible null values, it is recommended
     * to not use this information in the clientside. Instead, stick to using the User's data portion of the UserData.
     * @param rs The result to be converted to UserData.
     * @return The UserData object converted from the UserData with information missing being filled by the server where
     * possible. Read general description for more information.
     * @throws SQLException Thrown when SQL exceptions occur. Please ensure valid queries are executed to avoid this,
     * please ensure queries are correct and not malformed.
     */
    private static ArrayList<UserData> resultSetToUserDataList(ResultSet rs) throws SQLException {
        rs.beforeFirst();

        int rowCount = rs.last() ? rs.getRow() : 0;

        ArrayList<UserData> results = new ArrayList<>();

        if (rowCount > 0) {
            rs.beforeFirst();

            while (rs.next()) {
                String host = null;

                try {
                    host = InetAddress.getLocalHost().toString();
                } catch (UnknownHostException e) {
                    System.out.println("resultSetToUserData: Couldn't retrieving host so leaving value null.");
                }

                int port = Integer.parseInt(getSetting("Port"));

                results.add(new UserData(
                        host,
                        port,
                        rs.getString(1),
                        rs.getString(2),
                        null,
                        rs.getString(4)));
            }
        }
        return results;
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

        result = result > 0 ? 0 : 1;

        stmt.close();
        stopDB();

        return result;
    }

    public static boolean verifyPassword(String username, String password) throws SQLException, InvalidKeySpecException, NoSuchAlgorithmException {
        startDB();

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT password FROM users WHERE username='"+username+"';");

        rs.first();

        String results = rs.getString(1);
        stmt.close();

        stopDB();

        System.out.println(password);
        System.out.println(results);

        return CryptographyToolbox.validatePBKDF2WithHmacSHA1(password, results);
    }

    public static boolean verifyPasswordByID(String id, String password) throws SQLException, InvalidKeySpecException, NoSuchAlgorithmException {
        startDB();

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT password FROM users WHERE ID='"+id+"';");

        rs.first();

        String results = rs.getString(1);
        stmt.close();

        stopDB();

        System.out.println(password);
        System.out.println(results);

        return CryptographyToolbox.validatePBKDF2WithHmacSHA1(password, results);
    }



    public static ArrayList<UserGroup> getAllGroups() throws SQLException {
        startDB();

        Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT * FROM groups");

        ArrayList<UserGroup> groups = resultSetToGroupList(rs);;

        stmt.close();
        stopDB();



        return groups;
    }

    /**
     * This method will attempt to build the UserData object from information in the database and the server itself. For
     * example, the host and port will be attempted to be filled by the server application but if there is an exception
     * then this information should be null. To avoid running into problems by possible null values, it is recommended
     * to not use this information in the clientside. Instead, stick to using the User's data portion of the UserData.
     * @param rs The result to be converted to UserData.
     * @return The UserData object converted from the UserData with information missing being filled by the server where
     * possible. Read general description for more information.
     * @throws SQLException Thrown when SQL exceptions occur. Please ensure valid queries are executed to avoid this,
     * please ensure queries are correct and not malformed.
     */
    private static ArrayList<UserGroup> resultSetToGroupList(ResultSet rs) throws SQLException {
        rs.beforeFirst();

        int rowCount = rs.last() ? rs.getRow() : 0;

        ArrayList<UserGroup> results = new ArrayList<>();

        if (rowCount > 0) {
            rs.beforeFirst();

            while (rs.next()) {
                String host = null;

                try {
                    host = InetAddress.getLocalHost().toString();
                } catch (UnknownHostException e) {
                    System.out.println("resultSetToUserData: Couldn't retrieving host so leaving value null.");
                }

                int port = Integer.parseInt(getSetting("Port"));

                results.add(new UserGroup(
                        rs.getString(1),
                        rs.getBoolean(2),
                        rs.getBoolean(3),
                        rs.getBoolean(4),
                        rs.getBoolean(5),
                        rs.getBoolean(6))
                );
            }
        }
        return results;
    }

    public static int updateGroup(UserData user, String newGroup) throws SQLException {
        startDB();

        Statement stmt = con.createStatement();
        int result = stmt.executeUpdate( "UPDATE users SET user_group='" + newGroup + "' WHERE username='" + user.getUsername() + "';");

        result = result > 0 ? 0 : 1;

        stmt.close();
        stopDB();

        return result;
    }

    public static int addGroup(UserGroup group) throws SQLException {
        startDB();

        Statement stmt = con.createStatement();
        int result = stmt.executeUpdate("INSERT INTO groups(" +
                "user_group, " +
                "administrator, " +
                "monitoring, " +
                "processes, " +
                "general, " +
                "power) VALUES (" +
                "'"+group.getName()+"', " +
                "'"+group.getAdmin()+"', " +
                "'"+group.getMonitoring()+"', " +
                "'"+group.getProcess()+"', " +
                "'"+group.getGeneral()+"', " +
                "'"+group.getPower()+"');");

        result = result > 0 ? 0 : 1;

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

        return result > 0 ? 0 : 1;
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

        return result > 0 ? 0 : 1;
    }

    public static int addSetting(String setting, String value) throws SQLException {
        startDB();

        Statement stmt = con.createStatement();
        int result = stmt.executeUpdate("INSERT INTO settings(setting, value) VALUES ('"+setting+"', '"+value+"');");

        stmt.close();
        stopDB();

        return result > 0 ? 0 : 1;
    } //get set

    public static String getSetting(String setting) throws SQLException {
        startDB();

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM settings WHERE setting='"+setting+"'");

        rs.first();
        rs.getString(1);

        String results = rs.getString(2);
        stmt.close();

        stopDB();

        return results;
    }

    public static int setSettings(String setting, String value) throws SQLException {
        startDB();

        Statement stmt = con.createStatement();
        int result = stmt.executeUpdate("UPDATE settings SET value='" + value + "' WHERE setting='" + setting + "';");

        stmt.close();
        stopDB();

        return result > 0 ? 0 : 1;
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
