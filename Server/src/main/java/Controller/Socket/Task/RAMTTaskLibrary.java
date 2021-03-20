package Controller.Socket.Task;

import Model.General.AppPermission;
import Model.General.OSType;
import Model.Task.Response;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;
import Model.User.UserData;
import Controller.Database.DBManager;
import Model.User.UserGroup;
import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellNotAvailableException;
import com.profesorfalken.jpowershell.PowerShellResponse;
import org.json.*;

import java.io.*;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.*;

import static Model.General.OSType.*;

/**
 * This class holds all the methods needed to perform the tasks that this application functionally needs. It handles
 * authorisation on its own and will return errors codes when there is a problem. If you wish to let the user known of a
 * problem or why something failed, please refer to these error codes:
 *
 * A (error) code referring to the success of the operation. The codes are as follows:
 * - 0 - Success without issue.
 * - 1 - Generic error where the request failed normally i.e. no results found or can't add user.
 * - 2 - Parameters do not meet the require constraints of the database column, field or value.
 * - 3 - Parameter contained semantically immutable field such as root/default user in a delete query.
 * - 10 - Username not found.
 * - 11 - Username found but incorrect password.
 * - 12 - User details verified but account is suspended
 * - 19 - User details verified but permissions are not satisfied (Unauthorised).
 * - 20 - An SQL exception was thrown that wasn't handled (correctly).
 * - 21 - Duplicate SQL error. When a value given would of violated a unique column for example.
 * - 44 - Data given couldn't be found within the request i.e. row not found when updating a line in the database.
 * - 99 - Catastrophic generic error. If this has returned, something has gone seriously wrong (i.e. unforeseen bugs).
 */
public class RAMTTaskLibrary {
    private static PowerShell shell;

    /**
     * A semantic function that doesn't actually login a user but provides a response that lets a client know that their
     * UserData (user details) are valid in the database. This in turn allows them to know if they can make requests and
     * therefore what request can be made by reflecting on their provided UserData.
     *
     * This is all assuming the client is respecting the servers rules on TaskRequest processing i.e. client
     * respectfully knows that FetchProcess task will not succeed if the logged in user's group does not contain process
     * privileges.
     *
     * @param request The request sent from the client from login. Should contain the user details required to validate
     *                a login attempt.
     * @return A (error) code referring to the success of the operation. For a list of the error codes, please refer to
     *                the classes JavaDoc.
     */
    public static TaskResponse<Void> login(TaskRequest request) {
        try {
            UserData dbUser = DBManager.getUser(request.getUser().getUsername());

            if (dbUser == null) {  //If no user.
                return new TaskResponse<>(request, Response.FAILEDAUTHENTICATION, 10);
            } else {

                if (DBManager.verifyPassword(request.getUser().getUsername(), request.getUser().getPassword())) {

                    if (dbUser.isSuspended()) {
                        return new TaskResponse<>(request, Response.FAILEDAUTHENTICATION, 12);
                    } else { // everything is corrected.
                        return new TaskResponse<>(request, Response.SUCCESS, 0);
                    }

                } else { //Password wrong.
                    return new TaskResponse<>(request, Response.FAILEDAUTHENTICATION, 11);
                }

            }


        } catch (NoSuchAlgorithmException | SQLException | InvalidKeySpecException e) {
            e.printStackTrace();
            return new TaskResponse<>(request, Response.INTERRUPTED, 20);
        }
    }

    public static int killProcess(int pid) {
        try {
            switch (getOS()) {
                case WINDOWS_PS:
                    PowerShell shell = PowerShell.openSession();
                    shell.close();
                    break;
                case WINDOWS:
                    ProcessBuilder processBuilder = new ProcessBuilder();

                    // Run this on Windows, cmd, /c = terminate after this run
                    processBuilder.command("cmd.exe", "/c", "ping -n 3 google.com");

                    Process process = processBuilder.start();

                    // blocked :(
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }

                    int exitCode = process.waitFor();
                    System.out.println("\nExited with error code : " + exitCode);
                    break;
                case LINUX:
                    break;
                case MAC:
                    break;
                default: //OTHER
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return 99;
        }
        return 1;
    }

    public static int restartProcess(int pid) {
        return 1;
    }

