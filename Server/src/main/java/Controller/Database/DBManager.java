package Controller.Database;

import Controller.CryptographyToolbox;
import Model.User.UserData;
import Model.User.UserGroup;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.h2.jdbc.JdbcSQLNonTransientException;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/** An (error) code referring to the success of the operation. The codes are as follows:
 * - 0 - Success without issue.
 * - 1 - Generic error where the request failed normally i.e. no results found or can't add user.
 * - 2 - Parameters do not meet the require constraints of the database column, field or value.
 * - 3 - Parameter contained semantically immutable field such as root/default user in a delete query.
 * - 10 - Username not found.
 * - 11 - Username found but incorrect password.
 * - 12 - User details verified but account is suspended
 * - 19 - User details verified but permissions are not satisfied.
 * - 20 - An SQL exception was thrown that wasn't handled (correctly).
 * - 21 - Duplicate SQL error. When a value given would of violated a unique column for example.
 * - 44 - Data given couldn't be found within the request i.e. row not found when updating a line in the database.
 * - 99 - Catastrophic generic error. If this has returned, something has gone seriously wrong (i.e. unforeseen bugs).
 */
public class DBManager {
    private static Connection con;
    public static final File dbPath = new File("data/");
    private static final File db = new File(dbPath+"/db.mv.db");

    private static final String dbName = "RAMT";
    private static final String dbPassword = "BAdgAvHML'5qK+_S";

    private static final String defaultAdmin = "Administrator";
    private static final String defaultGroup = "Default";

    /*--- Setup  ---*/

    /**
     * Checks if the database file is present. If it is then the database is marked as setup.
     * It is possible the database is malformed however and this could occur through faulty backups.
     * @return True if database is found and false otherwise.
     */
    public static boolean isSetup() {
        return db.isFile();
    }

    /**
     * Attempts to setup database if it is found that no database file is found. Using this essentially completes the
     * installation of the application.
     * @param username A valid username of the root user.
     * @param hashedPassword A valid hashed password of the root user.
     * @param secure True if secure sockets should be used and false for plain sockets.
     * @param port The port in which the server should run on.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 2, 20
     */
    public static int setup(String username, String hashedPassword, Boolean secure, int port, String ftpUsername, String ftpPassword) {
        if (!isStringConstraint("username", username) ||
                !isStringConstraint("password", hashedPassword) ||
                !isIntegerConstraint("port", port)) {
            return 2; // Invalid value(s).
        }

        try {
            int setupData;

            if( !isSetup() ) {
                System.out.println("No DB detected, starting setup.");
                setupDB();
                setupData = setupData(username, hashedPassword, secure, port, ftpUsername, ftpPassword);
            } else  {
                IllegalStateException e = new IllegalStateException("Database already exists. Please wipe before trying to setup again.");
                e.printStackTrace();
                throw e;
            }

            if (setupData != 0) { return setupData; }
            else if (isSetup()) { return 0;}
        } catch (SQLException e) {
            e.printStackTrace();
            return 20;
        }

        return 99;
    }

    /*--- Users Queries ---*/

