package Controller.Socket.Task;

import Model.Task;
import Model.UserData;
import Controller.Database.DBManager;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

/**
 * This class holds all the methods needed to perform the tasks that this application functionally needs. It handles
 * authorisation on its own and will return errors codes when there is a problem. If you wish to let the user known of a
 * problem or why something failed, please refer to these error codes:
 *
 * An (error) code referring to the success of the operation. The codes are as follows:
 * - 0 - Success without issue.
 * - 1 - Generic error where the request couldn't be handled or processed as expected. Client side can be 99 when
 *       Response aren't retrievable by client so are created manually.
 * - 10 - Username not found.
 * - 11 - Username found but incorrect password.
 * - 12 - User details verified but account is suspended
 * - 19 - User details verified but permissions are not satisfied.
 * - 20 - An SQL exception was thrown that wasn't handled.
 * - 44 - Data given couldn't be found within the request i.e. row not found when updating a line in the database.
 */
public class RAMTTaskLibrary {

    /**
     * This method is to encapsulate the authorisation of task and is called upon by other methods before processing any
     * tasks. If the task was successful then the return of this method will be 0. Any value greater than 0 indicates an
     * error.
     * @param user The UserData object refering to the user.
     * @param task The task enum representing the requested task.
     * @return An (error) code referring to the success of the operation. For a list of the error codes, please refer to
     * the classes JavaDoc.
     */
    private static int authorise(UserData user, Task task) {

        return 1;
    }

    /**
     *
     * @param user
     * @return An (error) code referring to the success of the operation. For a list of the error codes, please refer to
     * the classes JavaDoc.
     */
    public static int login(UserData user) {
        try {
            if (DBManager.getUser(user.getUsername()) == null) {
                return 10;
            } else if (DBManager.verifyPassword(user.getUsername(), user.getPassword())) {
                return 0;
            } else {
                return 11;
            }
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | SQLException e) {
            e.printStackTrace();
            return 20;
        }
    }

    public static int killProcess(int pid) {
        return 1;
    }

    public static int restartProcess(int pid) {
        return 1;
    }

    public static int fetchProcesses() {
        return 1;
    }

    public static int shutdown(int delay) {
        return 1;
    }

    public static int restart(int delay) {
        return 1;
    }

    public static int sleep(int delay) {
        return 1;
    }

    public static int addUser(UserData user) {
        return 1;
    }

    public static int editUser(UserData newUser, UserData oldUser) {
        return 1;
    }

    public static int deleteUser(UserData user) {
        return 1;
    }

    public static int getSettings(String setting) {
        return 1;
    }

    public static int editSetting(String setting) {
        return 1;
    }

    public static int startFTP() {
        return 1;
    }

    public static int stopFTP() {
        return 1;
    }

    public static int restartFTP() {
        return 1;
    }

    public static int cleanDisk() {
        return 1;
    }

    public static int enableWifi() {
        return 1;
    }

    public static int disableWifi() {
        return 1;
    }

    public static int enableBluetooth() {
        return 1;
    }

    public static int disableBluetooth() {
        return 1;
    }

}
