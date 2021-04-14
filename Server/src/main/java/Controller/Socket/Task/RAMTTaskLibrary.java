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
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.json.JSONArray;
import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static Model.General.OSType.*;

/**
 * This class holds all the methods needed to perform the tasks that this application functionally needs. It handles
 * authorisation on its own and will return errors codes when there is a problem. If you wish to let the user known of a
 * problem or why something failed, please refer to these error codes:
 *
 * A (error) code referring to the success of the operation. The codes are as follows:
 * - 0 - Success without issue.
 * - 1 - Generic error where the request failed normally i.e. no results found or something *without* exception/fault.
 * - 2 - Parameters do not meet the require constraints of the database column, field or value.
 * - 3 - Parameter contained semantically immutable field such as root/default user in a delete query.
 * - 4 - Parameters required are invalid. This could be because of a null//incorrect value in request parameters.
 * - 10 - Username not found.
 * - 11 - Username found but incorrect password.
 * - 12 - User details verified but account is suspended
 * - 19 - User details verified but permissions are not satisfied (Unauthorised).
 * - 20 - An SQL exception was thrown that wasn't handled (correctly).
 * - 21 - Duplicate SQL error. When a value given would of violated a unique column for example.
 * - 31 - Process task related error, process restart attempted, killed but couldn't start again.
 * - 44 - Data given couldn't be found within the request i.e. row not found when updating a line in the database.
 * - 97 - Server wasn't launched with permissions (i.e. sudo, as admin...).
 * - 98 - Server doesn't support this task.
 * - 99 - Catastrophic generic error. If this has returned, something has gone seriously wrong (i.e. unforeseen bugs).
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class RAMTTaskLibrary {
    private static PowerShell shell;
    private static FtpServer server;

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
     * @return A Response with further information in an (error) code referring to the success of the operation. For a
     * list of the error codes, please refer to the classes JavaDoc. If the response is successful their will be userdata
     * attached. Otherwise UserData will be null when the response is returned.
     */
    public static TaskResponse<UserData> login(TaskRequest<Void> request) {
        try {
            if (request.getUser() == null) {                                                         // Parameter null?
                return new TaskResponse<>(request, Response.FAILEDAUTHENTICATION, 4);

            } else if (DBManager.getUser(request.getUser().getUsername()) == null) {                 // User(name) doesn't exist?
                return new TaskResponse<>(request, Response.FAILEDAUTHENTICATION, 10);

            } else if (DBManager.verifyPassword(request.getUser().getUsername(), request.getUser().getPassword())) { // Right Password?
                if (request.getUser().isSuspended()) {  // Suspended
                    return new TaskResponse<>(request, Response.FAILEDAUTHENTICATION, 12);

                } else {                                // User Checks out!
                    UserData loggedInUser = DBManager.getUser(request.getUser().getUsername());
                    loggedInUser.setObjGroup(DBManager.getGroup(loggedInUser.getGroup())); // adding UserGroup

                    // Get macAddress for user connected port.
                    loggedInUser.setMacAddress(getMacAddress(request.getUser().getHost()));

                    return new TaskResponse<>(request, Response.SUCCESS, 0, loggedInUser);

                }
            }

            return new TaskResponse<>(request, Response.FAILEDAUTHENTICATION, 11);
        } catch (NoSuchAlgorithmException | SQLException | InvalidKeySpecException e) {
            e.printStackTrace();
            return new TaskResponse<>(request, Response.INTERRUPTED, 20);
        }
    }

    /**
     * Kills the process given to it by PID.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<Void> killProcess(TaskRequest<Integer> request) {
        TaskResponse auth = authorise(request, AppPermission.PROCESS);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            switch (getOS()) {
                case WINDOWS_PS -> {
                    PowerShellResponse response = shell.executeCommand("Stop-Process -Id " + request.getParameter() + " -PassThru -Force");
                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);
                }
                case WINDOWS -> {
                    Process winCMD = new ProcessBuilder("cmd.exe", "-c", "taskkill /PID" + request.getParameter() + "/F /T").start();
                    return (winCMD.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);
                }
                case LINUX -> {
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "kill -9 " + request.getParameter()).start();
                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);
                }
                case MAC -> {
                    Process macCMD = new ProcessBuilder("/bin/zsh", "-c", "kill -9 " + request.getParameter()).start();
                    return macCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new TaskResponse<>(request, Response.SUCCESS, 99);
        }
        return new TaskResponse<>(request, Response.SUCCESS, 99);
    }

    /**
     * Kills and attempts to start again with for the process given by PID.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<Void> restartProcess(TaskRequest<Integer> request, boolean...test) {
        TaskResponse auth = authorise(request, AppPermission.PROCESS);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            switch (getOS()) {
                case WINDOWS_PS -> {
                    // Grab process information
                    PowerShellResponse response = shell.executeCommand("Get-WmiObject Win32_Process -Filter \"ProcessId='"
                            + request.getParameter() + "'\" | Select-Object CommandLine | ft -HideTableHeaders | " +
                            "Format-Table -AutoSize | Out-String -Width 10000");

                    // Parse start and end of output
                    String reMsg = response.getCommandOutput();
                    int pathEnd = reMsg.indexOf("\"",reMsg.indexOf("\"") + 1);

                    String path;
                    try { path = reMsg.substring(3, pathEnd); } catch (StringIndexOutOfBoundsException e) { path = reMsg; }
                    String args;
                    try { args = reMsg.substring(pathEnd + 2); } catch (StringIndexOutOfBoundsException e) { args = ""; }

                    System.out.println("Restart Path: " + path); // Just path
                    System.out.println("Restart Args: " + args); // Just Args

                    // Can we kill to restart it?
                    if (killProcess(request).getResponseCode() == 0) {
                        //Process killed, lets start it.
                        PowerShellResponse responseStart;
                        powershellShortTimeout();

                        if (args.isEmpty()) {
                            responseStart = shell.executeCommand("Start-Process -FilePath \"" + path + "\" -PassThru");
                        } else {
                            responseStart = shell.executeCommand("Start-Process -FilePath \"" + path + "\" -ArgumentList \"" + args + "\" -PassThru");
                        }

                        powershellDefaultTimeout();

                        if (responseStart.isError()) {
                            Process winCMD = new ProcessBuilder( path.split(" ")[2]).start();

                            if (winCMD.isAlive()) {
                                if (test.length > 0) {
                                    winCMD.destroyForcibly(); // kill for test.
                                }
                                return new TaskResponse<>(request, Response.SUCCESS, 0);
                            } else {
                                return new TaskResponse<>(request, Response.FAILED, 31);
                            }
                        }

                        // Otherwise no errors, then pass success unless timeout.
                        return (responseStart.isTimeout()) ?
                                new TaskResponse<>(request, Response.FAILED, 31) :
                                new TaskResponse<>(request, Response.SUCCESS, 0);

                    } else {
                        // Couldn't kill.
                        return new TaskResponse<>(request, Response.FAILED, 1);
                    }
                }
                case WINDOWS -> {
                    Process winCMD = new ProcessBuilder("cmd.exe", "-c", "taskkill /PID" + request.getParameter() + "/F /T").start();
                    return (winCMD.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);
                }
                // Linux and Mac depend on a specific signal. Application support is therefore needed.
                case LINUX -> {
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "kill -19 " + request.getParameter()).start();
                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);
                }
                case MAC -> {
                    Process macCMD = new ProcessBuilder("/bin/zsh", "-c", "kill -19 " + request.getParameter()).start();
                    return macCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new TaskResponse<>(request, Response.SUCCESS, 99);
        }
        return new TaskResponse<>(request, Response.SUCCESS, 99);
    }

    /**
     * Gets a JSON array of all the processes from the server.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<String> fetchProcesses(TaskRequest<Void> request) {
        TaskResponse auth = authorise(request, AppPermission.PROCESS);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            switch (getOS()) {
                case WINDOWS_PS:
                    String script = "controller/windows/ps/AllProcessesToJson.ps1";
                    BufferedReader srcReader = new BufferedReader(
                            new InputStreamReader(Objects.requireNonNull(
                                    ClassLoader.getSystemClassLoader().getResourceAsStream(script))));

                    PowerShellResponse response = shell.executeScript(srcReader); // Resource Hog. I've optimised the
                                                                                  // script plenty already

                    return new TaskResponse<>(request, Response.SUCCESS, 0, response.getCommandOutput());
                case WINDOWS:
                    ProcessBuilder winCMD = new ProcessBuilder();
                    winCMD.command("cmd.exe", "/c", "tasklist /V /fo CSV");

                    Process process = winCMD.start();
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }

                    return (process.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case LINUX:
                    Process linuxCMD = new ProcessBuilder("/bin/bash",  "-c", scriptUnixAllProcesses()).start();

                    BufferedReader linuxReader = new BufferedReader(new InputStreamReader(linuxCMD.getInputStream()));
                    StringBuilder linuxResponse = new StringBuilder();
                    String linuxBuffer;
                    while ((linuxBuffer = linuxReader.readLine()) != null) { linuxResponse.append(linuxBuffer); }

                    return new TaskResponse<>(request, Response.SUCCESS, 0, linuxResponse.toString());
                case MAC:
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", scriptUnixAllProcesses()).start();

                    BufferedReader macReader = new BufferedReader(new InputStreamReader(macCMD.getInputStream()));
                    StringBuilder macResponse = new StringBuilder();
                    String strBuffer;
                    while ((strBuffer = macReader.readLine()) != null) { macResponse.append(strBuffer); }

                    return new TaskResponse<>(request, Response.SUCCESS, 0, macResponse.toString());
                default: //OTHER
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * Shuts down the host system.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<Void> shutdown(TaskRequest<Void> request) {
        TaskResponse auth = authorise(request, AppPermission.POWER);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            switch (getOS()) {
                case WINDOWS_PS:
                    PowerShellResponse response = shell.executeCommand("Stop-Computer -ComputerName localhost");

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    Process process = new ProcessBuilder("cmd.exe", "/c", "shutdown /s").start();

                    return (process.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case LINUX:
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "poweroff" + request.getParameter()).start();

                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", "osascript -e 'tell app \"System Events\" to shut down'").start();

                    return macCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                default: //OTHER
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * Restarts the host system.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<Void> restart(TaskRequest<Void> request) {
        TaskResponse auth = authorise(request, AppPermission.POWER);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            switch (getOS()) {
                case WINDOWS_PS:
                    PowerShellResponse response = shell.executeCommand("Restart-Computer -ComputerName localhost");

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    Process process = new ProcessBuilder("cmd.exe", "/c", "shutdown /r").start();

                    return (process.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case LINUX:
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "reboot").start();

                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", "osascript -e 'tell app \"System Events\" to restart'").start();

                    return macCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                default:
                    return new TaskResponse<>(request, Response.FAILED, 98); // Unsupported
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new TaskResponse<>(request, Response.FAILED, 99);
        }
    }

    /**
     * Sleeps the host system.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<Void> sleep(TaskRequest<Void> request) {
        TaskResponse auth = authorise(request, AppPermission.POWER);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            switch (getOS()) {
                case WINDOWS_PS:
                    String script = "controller/windows/ps/SleepSystem.ps1";
                    BufferedReader srcReader = new BufferedReader(
                            new InputStreamReader(Objects.requireNonNull(
                                    ClassLoader.getSystemClassLoader().getResourceAsStream(script))));

                    PowerShellResponse response = shell.executeScript(srcReader);

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case LINUX:
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "systemctl suspend").start();

                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", "osascript -e 'tell app \"System Events\" to sleep'").start();

                    return macCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                default: // Can't do this without specific environment in windows CMD so windows ends here.
                    return new TaskResponse<>(request, Response.FAILED, 98); // Unsupported
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new TaskResponse<>(request, Response.FAILED, 99);
        }
    }

    /**
     * Starts and crafts the embedded FTP. The user details are recreated using server settings. This is pure java code
     * and has not platform specific requirements.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<Void> startFTP(TaskRequest<Void> request) {
        TaskResponse auth = authorise(request, AppPermission.GENERAL);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        if (server != null) {
            System.out.println("FTP already started.");
        } else {
            System.out.println("server is null.");
            FtpServerFactory serverFactory = new FtpServerFactory();
            ListenerFactory factory = new ListenerFactory();

            // Setting listener port.
            try {
                factory.setPort(Integer.parseInt(DBManager.getSetting("FTP Port")));
            } catch (SQLException e) {
                return new TaskResponse<>(request, Response.FAILEDSTART, 99);
            }
            serverFactory.addListener("default", factory.createListener());

            // User Management
            PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();

            File ftpPropFile = new File("data/ftp.properties");
            if (!ftpPropFile.isFile()) {
                try {Files.createFile(ftpPropFile.toPath());} catch (IOException e) {e.printStackTrace();}
            }

            userManagerFactory.setFile(ftpPropFile);
            userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor());
            UserManager um = userManagerFactory.createUserManager();

            // Adding root user
            BaseUser rootUser = new BaseUser();

            try {
                rootUser.setName(DBManager.getSetting("FTP Username"));
                rootUser.setPassword(DBManager.getSetting("FTP Password"));
            } catch (SQLException e) {
                rootUser.setName("RAMTUser"); // Default fallbacks.
                rootUser.setPassword("$%^DFG543*z");
            }

            rootUser.setEnabled(true);

            List<Authority> rootAuthorities = new ArrayList<>();
            rootAuthorities.add(new WritePermission());
            rootAuthorities.add(new ConcurrentLoginPermission(0, 0));
            rootAuthorities.add(new TransferRatePermission(0, 0));
            rootUser.setAuthorities(rootAuthorities);

            rootUser.setMaxIdleTime(120);
            rootUser.setHomeDirectory("/");

            boolean guestEnabled;
            try {
                guestEnabled = Boolean.parseBoolean(DBManager.getSetting("FTP Guest Enabled"));
            } catch (SQLException e) {
                e.printStackTrace();
                guestEnabled = false;
            }

            if (guestEnabled) {
                // Adding Guest user.

                BaseUser guestUser = new BaseUser();

                try {
                    guestUser.setName(DBManager.getSetting("FTP Guest Username"));
                    guestUser.setPassword(DBManager.getSetting("FTP Guest Password"));
                } catch (SQLException e) {
                    guestUser.setName("RAMTGuestUser"); // Default fallbacks.
                    guestUser.setPassword("$%^DFG543*z");
                }

                guestUser.setEnabled(true);

                guestUser.setMaxIdleTime(60);
                guestUser.setHomeDirectory(DBManager.dbPath.getAbsolutePath());
                try {
                    um.save(guestUser);
                } catch (FtpException e) {
                    e.printStackTrace();
                    return new TaskResponse<>(request, Response.FAILED, 99);
                }
            }
            // Server save users and start.
            try {
                um.save(rootUser);
                serverFactory.setUserManager(um);
                // Server creation.
                server = serverFactory.createServer();
                server.start();
                System.out.println("FTP Server started!");
            } catch (FtpException e) {
                e.printStackTrace();
                return new TaskResponse<>(request, Response.FAILED, 99);
            }
        }

        return new TaskResponse<>(request, Response.SUCCESS, 0);
    }

    /**
     * Stops the FTP.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<Void> stopFTP(TaskRequest<Void> request) {
        TaskResponse auth = authorise(request, AppPermission.GENERAL);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        if (server != null){
            server.stop();
            server = null;
            System.out.println("FTP Server stopped!");
        }

        return new TaskResponse<>(request, Response.SUCCESS, 0);
    }

    /**
     * Restarts the FTP by using stop and start FTP methods in this class if authorised as usual.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<Void> restartFTP(TaskRequest<Void> request) {
        TaskResponse auth = authorise(request, AppPermission.GENERAL);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        stopFTP(request);
        return startFTP(request);
    }

    /**
     * Will attempt to clean the disks based on the int given
     * 0 for full system disk clean,
     * 1 for extra disks clean, (Unsupported on base CMD for windows).
     * 2 for all disks clean,
     * 3 for bin /recycle empty/clean.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<Integer> cleanDisk(TaskRequest<Integer> request) {
        TaskResponse auth = authorise(request, AppPermission.GENERAL);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            switch (getOS()) {
                case WINDOWS_PS:
                    return switch (request.getParameter()) {

                        // System Drive Clean
                        case 0 -> shell.executeCommand("Start-Process -Wait \"$env:SystemRoot\\System32\\cleanmgr.exe\"" +
                                " -ArgumentList \"/AUTOCLEAN /d C\"; Write-Host 0;").isError() ?
                                new TaskResponse<>(request, Response.FAILED, 1, -1) :
                                new TaskResponse<>(request, Response.SUCCESS, 0, -1);

                        // Extra or All Drive(s) Clean
                        case 1,2 -> {
                            // Pre Main PowerShell Setup and Parameter Handling
                            System.out.println("Getting Drives...");
                            JSONArray json = new JSONArray(shell.executeCommand("Get-PSDrive | select name | ConvertTo-Json").getCommandOutput());
                            boolean excludeSystem = request.getParameter() == 1;
                            int errorCount = 0;

                            // Builder PowerShell Char Array
                            System.out.println("Building Char Array...");
                            ArrayList<Character> charArray = new ArrayList<>();

                            for (int i = 0; i < json.length(); i++) {
                                String current = (String) json.getJSONObject(i).get("Name");
                                if (current.length() == 1 && !(excludeSystem && current.equals("C"))) {
                                    charArray.add(current.charAt(0));
                                }
                            }

                            // Save CharArray to PS Session Then Process Main PowerShell Script.
                            powershellLongTimeout();
                            System.out.println("Main Shell Execution... Drives Detected " + charArray);
                            for (char c :charArray) {
                                errorCount += PowerShell.executeSingleCommand("cleanmgr /AUTOCLEAN /d " + c +
                                        "; Write-Host").isError() ? 1 : 0; // Write-Host to avoid False-Positive errors
                            }

                            // Finish up and return result.
                            powershellDefaultTimeout();
                            System.out.println("Drive Cleaner PS: Error Count " + errorCount);
                            yield new TaskResponse<>(request,
                                    errorCount > 0 ? Response.FAILED : Response.SUCCESS,
                                    errorCount > 0 ? 1 : 0,
                                    errorCount);
                        }

                        // Recycle bin clean
                        default -> shell.executeCommand("Clear-RecycleBin -Force").isError() ?
                                new TaskResponse<>(request, Response.FAILED, 1, -1) :
                                new TaskResponse<>(request, Response.SUCCESS, 0, -1);
                    };
                case WINDOWS:
                    String cmdScript =  switch (request.getParameter()) {
                        case 0 -> "cleanmgr.exe /AUTOCLEAN /d C";
                        case 2 -> "cleanmgr.exe /AUTOCLEAN";
                        case 3 ->  "rd /s /q %systemdrive%\\$Recycle.bin";
                        default -> "";
                    };

                    Process process;
                    if (!cmdScript.isEmpty()) {
                        process = new ProcessBuilder("cmd.exe", "/c", cmdScript).start();
                    } else {
                        return new TaskResponse<>(request, Response.FAILED, 98);
                    }

                    return (process.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case LINUX:
                    String linuxScript =  switch (request.getParameter()) {
                        case 0,1,2 -> "apt-get autoclean -y";
                        case 3 -> "rm -rf ~/.local/share/Trash/*";
                        default -> "";
                    };

                    Process linuxCMD;
                    if (!linuxScript.isEmpty()) {
                        linuxCMD = new ProcessBuilder("/bin/bash", "-c", "apt-get clean").start();
                    } else {
                        return new TaskResponse<>(request, Response.FAILED, 98);
                    }

                    System.out.println(linuxCMD.waitFor());
                    return (linuxCMD.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    String macScript =  switch (request.getParameter()) {
                        case 0,1,2 -> "cd /private/var/tmp/; rm -rf TM*";
                        case 3 -> "cd ~/Library/Caches/; rm -rf ~/Library/Caches/*";
                        default -> "";
                    };

                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", macScript).start();

                    return macCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                default:
                    return new TaskResponse<>(request, Response.FAILED, 98); // Unsupported
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new TaskResponse<>(request, Response.FAILED, 99);
        }
    }

    /**
     * Enables any wifi adapters that can be found.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<Void> enableWifi(TaskRequest<Integer> request) {
        TaskResponse auth = authorise(request, AppPermission.GENERAL);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            switch (getOS()) {
                case WINDOWS_PS:
                    if (request.getParameter() == 1) { disableWifi(request); } // Re-enable

                    PowerShellResponse response = shell.executeCommand("(Get-NetAdapter)" +
                            ".where({$psitem.name -like '*WiFi*'}) | " +
                            "Enable-NetAdapter -Confirm:$false -PassThru; Write-Host 0;");

                    System.out.println(response.getCommandOutput());

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    if (request.getParameter() == 1) { disableWifi(request); } // Re-enable
                    Process process1 = new ProcessBuilder("cmd.exe", "/c", "netsh interface set interface \"Wireless Network Connection\" Enable").start();
                    Process process2 = new ProcessBuilder("cmd.exe", "/c", "netsh interface set interface \"WiFi\" Enable").start();

                    return (process1.waitFor() != 0 || process2.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case LINUX:
                    if (request.getParameter() == 1) { disableWifi(request); } // Re-enable
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "nmcli radio wifi on").start();

                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    if (request.getParameter() == 1) { disableWifi(request); } // Re-enable
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", "networksetup -setairportpower en0 on").start();

                    return macCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                default:
                    return new TaskResponse<>(request, Response.FAILED, 98); // Unsupported
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new TaskResponse<>(request, Response.FAILED, 99);
        }
    }

    /**
     * Disables any WiFi adapters that can be found.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<Void> disableWifi(TaskRequest<Integer> request) {
        TaskResponse auth = authorise(request, AppPermission.GENERAL);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            switch (getOS()) {
                case WINDOWS_PS:
                    PowerShellResponse response = shell.executeCommand("(Get-NetAdapter)" +
                            ".where({$psitem.name -like '*WiFi*'}) | " +
                            "Disable-NetAdapter -Confirm:$false -PassThru; Write-Host 0;");

                    System.out.println(response.getCommandOutput());

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    Process process1 = new ProcessBuilder("cmd.exe", "/c", "netsh interface set interface \"Wireless Network Connection\" Disable").start();
                    Process process2 = new ProcessBuilder("cmd.exe", "/c", "netsh interface set interface \"WiFi\" Disable").start();

                    return (process1.waitFor() != 0 || process2.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case LINUX:
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "nmcli radio wifi off").start();

                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", "networksetup -setairportpower en0 off").start();

                    return macCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                default:
                    return new TaskResponse<>(request, Response.FAILED, 98); // Unsupported
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new TaskResponse<>(request, Response.FAILED, 99);
        }
    }

    /**
     * Enables any bluetooth adapters that can be found
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<Void> enableBluetooth(TaskRequest<Integer> request) {
        TaskResponse auth = authorise(request, AppPermission.GENERAL);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            switch (getOS()) {
                case WINDOWS_PS:
                    if (request.getParameter() == 1) { disableBluetooth(request); } // Re-enable

                    PowerShellResponse response = shell.executeCommand("(Get-NetAdapter)" +
                            ".where({$psitem.name -like '*Bluetooth*'}) | " +
                            "Enable-NetAdapter -Confirm:$false -PassThru; Write-Host 0;");

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    if (request.getParameter() == 1) { disableBluetooth(request); } // Re-enable
                    Process process1 = new ProcessBuilder("cmd.exe", "/c", "netsh interface set interface \"Bluetooth Network Connection\" Enable").start();
                    Process process2 = new ProcessBuilder("cmd.exe", "/c", "netsh interface set interface \"Bluetooth\" Enable").start();

                    return (process1.waitFor() != 0 || process2.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);


                case LINUX:
                    if (request.getParameter() == 1) { disableBluetooth(request); } // Re-enable
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "rfkill unblock bluetooth").start();

                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    if (request.getParameter() == 1) { disableBluetooth(request); } // Re-enable
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", scriptMacOSBluetooth(true)).start();

                    return macCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                default:
                    return new TaskResponse<>(request, Response.FAILED, 98); // Unsupported
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new TaskResponse<>(request, Response.FAILED, 99);
        }
    }

    /**
     * Disables any bluetooth adapters that can be found
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<Void> disableBluetooth(TaskRequest<Integer> request) {
        TaskResponse auth = authorise(request, AppPermission.GENERAL);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            switch (getOS()) {
                case WINDOWS_PS:
                    PowerShellResponse response = shell.executeCommand("(Get-NetAdapter)" +
                            ".where({$psitem.name -like '*Bluetooth*'}) | " +
                            "Disable-NetAdapter -Confirm:$false -PassThru; Write-Host 0;");

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    Process process1 = new ProcessBuilder("cmd.exe", "/c", "netsh interface set interface \"Bluetooth Network Connection\" Disable").start();
                    Process process2 = new ProcessBuilder("cmd.exe", "/c", "netsh interface set interface \"Bluetooth\" Disable").start();

                    return (process1.waitFor() != 0 || process2.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case LINUX:
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "rfkill block bluetooth").start();

                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", scriptMacOSBluetooth(false)).start();

                    return macCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                default:
                    return new TaskResponse<>(request, Response.FAILED, 98); // Unsupported
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new TaskResponse<>(request, Response.FAILED, 99);
        }
    }

    /**
     * Gets the settings desired in the parameters of the request if they exist.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<String> getSetting(TaskRequest<String> request) {
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            return new TaskResponse<>(request, Response.SUCCESS, 0, DBManager.getSetting(request.getParameter()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Gets all the settings as a hashmap.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<HashMap<String, String>> getSettings(TaskRequest<Void> request) {
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            System.out.println("Not authed");
            return auth;
        }
        System.out.println("Authed");

        try {
            return new TaskResponse<>(request, Response.SUCCESS, 0, DBManager.getSettings());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Sets the settings defined in the parameters of this request.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<Void> setSetting(TaskRequest<String[]> request) {
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            return new TaskResponse<>(request, Response.SUCCESS, DBManager.updateSetting(request.getParameter()[0], request.getParameter()[1])); // Should make sure DB and RAMT codes are same.
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Sets the settings defined in the key and value of the hash map in the request parameter.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<String> setSettings(TaskRequest<HashMap<String, String>> request) {
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            return new TaskResponse<>(request, Response.SUCCESS, DBManager.updateSettings(request.getParameter())); // Should make sure DB and RAMT codes are same.
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Gets the user in the parameters of the request.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<UserData> getUser(TaskRequest<String> request) {
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            return new TaskResponse<>(request, Response.SUCCESS, 0, DBManager.getUser(request.getParameter())); // Should make sure DB and RAMT codes are same.
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Gets all the users in an ArrayList.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<ArrayList<UserData>> getUsers(TaskRequest<Void> request) {
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            return new TaskResponse<>(request, Response.SUCCESS, 0,DBManager.getAllUsers()); // Should make sure DB and RAMT codes are same.
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Deletes the user in the request parameter by username.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<?> deleteUser(TaskRequest<String> request) {
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            return new TaskResponse<>(request, Response.SUCCESS, DBManager.deleteUser(request.getParameter())); // Should make sure DB and RAMT codes are same.
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Deletes all the users in a given group name defined in the requests parameters.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<?> deleteUsers(TaskRequest<String> request) {
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            return new TaskResponse<>(request, Response.SUCCESS, DBManager.deleteGroupUsers(request.getParameter())); // Should make sure DB and RAMT codes are same.
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Deletes a group which will cause any remaining users to be moved to the default group.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<?> deleteGroup(TaskRequest<String> request) {
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            return new TaskResponse<>(request, Response.SUCCESS, DBManager.deleteGroup(request.getParameter())); // Should make sure DB and RAMT codes are same.
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Updates the user with details defined in the request parameters.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<?> updateUser(TaskRequest<String[]> request) { //0,1,2 (what to change, what username, updated value)
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            return switch (request.getParameter()[0].toLowerCase()) {
                // Should make sure DB and RAMT codes are same.
                case "username" -> new TaskResponse<>(request, Response.SUCCESS, DBManager.updateUsername(request.getParameter()[1], request.getParameter()[2]));
                // Should make sure DB and RAMT codes are same.
                case "password" -> new TaskResponse<>(request, Response.SUCCESS, DBManager.updatePassword(request.getParameter()[1], request.getParameter()[2]));
                // Should make sure DB and RAMT codes are same.
                case "group" -> new TaskResponse<>(request, Response.SUCCESS, DBManager.changeUserGroup(request.getParameter()[1], request.getParameter()[2]));
                // Should make sure DB and RAMT codes are same.
                default -> new TaskResponse<>(request, Response.FAILED, 4);
            };
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Addes the new user (UserData) to the database in the request parameters.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<Void> addUser(TaskRequest<UserData> request) {
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            return new TaskResponse<>(request, Response.SUCCESS, DBManager.addUser(request.getParameter())); // Should make sure DB and RAMT codes are same.
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Creates the given group with the object defined in the request parameters
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<Void> addGroup(TaskRequest<UserGroup> request) {
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            return new TaskResponse<>(request, Response.SUCCESS, DBManager.addGroup(request.getParameter())); // Should make sure DB and RAMT codes are same.
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Gets the group with the matching group name.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<UserGroup> getGroup(TaskRequest<String> request) {
        if (login(((TaskRequest) request)).getResponse() == Response.SUCCESS &&
                request.getUser().getGroup().equals(request.getParameter())) {
            System.out.println("Bypassing get group full auth to allow user to grab their own group.");
        } else {
            TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
            if (auth.getResponse() != Response.SUCCESS) {
                return auth;
            }
        }

        try {
            return new TaskResponse<>(request, Response.SUCCESS, 0, DBManager.getGroup(request.getParameter())); // Should make sure DB and RAMT codes are same.
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Gets an arraylist of all groups.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<ArrayList<UserGroup>> getGroups(TaskRequest<Void> request) {
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            return new TaskResponse<>(request, Response.SUCCESS, 0, DBManager.getAllGroups()); // Should make sure DB and RAMT codes are same.
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Suspends the given user.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<?> suspendUser(TaskRequest<String> request) {
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            return new TaskResponse<>(request, Response.SUCCESS, DBManager.suspendUser(request.getParameter())); // Should make sure DB and RAMT codes are same.
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Un-suspends the given user.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<?> unsuspendUser(TaskRequest<String> request) {
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            return new TaskResponse<>(request, Response.SUCCESS, DBManager.unsuspendUser(request.getParameter())); // Should make sure DB and RAMT codes are same.
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Suspends all the users in a given group.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<?> suspendUsers(TaskRequest<String> request) {
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            return new TaskResponse<>(request, Response.SUCCESS, DBManager.suspendGroupUsers(request.getParameter())); // Should make sure DB and RAMT codes are same.
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Updates a group with the given information.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<?> updateGroup(TaskRequest<String[]> request) { //0,1,2+ (what to change, what group name, updated value(s))
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        try {
            return switch (request.getParameter()[0].toLowerCase()) {
                // Should make sure DB and RAMT codes are same.
                case "name" -> new TaskResponse<>(request, Response.SUCCESS, DBManager.updateGroupName(request.getParameter()[1], request.getParameter()[2]));
                // Should make sure DB and RAMT codes are same.
                case "permissions" -> new TaskResponse<>(request, Response.SUCCESS, DBManager.updatePermissions(request.getParameter()[1],
                        Boolean.parseBoolean(request.getParameter()[2]),
                        Boolean.parseBoolean(request.getParameter()[3]),
                        Boolean.parseBoolean(request.getParameter()[4]),
                        Boolean.parseBoolean(request.getParameter()[5]),
                        Boolean.parseBoolean(request.getParameter()[6])));
                // Should make sure DB and RAMT codes are same.
                case "migrate" -> new TaskResponse<>(request, Response.SUCCESS, DBManager.migrateGroup(request.getParameter()[1], request.getParameter()[2]));
                // Should make sure DB and RAMT codes are same.
                default -> new TaskResponse<>(request, Response.FAILED, 4);
            };
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TaskResponse<>(request, Response.FAILED, 99);
    }

    /**
     * Marks the server for wipe and closes the application if successful.
     * @param request The request sent from the client to perform this action and perform authorisation on.
     * @return The response of the procedure containing its state and any parameters expected on success.
     */
    public static TaskResponse<Void> factoryReset(TaskRequest<Void> request, Boolean...test) {
        TaskResponse auth = authorise(request, AppPermission.ADMINISTRATOR);
        if (auth.getResponse() != Response.SUCCESS) {
            return auth;
        }

        stopFTP(request); // May cause errors if not stopped.

        if (test.length == 0) {
            Executors.newScheduledThreadPool(1).schedule(() -> System.exit(0), 2, TimeUnit.SECONDS);
        }


        boolean isMarked;

        try {
            isMarked = new File(DBManager.dbPath.getAbsolutePath() + ".wipe").createNewFile();
        } catch (IOException e) {
            isMarked = false;
            e.printStackTrace();
        }

        return isMarked ?
                new TaskResponse<>(request, Response.SUCCESS, 0) :
                new TaskResponse<>(request, Response.FAILED, 1);
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
        if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
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
     * error. This method accepts uses raw types because it doesn't touch anything related to generics. It should be
     * safe to cast back to any expected requests.
     * @param request The Response containing UserData object referring to the user.
     * @param permission The task's permissions enum representing the requested task.
     * @return A (error) code referring to the success of the operation. For a list of the error codes, please refer to
     * the classes JavaDoc.
     */
    private static TaskResponse authorise(TaskRequest request, AppPermission permission) {
        TaskResponse loginResponse = login(request);

        if (loginResponse.getResponse() != Response.SUCCESS) {
            return loginResponse;
        }

        UserGroup group;
        try {
            group = DBManager.getGroup(request.getUser().getGroup());
        } catch (SQLException e) {
            System.out.println("Auth query failed, failing auth as fallback.");
            e.printStackTrace();
            return new TaskResponse(request, Response.FAILEDAUTHENTICATION, 20);
        }

        if (group.getAdmin().equalsIgnoreCase("true")) {
            return new TaskResponse<>(request, Response.SUCCESS, 0); // Because admins have access to all.
        } else {
            boolean authed = switch (permission) {
                case GENERAL -> group.getGeneral().equalsIgnoreCase("true");
                case PROCESS -> group.getProcess().equalsIgnoreCase("true");
                case MONITORING -> group.getMonitoring().equalsIgnoreCase("true");
                case POWER -> group.getPower().equalsIgnoreCase("true");
                case NONE -> true; // if none are needed.
                default -> false; // In-case more are added in a future update.
            };

            if (authed) {
                return new TaskResponse<>(request, Response.SUCCESS, 0);
            } else {
                return new TaskResponse<>(request, Response.FAILED, 19);
            }
        }
    }

    private static byte[] getMacAddress(String ip) {
        try {
            InetAddress localHost = InetAddress.getByName(ip);
            NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
            return ni.getHardwareAddress();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        return null;
    }


    /*
     * All Scripts as variables/methods go here to prevent clutter of the top of this class.
     * This is to lower
     */
    private static String scriptUnixAllProcesses() {
        return  "ps -Ao pid,%mem,%cpu,stat,ucomm | awk 'NR>1'| awk '\n" +
                "BEGIN { ORS = \"\"; print \" [ \"}\n" +
                "{ printf \"%s{\\\"IDProcess\\\": \\\"%s\\\", \\\"WorkingSetPrivate\\\": \\\"%s\\\", \\\"PercentProcessorTime\\\": \\\"%s\\\", \\\"Status\\\": \\\"%s\\\", \\\"Name\\\": \\\"%s %s %s %s\\\"}\",\n" +
                "      separator, $1, $2, $3, $4, $5, $6, $7, $8\n" +
                "  separator = \", \"\n" +
                "}\n" +
                "END { print \" ] \" }';";
    }

    private static String scriptMacOSBluetooth(boolean on) {
        if (on) {
            return "tell application \"System Events\" to tell process \"SystemUIServer\"\n" +
                    "set bt to (first menu bar item whose description is \"bluetooth\") of menu bar 1\n" +
                    "click bt\n" +
                    "tell (first menu item whose title is \"Turn Bluetooth On\") of menu of bt\n" +
                    "click\n" +
                    "click menu item \"Turn Bluetooth On\"\n" +
                    "end tell\n" +
                    "end tell\n" +
                    "end tell";
        } else {
            return "tell application \"System Events\" to tell process \"SystemUIServer\"\n" +
                    "set bt to (first menu bar item whose description is \"bluetooth\") of menu bar 1\n" +
                    "click bt\n" +
                    "tell (first menu item whose title is \"Turn Bluetooth Off\") of menu of bt\n" +
                    "click\n" +
                    "click menu item \"Turn Bluetooth Off\"\n" +
                    "end tell\n" +
                    "end tell\n" +
                    "end tell";
        }
    }

    /**
     * 5 Second timeout for the powershell.
     */
    private static void powershellShortTimeout() {
        Map<String, String> myConfig = new HashMap<>();
        myConfig.put("maxWait", "5000");
        shell.configuration(myConfig);
    }

    /**
     * 10 Second (default) timeout for the powershell.
     */
    private static void powershellDefaultTimeout() {
        Map<String, String> myConfig = new HashMap<>();
        myConfig.put("maxWait", "10000");
        shell.configuration(myConfig);
    }

    /**
     * 30 Second timeout for the powershell.
     */
    private static void powershellLongTimeout() {
        Map<String, String> myConfig = new HashMap<>();
        myConfig.put("maxWait", "30000");
        shell.configuration(myConfig);
    }
}