    /**
     * Adds a user to the database using a UserData Object. The object must have valid username, password & user_group.
     * A suspension status must be set too. The password will be hashed according to the needs of the server.
     * @param user A UserData object with the aforementioned valid values.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 2, 21
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int addUser(UserData user) throws SQLException {
        if (!isStringConstraint("username", user.getUsername()) ||
                !isStringConstraint("password", user.getPassword()) ||
                !isStringConstraint("user_group", user.getGroup())) { return 2; } // Invalid value(s).

        startDB();
        Statement stmt = con.createStatement();

        int result;
        try {
            result = stmt.executeUpdate("INSERT INTO users(username, password, user_group, suspended) VALUES ('" +
                    user.getUsername() + "', '" +
                    CryptographyToolbox.generatePBKDF2WithHmacSHA512(user.getPassword()) + "', '" +
                    user.getGroup() + "', '" +
                    user.isSuspended() + "');")
                    > 0 ? 0 : 1; // 0 for success true for couldn't add.
        } catch (JdbcSQLIntegrityConstraintViolationException e) {
            result = 21;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            result = 99;
        }

        stmt.close();
        stopDB();

        return result ;
    }

    /**
     *  Searches the DB for any row with the given username and returns the first found result. Passwords are redacted
     *  (null).
     * @param username A valid username.
     * @return A userdata object with all the found information of the user.
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static UserData getUser(String username) throws SQLException {

        startDB();

        Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        stmt.execute("SELECT * FROM users WHERE username='"+username+"'");
        return parseUserDataFromDB(stmt);
    }

    /**
     * Finds the user by its ID. Useful for getting the default admin for example. Passwords are redacted (null).
     * @param id A valid UUID string of the user to be found.
     * @return A userdata object with all the found information of the user.
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static UserData getUserByID(String id) throws SQLException {
        startDB();

        Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        stmt.execute("SELECT * FROM users WHERE ID='"+id+"'");
        return parseUserDataFromDB(stmt);
    }

    /**
     * Retrieves all users from the database with redacted (null) passwords.
     * @return An ArrayList of userdata object with all the found information of the users.
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static ArrayList<UserData> getAllUsers() throws SQLException {
        startDB();

        Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT * FROM users");

        ArrayList<UserData> users = resultSetToUserDataList(rs);

        stmt.close();
        stopDB();

        return users;
    }

    /**
     * Deletes the first found user with the matching username.
     * @param username A valid username of the user to be deleted.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 3, 21
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int deleteUser(String username) throws SQLException {
        if (DBManager.getUserByID("0").getUsername().equals(username)) { return 3;}

        startDB();
        Statement stmt = con.createStatement();

        int returnCode = stmt.executeUpdate( "DELETE FROM users WHERE username='" + username + "';") > 0 ? 0 : 1;
        // If result is more than 0 (Success), returning code 0!
        stmt.close();
        stopDB();

        return returnCode;
    }

    /**
     * Updates the given username to the newly provided one.
     * @param oldName The valid current name of the user.
     * @param newName The valid name to assign to the user.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 2
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int updateUsername(String oldName, String newName) throws SQLException {
        if (!isStringConstraint("username", oldName) ||
                !isStringConstraint("username", newName)) { return 2;}

        startDB();
        Statement stmt = con.createStatement();

        int returnCode = stmt.executeUpdate( "UPDATE users " +
                "SET username='" + newName + "' " +
                "WHERE username='" + oldName + "';")
                > 0 ? 0 : 1;// If result is more than 0 (Success), returning code 0!
        stmt.close();
        stopDB();

        return returnCode;
    }

    /**
     * Updates the users password. The password should be hashed.
     * @param username The valid username of the user.
     * @param newPassword The valid new hashed password of the user.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 2
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int updatePassword(String username, String newPassword) throws SQLException {
        if (!isStringConstraint("username", username) ||
                !isStringConstraint("password", newPassword)) { return 2;}

        startDB();

        Statement stmt = con.createStatement();
        int returnCode = stmt.executeUpdate( "UPDATE users " +
                "SET password='" + newPassword + "' " +
                "WHERE username='" + username + "';")
                > 0 ? 0 : 1;// If result is more than 0 (Success), returning code 0!

        stmt.close();
        stopDB();

        return returnCode;
    }

    /**
     * Checks if the password is valid according to the current hashing algorithm.
     * @param username The valid username of the user.
     * @param password The valid password of the user.
     * @return True for a password match and false otherwise.
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static boolean verifyPassword(String username, String password) throws SQLException, InvalidKeySpecException, NoSuchAlgorithmException {
        startDB();

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT password FROM users WHERE username='"+username+"';");

        rs.first();
        String results = rs.getString(1);

        stmt.close();
        stopDB();

        return CryptographyToolbox.validatePBKDF2WithHmacSHA512(password, results);
    }

    /**
     * Checks if the password is valid according to the current hashing algorithm via UUID.
     * @param id The valid UUID of the user.
     * @param password The valid password of the user.
     * @return True for a password match false otherwise.
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static boolean verifyPasswordByID(String id, String password) throws SQLException, InvalidKeySpecException, NoSuchAlgorithmException {
        startDB();

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT password FROM users WHERE ID='"+id+"';");

        rs.first();
        String results = rs.getString(1);

        stmt.close();
        stopDB();

        return CryptographyToolbox.validatePBKDF2WithHmacSHA512(password, results);
    }

    /**
     * Suspends the given user if they aren't the root account.
     * @param username A valid username.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 3
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int suspendUser(String username) throws SQLException {
        if (DBManager.getUserByID("0").getUsername().equals(username)) { return 3;}
        startDB();

        Statement stmt = con.createStatement();
        int resultCode = stmt.executeUpdate( "UPDATE users " +
                "SET suspended=true " +
                "WHERE username='" + username + "';")
                > 0 ? 0 : 1;// If result is more than 0 (Success), returning code 0!

        stmt.close();
        stopDB();

        return resultCode;
    }

    /**
     * Unsuspend the given user if they aren't the root account.
     * @param username A valid username.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 3
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int unsuspendUser(String username) throws SQLException {
        if (DBManager.getUserByID("0").getUsername().equals(username)) { return 3;}
        startDB();

        Statement stmt = con.createStatement();
        int resultCode = stmt.executeUpdate( "UPDATE users " +
                "SET suspended=false " +
                "WHERE username='" + username + "';")
                > 0 ? 0 : 1;// If result is more than 0 (Success), returning code 0!

        stmt.close();
        stopDB();

        return resultCode;
    }

    /**
     * Moves to user to the group given. The user must not be the root account.
     * @param username The valid username of the user to be moved.
     * @param groupName The valid group name of the group to move the user too.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 2, 3
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int changeUserGroup(String username, String groupName) throws SQLException {
        if (!isStringConstraint("username", username)) { return 2;}
        else if (!isStringConstraint("user_group", groupName)) { return 2;}
        else if (DBManager.getUserByID("0").getUsername().equals(username)) {return 3; }

        startDB();
        Statement stmt = con.createStatement();

        int returnCode = stmt.executeUpdate( "UPDATE users " +
                "SET user_group='" + groupName + "' " +
                "WHERE username='" + username + "';")
                > 0 ? 0 : 1;// If result is more than 0 (Success), returning code 0!

        stmt.close();
        stopDB();

        return returnCode;
    }

    /*--- Groups Queries ---*/