    public static TaskResponse<String> fetchProcesses(TaskRequest request) {
        try {
            switch (getOS()) {
                case WINDOWS_PS:
                    String script = "controller/windows/ps/AllProcessesToJson.ps1";
                    BufferedReader srcReader = new BufferedReader(
                            new InputStreamReader(Objects.requireNonNull(
                                    ClassLoader.getSystemClassLoader().getResourceAsStream(script))));

                    PowerShellResponse response = shell.executeScript(srcReader); // Resource Hog. I've optimised the
                    // script plenty already

                    System.out.println(response.getCommandOutput());
                    return new TaskResponse<>(request, Response.SUCCESS, 0, response.getCommandOutput());
                case WINDOWS:
                    ProcessBuilder winCMD = new ProcessBuilder();
                    //TODO ALL UNTESTED AND UNFINISHED. None powershell is not fully supported maybe. alert user.
                    // Run this on Windows, cmd, /c = terminate after this run
                    winCMD.command("cmd.exe", "/c", "tasklist /V /fo CSV");

                    Process process = winCMD.start();
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }

                    int exitCode = process.waitFor();
                    System.out.println("Exited with error code : " + exitCode);
                    break;
                case LINUX:
                    break;
                case MAC:
                    File macScript = new File(Objects.requireNonNull(
                            ClassLoader.getSystemClassLoader().getResource("Controller/Mac/AllProcessesToJson.sh"))
                            .getFile());


                    String[] cmd = {"/bin/zsh",  macScript.getName(), macScript.getAbsolutePath()};

                    System.out.println(cmd);

                    Process macCMD = new ProcessBuilder(cmd).start();

                    BufferedReader macReader =
                            new BufferedReader(new InputStreamReader(macCMD.getInputStream()));

                    String macLine;
                    while ((macLine = macReader.readLine()) != null) {
                        System.out.println(macLine);
                    }

                    int macCode = macCMD.waitFor();
                    System.out.println("Exited with error code : " + macCode);
                    break;
                default: //OTHER
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return null;
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

    /**
     * Gets the current OS at runtime from the JVM. The current OS is will be from a list from the Oracle JDK 14
     * Certified System Configurations spec sheet as of 15th of March 2021. Anything not in this list somehow will
     * most likely be reported as other or unsupported.
     *
     * Note: JPowerShell has some delayed start to powershell commands so it is a good idea to call getOS() at least
     * once to warm up JPowerShell. This of course will only positively effect powershell environments.
     * @return The current OSType given from the JVM. If the OS type is unsupported or unrecognised then OSType OTHER
     * is returned.
     */
    public static OSType getOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        // Windows
        if (osName.contains("windows")) {
            try {

                if (shell == null) {
                    Map<String, String> myConfig = new HashMap<>();
                    myConfig.put("maxWait", "30000");
                    shell = PowerShell.openSession().configuration(myConfig);
                }

                return WINDOWS_PS;
            } catch (PowerShellNotAvailableException e) {
                return WINDOWS;
            }
        }

        // Linux
        if (osName.contains("linux")) {
            return LINUX;
        }

        //Mac
        if (osName.contains("mac")) {
            return MAC;
        }

        return OTHER; // unsupported.
    }

    /**
     * This method is to encapsulate the authorisation of task and is called upon by other methods before processing any
     * tasks. If the task was successful then the return of this method will be 0. Any value greater than 0 indicates an
     * error.
     * @param user The UserData object refering to the user.
     * @param permission The task's permissions enum representing the requested task.
     * @return A (error) code referring to the success of the operation. For a list of the error codes, please refer to
     * the classes JavaDoc.
     */
    private static boolean authorise(UserData user, AppPermission permission) throws SQLException {
        UserGroup group = DBManager.getGroup(user.getGroup());

        if (group.getAdmin().toLowerCase().equals("true")) {
            return true; // Because admins have access to all.
        } else {
            return switch (permission) {
                case GENERAL -> group.getGeneral().toLowerCase().equals("true");
                case PROCESS -> group.getProcess().toLowerCase().equals("true");
                case MONITORING -> group.getMonitoring().toLowerCase().equals("true");
                case POWER -> group.getPower().toLowerCase().equals("true");
                case NONE -> true; // if none are needed.
                default -> false; // In-case more are added in a future update.
            };
        }
    }

}