    /**
     * Gets all the group names and compiles them into an ArrayList as a return.
     * @return The ArrayList of UserGroup Objects representing the groups.
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static UserGroup getGroup(String user_group) throws SQLException {
        startDB();

        Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT * FROM groups WHERE user_group='"+user_group+"'");

        UserGroup group = resultSetToGroup(rs);

        stmt.close();
        stopDB();

        return group;
    }

    /**
     * Gets all the group names and compiles them into an ArrayList as a return.
     * @return The ArrayList of UserGroup Objects representing the groups.
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static ArrayList<UserGroup> getAllGroups() throws SQLException {
        startDB();

        Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT * FROM groups");

        ArrayList<UserGroup> groups = resultSetToGroupList(rs);

        stmt.close();
        stopDB();

        return groups;
    }

    /**
     * Updates a given group's name to a new one provided.
     * @param oldName The current valid name of the group.
     * @param newName The new valid name to assign to the group.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 2, 3
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int updateGroupName(String oldName, String newName) throws SQLException {
        if (!isStringConstraint("user_group", oldName)) { return 2;}
        else if (!isStringConstraint("user_group", newName)) { return 2;}
        else if (oldName.equals(defaultAdmin) || oldName.equals(defaultGroup)) {return 3; }
        else if (newName.equals(defaultAdmin) || newName.equals(defaultGroup)) {return 3; }

        startDB();

        Statement stmt = con.createStatement();
        int returnCode =  stmt.executeUpdate( "UPDATE groups " +
                "SET user_group='" + newName + "' " +
                "WHERE user_group='" + oldName + "';")
                > 0 ? 0 : 1;// If result is more than 0 (Success), returning code 0!

        stmt.close();
        stopDB();

        return returnCode;
    }

    /**
     * Adds a group to the Database using the UserGroup object to represent it.
     * @param group The UserGroup object representing the user to add.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 2, 3
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int addGroup(UserGroup group) throws SQLException {
        if (!isStringConstraint("user_group", group.getName())) { return 2;}
        else if (group.getName().equals(defaultAdmin) || group.getName().equals(defaultGroup)) {return 3; }

        startDB();

        Statement stmt = con.createStatement();
        int returnCode = stmt.executeUpdate("INSERT INTO groups(" +
                "user_group, " +
                "administrator, " +
                "general, " +
                "processes, " +
                "monitoring, " +
                "power) VALUES (" +
                "'"+group.getName()+"', " +
                "'"+group.getAdmin()+"', " +
                "'"+group.getGeneral()+"', " +
                "'"+group.getProcess()+"', " +
                "'"+group.getMonitoring()+"', " +
                "'"+group.getPower()+"');")
                > 0 ? 0 : 1;// If result is more than 0 (Success), returning code 0!

        stmt.close();
        stopDB();

        return returnCode;
    }

    /**
     * Updates the permissions of the given group by name.
     *
     * Note: Assigning Administrator might as well be giving full permissions as they can create and change any users
     * permissions.
     * @param user_group The name of the group to update its permissions.
     * @param administrator Administrative (master/all) privileges if true, false to deny this privilege.
     * @param general General privileges if true, false to deny this privilege.
     * @param processes Process privileges if true, false to deny this privilege.
     * @param monitoring Monitoring privileges if true, false to deny this privilege.
     * @param power Power Management privileges if true, false to deny this privilege.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 2, 3
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int updatePermissions(String user_group, Boolean administrator, Boolean general, Boolean processes, Boolean monitoring, Boolean power) throws SQLException {
        if (!isStringConstraint("user_group", user_group)) { return 2;}
        else if (user_group.equals(defaultAdmin) || user_group.equals(defaultGroup)) {return 3; }

        startDB();

        Statement stmt = con.createStatement();
        int returnCode = stmt.executeUpdate("UPDATE groups " +
                "SET " +
                "administrator='"+administrator+"', " +
                "monitoring='"+monitoring+"', " +
                "processes='"+processes+"', " +
                "general='"+general+"', " +
                "power='"+power+"' " +
                "WHERE user_group='"+user_group+"';")
                > 0 ? 0 : 1;// If result is more than 0 (Success), returning code 0!

        stmt.close();
        stopDB();

        return returnCode;
    }

    /**
     * Migrate all users within the given group to another provided group.
     * @param fromGroup The original group with the users to move.
     * @param toGroup The new group move users to be moved into.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 2, 3
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int migrateGroup(String fromGroup, String toGroup) throws SQLException {
        if (!isStringConstraint("user_group", fromGroup)) { return 2;}
        else if (!isStringConstraint("user_group", toGroup)) { return 2;}
        else if (fromGroup.equals(defaultAdmin) || fromGroup.equals(defaultGroup)) {return 3; }

        startDB();

        Statement stmt = con.createStatement();
        int returnCode = stmt.executeUpdate( "UPDATE users " +
                "SET user_group='" + toGroup + "' " +
                "WHERE user_group='" + fromGroup + "';")
                > 0 ? 0 : 1;// If result is more than 0 (Success), returning code 0!

        stmt.close();
        stopDB();

        return returnCode;
    }

    /**
     * The group of users in which all shall be suspended that are 'currently' in.
     * @param group The group name to suspend its users.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 2, 3
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int suspendGroupUsers(String group) throws SQLException {
        if (!isStringConstraint("user_group", group)) { return 2;}
        else if (group.equals(defaultAdmin) || group.equals(defaultGroup)) {return 3; }

        startDB();

        Statement stmt = con.createStatement();
        int returnCode =stmt.executeUpdate( "UPDATE users " +
                "SET suspended=True " +
                "WHERE user_group='" + group + "';")
                > 0 ? 0 : 1;// If result is more than 0 (Success), returning code 0!

        stmt.close();
        stopDB();

        return returnCode;
    }

    /**
     * The group of users in which all shall be unsuspended that are 'currently' in.
     * @param group The group name to unsuspend its users.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 2, 3
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int unsuspendGroupUsers(String group) throws SQLException {
        if (!isStringConstraint("user_group", group)) { return 2;}
        else if (group.equals(defaultAdmin) || group.equals(defaultGroup)) {return 3; }

        startDB();

        Statement stmt = con.createStatement();
        int returnCode = stmt.executeUpdate( "UPDATE users " +
                "SET suspended=False " +
                "WHERE user_group='" + group + "';")
                > 0 ? 0 : 1;// If result is more than 0 (Success), returning code 0!

        stmt.close();
        stopDB();

        return returnCode;
    }

    /**
     * Deletes all of the users in the group while leaving the group remaining.
     * @param group The group's name which contains the users to be delete.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 2, 3
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int deleteGroupUsers(String group) throws SQLException {
        if (!isStringConstraint("user_group", group)) { return 2;}
        else if (group.equals(defaultAdmin) || group.equals(defaultGroup)) {return 3; }

        startDB();

        Statement stmt = con.createStatement();
        int returnCode = stmt.executeUpdate( "DELETE FROM users " +
                "WHERE user_group='" + group + "';")
                > 0 ? 0 : 1;// If result is more than 0 (Success), returning code 0!

        stmt.close();
        stopDB();

        return returnCode;
    }

    /**
     * Deletes the group which if there any users left inside shall be assigned to the default group.
     * @param group The group's name to delete.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 2, 3
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int deleteGroup(String group) throws SQLException {
        if (!isStringConstraint("user_group", group)) { return 2;}
        else if (group.equals(defaultAdmin) || group.equals(defaultGroup)) {return 3; }

        startDB();

        Statement stmt = con.createStatement();
        int returnCode = stmt.executeUpdate( "DELETE FROM groups " +
                "WHERE user_group='" + group + "';")
                > 0 ? 0 : 1;// If result is more than 0 (Success), returning code 0!

        stmt.close();
        stopDB();

        return returnCode;
    }

    /*--- Settings Queries ---*/

    /**
     * Adds a setting to the database which a key setting value used as its name and a value which is the same one to
     * store under the key. If there is already a setting with the same key then the value cannot be inserted.
     * @param setting The key name of the setting.
     * @param value The value to store under the key.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 2
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int addSetting(String setting, String value) throws SQLException {
        if (!isStringConstraint("setting", setting) ||
                !isStringConstraint("setting_value", value)) { return 2;}

        // More specific contraints
        try {
            if (setting.equals("Monitoring Polling Rate") && !isIntegerConstraint(setting, Float.parseFloat(value))) {
                return 2;
            } else if (setting.contains("Port") && !isIntegerConstraint("Port", Integer.parseInt(value))) {
                return 2;
            }
        } catch (NumberFormatException e) {return  2;} // Invalid setting for polling rate.

        startDB();

        Statement stmt = con.createStatement();
        int result = stmt.executeUpdate("INSERT INTO settings(setting, value) " +
                "VALUES ('"+setting+"', '"+value+"');")
                > 0 ? 0 : 1;// If result is more than 0 (Success), returning code 0!

        stmt.close();
        stopDB();

        return result;
    } //get set

    /**
     * Gets the setting value of the given setting's key name.
     * @param setting The setting's key name.
     * @return The value of the setting if any.
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static String getSetting(String setting) throws SQLException {
        startDB();

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM settings WHERE setting='"+setting+"'");

        rs.next();
        String result = rs.getString(2);

        stmt.close();
        stopDB();

        return result;
    }

    /**
     * Gets all settings at once in a hashmap.
     * @return The value of the settings in a hashmap.
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static HashMap<String, String> getSettings() throws SQLException {
        startDB();

        HashMap<String,String> results = new HashMap<>();

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM settings");

        while (rs.next()) { results.put(rs.getString(1), rs.getString(2)); }

        stmt.close();
        stopDB();

        return results;
    }

    /**
     * Updates the given setting's value via its key. If there is no key value then nothing will happen (i.e. error 1).
     * @param setting The setting's key name.
     * @param value The value to update the setting to.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 2
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int updateSetting(String setting, String value) throws SQLException {
        if (!isStringConstraint("setting", setting) ||
                !isStringConstraint("setting_value", value)) { return 2;}

        // More specific contraints
        try {
            if (setting.equals("Monitoring Polling Rate") && !isIntegerConstraint(setting, Float.parseFloat(value))) {
                return 2;
            } else if (setting.contains("Port") && !isIntegerConstraint("Port", Integer.parseInt(value))) {
                return 2;
            }
        } catch (NumberFormatException e) {return  2;} // Invalid setting for polling rate.

        startDB();

        Statement stmt = con.createStatement();
        int resultCode = stmt.executeUpdate("UPDATE settings " +
                "SET value='" + value + "' " +
                "WHERE setting='" + setting + "';")
                > 0 ? 0 : 1;// If result is more than 0 (Success), returning code 0!

        stmt.close();
        stopDB();

        return resultCode;
    }
    /**
     * Updates the given setting's value via its key. If there is no key value then nothing will happen (i.e. error 1).
     * @param settings The hashmap with the keys being equal to the setting to be changed and its key value being the
     *                 new value of the setting itself.
     * @return A RAMT DBManager class result code
     *
     * Possible Codes: 0, 1, 2
     * @throws SQLException Thrown when the SQL query couldn't be completed. Perhaps because of bad parameters.
     */
    public static int updateSettings(HashMap<String, String> settings) throws SQLException {
        for (String key: settings.keySet()) {
            System.out.println(key +"|"+settings.get(key));
            if (!isStringConstraint("setting", key) ||
                    !isStringConstraint("setting_value", settings.get(key))) { return 2;}
            try {
                if (key.equals("Monitoring Polling Rate") && !isIntegerConstraint(key, Float.parseFloat(settings.get(key)))) {
                    return 2;
                } else if (key.contains("Port") && !isIntegerConstraint("Port", Integer.parseInt(settings.get(key)))) {
                    return 2;
                }
            } catch (NumberFormatException e) {return  2;}
        }

        startDB();

        int resultCode = 0;

        for (String key: settings.keySet()) {
            Statement stmt = con.createStatement();

            resultCode += stmt.executeUpdate("UPDATE settings " +
                    "SET value='" + settings.get(key) + "' " +
                    "WHERE setting='" + key + "';")
                    > 0 ? 0 : 1;// If result is more than 0 (Success), returning code 0!

            stmt.close();
        }
        stopDB();

        return resultCode > 0 ? 1 : 0;
    }

    /*--- Private and/or assisting functions ---*/

    /**
     * Used internally to check if the given value matches the constraints within the DB via hardcoded values.
     * First a constraint is given then the value. If the value doesn't meet the constraint then it fails the check.
     * Booleans do not need to be check against constraints as there are only two possible values to be given with the
     * data type.
     * @param constraint The constraint to test the value against. Constraints:
     *                   -username
     *                   -password
     *                   -user_group
     *                   -setting
     *                   -setting-value
     * @param value The value to check against the constraint.
     * @return True if the value matches the given constraint false otherwise.
     */
    private static boolean isStringConstraint(String constraint, String value) {
        if (constraint == null || value == null) {
            return false;
        }

        return switch (constraint.toLowerCase()) {
            case "username", "user_group"    -> value.length() > 0 && value.length() <= 64;
            case "password", "setting_value" -> /* Zero and... */     value.length() <= 512;
            case "setting"                   -> value.length() > 0 && value.length() <= 128;
            default -> false; // No constraint found.
        };
    }
    /**
     * Used internally to check if the given value matches the constraints within the DB via hardcoded values.
     * First a constraint is given then the value. If the value doesn't meet the constraint then it fails the check.
     * Booleans do not need to be check against constraints as there are only two possible values to be given with the
     * data type.
     * @param constraint The constraint to test the value against. Constraints:
     *                   -port
     * @param value The value to check against the constraint.
     * @return True if the value matches the given constraint false otherwise.
     */
    private static boolean isIntegerConstraint(String constraint, Number value) {
        if (constraint == null || value == null) {
            return false;
        }

        return switch (constraint.toLowerCase()) {
            case "port" -> value.intValue() > 0 && value.intValue() <= 65535; //Only the one, but write as such for upgrade/maintainability.
            case "monitoring polling rate" -> value.floatValue() >= 1.5 && value.floatValue() <= 256;
            default -> false; // No constraint found.
        };
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
     * itself. For example, the host could be the public ip or domain or a local host ip so that will remain null.
     * To avoid running into problems by possible null values, it is recommended to not use this information on the
     * client side in full. Instead, stick to using the User's data portion of the UserData and filling any blank's with
     * an available info here only if needed.
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

            int port = Integer.parseInt(getSetting("Port"));

            results = new UserData(
                    null,
                    port,
                    rs.getString(1),
                    rs.getString(2),
                    null,
                    rs.getString(4),
                    rs.getBoolean(5),
                    Boolean.parseBoolean(getSetting("Security")),
                    getGroup(rs.getString(4)),
                    Integer.parseInt(getSetting("Monitoring Port"))
            );
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

                int port;
                try {
                    port = Integer.parseInt(getSetting("Port"));
                } catch (JdbcSQLNonTransientException e) {
                    port = 3069; // fallback.
                }

                results.add(new UserData(
                       null,
                        port,
                        rs.getString(1),
                        rs.getString(2),
                        null,
                        rs.getString(4),
                        rs.getBoolean(5),
                        Boolean.parseBoolean(getSetting("Security")),
                        getGroup(rs.getString(4)),
                        Integer.parseInt(getSetting("Monitoring Port"))
                ));
            }
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
    private static UserGroup resultSetToGroup(ResultSet rs) throws SQLException {
        rs.beforeFirst();

        rs.next();

        try {
            return new UserGroup(
                    rs.getString(1),
                    rs.getBoolean(2),
                    rs.getBoolean(3),
                    rs.getBoolean(4),
                    rs.getBoolean(5),
                    rs.getBoolean(6));
        } catch (JdbcSQLNonTransientException e) { // No data from result set.
            return null;
        }
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

                System.out.println(rs.getString(1));

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

    /**
     * Starts the database while also checking for an errors. If the database hasn't been setup then it will attempt to
     * do so. As it was last knowingly configured. The user data wasn't setup too. This must be done manually later
     * while prompting the user to enter the required data.
     */
    private static void startDB() {
        try
        {
            Class.forName("org.h2.Driver");
            con = DriverManager.getConnection("jdbc:h2:./data/db", dbName, dbPassword );
        }
        catch( Exception e )
        {
            e.printStackTrace();
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
                "                    suspended bool DEFAULT false NOT NULL," +
                "                    UNIQUE (username));");
        stmt.executeUpdate( "create table groups (user_group varchar(64) NOT NULL primary key," +
                "                    administrator boolean(0) default false NOT NULL, " +
                "                    general boolean(0) default false NOT NULL, " +
                "                    processes boolean(0) default false NOT NULL, " +
                "                    monitoring boolean(0) default false NOT NULL, " +
                "                    power boolean(0) default false NOT NULL);" );

        stmt.executeUpdate( "create table settings (setting varchar(128) NOT NULL primary key," +
                "                     value varchar(512) default false NOT NULL);" );

        stmt.executeUpdate("ALTER TABLE users ADD FOREIGN KEY (user_group) " +
                "REFERENCES groups(user_group) ON UPDATE CASCADE ON DELETE SET DEFAULT;");

        stmt.executeUpdate("INSERT INTO groups(user_group) VALUES ('Default');");

        stmt.executeUpdate("INSERT INTO groups(user_group, administrator, monitoring, processes, general, power) " +
                "                   VALUES ('Administrator', true, true, true, true, true);");

        stmt.close();
        con.close();
    }

    private static int setupData(String username, String hashedPassword, Boolean secure, int port, String ftpUsername, String ftpPassword) throws SQLException {
        if (!isStringConstraint("username", username) ||
                !isStringConstraint("password", hashedPassword) ||
                !isIntegerConstraint("port", port)) { return 2;}

        con = DriverManager.getConnection("jdbc:h2:./data/db",  dbName, dbPassword);
        Statement stmt = con.createStatement();

        stmt.executeUpdate( "INSERT INTO users(ID, username, password, user_group) VALUES ('0', '" + username +"', '" + hashedPassword +"', 'Administrator');" );

        // User Assigned
        int result1 = addSetting("Port", String.valueOf(port));
        int result2 = addSetting("Security",  secure.toString());
        int result3 = addSetting("FTP Username", ftpUsername);
        int result4 = addSetting("FTP Password",  ftpPassword);

        // Non-User Assigned
        int result5 = addSetting("FTP Port", String.valueOf(2221));
        int result6 = addSetting("FTP Guest Enabled", "false");
        int result7 = addSetting("FTP Guest Username", "Guest");
        int result8 = addSetting("FTP Guest Password", "");
        int result9 = addSetting("Monitoring Polling Rate", "3");
        int result10 = addSetting("Monitoring Port", String.valueOf(port-1));

        stmt.close();
        if (con != null) {con.close();}

        // Checking for errors adding settings.
        if (result1 != 0) { return result1; }
        else if (result2 != 0) { return result2; }
        else if (result3 != 0) { return result3; }
        else if (result4 != 0) { return result4; }
        else if (result5 != 0) { return result5; }
        else if (result6 != 0) { return result6; }
        else if (result7 != 0) { return result7; }
        else if (result8 != 0) { return result8; }
        else if (result9 != 0) { return result9; }
        else return result10;
    }

    /**
     * This method of wiping the database involves fully deleting the data folder and its contents
     * recursively. After this, the server will need to be restarted for the effects to be registered.
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
