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
import java.io.*;
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
 * - 4 - Parameters required are invalid. This could be because of a null value in request parameters.
 * - 10 - Username not found.
 * - 11 - Username found but incorrect password.
 * - 12 - User details verified but account is suspended
 * - 19 - User details verified but permissions are not satisfied (Unauthorised).
 * - 20 - An SQL exception was thrown that wasn't handled (correctly).
 * - 21 - Duplicate SQL error. When a value given would of violated a unique column for example.
 * - 31 - Process task related error, process restart attempted, killed but couldn't start again.
 * - 44 - Data given couldn't be found within the request i.e. row not found when updating a line in the database.
 * - 98 - Server doesn't support this task.
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
    public static TaskResponse<Void> login(TaskRequest<Void> request) {
        try {
            if (request.getUser() == null) {  return new TaskResponse<>(request, Response.FAILEDAUTHENTICATION, 4); }

            if (DBManager.getUser(request.getUser().getUsername()) == null) {
                return new TaskResponse<>(request, Response.FAILEDAUTHENTICATION, 10);
            }

            if (DBManager.verifyPassword(request.getUser().getUsername(), request.getUser().getPassword())) {
                return request.getUser().isSuspended() ?
                        new TaskResponse<>(request, Response.FAILEDAUTHENTICATION, 12) :
                        new TaskResponse<>(request, Response.SUCCESS, 0);
            }

            return new TaskResponse<>(request, Response.FAILEDAUTHENTICATION, 11);
        } catch (NoSuchAlgorithmException | SQLException | InvalidKeySpecException e) {
            e.printStackTrace();
            return new TaskResponse<>(request, Response.INTERRUPTED, 20);
        }
    }

    public static TaskResponse<Void> killProcess(TaskRequest<Integer> request) {
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

    public static TaskResponse<Void> restartProcess(TaskRequest<Integer> request) {
        try {
            switch (getOS()) {
                case WINDOWS_PS -> {
                    PowerShellResponse response = shell.executeCommand("Get-WmiObject Win32_Process -Filter \"ProcessId='"
                            + request.getParameter() + "'\" | Select-Object CommandLine | ft -HideTableHeaders | " +
                            "Format-Table -AutoSize | Out-String -Width 10000");

                    String reMsg = response.getCommandOutput();
                    int pathEnd = reMsg.indexOf("\"",reMsg.indexOf("\"") + 1);

                    String path = reMsg.substring(3, pathEnd); //Todo catch and handle java.lang.StringIndexOutOfBoundsException
                    String args;
                    try { args = reMsg.substring(pathEnd + 2); } catch (StringIndexOutOfBoundsException e) { args = ""; }

                    System.out.println(path); // Just path
                    System.out.println(args); // Just Args

                    //todo kill to process now and start it with the path and args

                    if (killProcess(request).getResponseCode() == 0) {
                        PowerShellResponse responseStart;
                        powershellShortTimeout();

                        if (args.isEmpty()) {
                            System.out.println("Starting a process without arguments");
                            responseStart = shell.executeCommand("Start-Process -FilePath \"" + path + "\" -PassThru");
                        } else {
                            System.out.println("Starting a process with arguments: " + args);
                            responseStart = shell.executeCommand("Start-Process -FilePath \"" + path + "\" -ArgumentList \"" + args + "\" -PassThru");
                        }

                        powershellDefaultTimeout();

                        return (responseStart.isError() || responseStart.isTimeout()) ?
                                new TaskResponse<>(request, Response.FAILED, 31) :
                                new TaskResponse<>(request, Response.SUCCESS, 0);
                    } else {
                        return new TaskResponse<>(request, Response.FAILED, 1);
                    }
                }
                case WINDOWS -> {
                    Process winCMD = new ProcessBuilder("cmd.exe", "-c", "taskkill /PID" + request.getParameter() + "/F /T").start();
                    return (winCMD.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);
                }
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

    public static TaskResponse<String> fetchProcesses(TaskRequest<Void> request) {
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

    public static TaskResponse<Void> shutdown(TaskRequest<Void> request) {
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

    private static TaskResponse<Void> restart(TaskRequest<Void> request) {
        try {
            switch (getOS()) {
                case WINDOWS_PS:
                    PowerShellResponse response = shell.executeCommand("Restart-Computer -ComputerName localhost");

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    Process process = new ProcessBuilder("cmd.exe", "/c", "shutdown /r").start();

                    // Get the output of the command.
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder winCMDOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) { winCMDOutput.append(line); }

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

    private static TaskResponse<Void> sleep(TaskRequest<Void> request) {
        try {
            switch (getOS()) {
                case WINDOWS_PS:
                    PowerShellResponse response = shell.executeCommand("");

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    Process process = new ProcessBuilder("cmd.exe", "/c", "").start();

                    // Get the output of the command.
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder winCMDOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) { winCMDOutput.append(line); }

                    return (process.waitFor() != 0) ?
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

                default:
                    return new TaskResponse<>(request, Response.FAILED, 98); // Unsupported
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new TaskResponse<>(request, Response.FAILED, 99);
        }
    }

    private static TaskResponse<String> getSettings(TaskRequest<String> request) {
        try {
            switch (getOS()) {
                case WINDOWS_PS:
                    PowerShellResponse response = shell.executeCommand("");

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    Process process = new ProcessBuilder("cmd.exe", "/c", "").start();

                    // Get the output of the command.
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder winCMDOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) { winCMDOutput.append(line); }

                    return (process.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);


                case LINUX:
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "").start();

                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", "").start();

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

    private static TaskResponse<Void> editSetting(TaskRequest<String[]> request) {
                try {
            switch (getOS()) {
                case WINDOWS_PS:
                    PowerShellResponse response = shell.executeCommand("");

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    Process process = new ProcessBuilder("cmd.exe", "/c", "").start();

                    // Get the output of the command.
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder winCMDOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) { winCMDOutput.append(line); }

                    return (process.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);


                case LINUX:
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "").start();

                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", "").start();

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

    private static TaskResponse<Void> startFTP(TaskRequest<Void> request) {
                try {
            switch (getOS()) {
                case WINDOWS_PS:
                    PowerShellResponse response = shell.executeCommand("");

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    Process process = new ProcessBuilder("cmd.exe", "/c", "").start();

                    // Get the output of the command.
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder winCMDOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) { winCMDOutput.append(line); }

                    return (process.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);


                case LINUX:
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "").start();

                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", "").start();

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

    private static TaskResponse<Void> stopFTP(TaskRequest<Void> request) {
                try {
            switch (getOS()) {
                case WINDOWS_PS:
                    PowerShellResponse response = shell.executeCommand("");

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    Process process = new ProcessBuilder("cmd.exe", "/c", "").start();

                    // Get the output of the command.
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder winCMDOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) { winCMDOutput.append(line); }

                    return (process.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);


                case LINUX:
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "").start();

                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", "").start();

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

    private static TaskResponse<Void> restartFTP(TaskRequest<Void> request) {
                try {
            switch (getOS()) {
                case WINDOWS_PS:
                    PowerShellResponse response = shell.executeCommand("");

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    Process process = new ProcessBuilder("cmd.exe", "/c", "").start();

                    // Get the output of the command.
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder winCMDOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) { winCMDOutput.append(line); }

                    return (process.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);


                case LINUX:
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "").start();

                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", "").start();

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

    private static TaskResponse<Void> cleanDisk(TaskRequest<Void> request) {
                try {
            switch (getOS()) {
                case WINDOWS_PS:
                    PowerShellResponse response = shell.executeCommand("");

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    Process process = new ProcessBuilder("cmd.exe", "/c", "").start();

                    // Get the output of the command.
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder winCMDOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) { winCMDOutput.append(line); }

                    return (process.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);


                case LINUX:
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "").start();

                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", "").start();

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

    private static TaskResponse<Void> enableWifi(TaskRequest<Void> request) {
                try {
            switch (getOS()) {
                case WINDOWS_PS:
                    PowerShellResponse response = shell.executeCommand("");

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    Process process = new ProcessBuilder("cmd.exe", "/c", "").start();

                    // Get the output of the command.
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder winCMDOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) { winCMDOutput.append(line); }

                    return (process.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);


                case LINUX:
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "").start();

                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", "").start();

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

    private static TaskResponse<Void> disableWifi(TaskRequest<Void> request) {
                try {
            switch (getOS()) {
                case WINDOWS_PS:
                    PowerShellResponse response = shell.executeCommand("");

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    Process process = new ProcessBuilder("cmd.exe", "/c", "").start();

                    // Get the output of the command.
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder winCMDOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) { winCMDOutput.append(line); }

                    return (process.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);


                case LINUX:
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "").start();

                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", "").start();

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

    private static TaskResponse<Void> enableBluetooth(TaskRequest<Void> request) {
                try {
            switch (getOS()) {
                case WINDOWS_PS:
                    PowerShellResponse response = shell.executeCommand("");

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    Process process = new ProcessBuilder("cmd.exe", "/c", "").start();

                    // Get the output of the command.
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder winCMDOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) { winCMDOutput.append(line); }

                    return (process.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);


                case LINUX:
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "").start();

                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", "").start();

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

    private static TaskResponse<Void> disableBluetooth(TaskRequest<Void> request) {
                try {
            switch (getOS()) {
                case WINDOWS_PS:
                    PowerShellResponse response = shell.executeCommand("");

                    return (response.isError() || response.isTimeout()) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case WINDOWS:
                    Process process = new ProcessBuilder("cmd.exe", "/c", "").start();

                    // Get the output of the command.
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder winCMDOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) { winCMDOutput.append(line); }

                    return (process.waitFor() != 0) ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);


                case LINUX:
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "").start();

                    return linuxCMD.waitFor() != 0 ?
                            new TaskResponse<>(request, Response.FAILED, 1) :
                            new TaskResponse<>(request, Response.SUCCESS, 0);

                case MAC:
                    Process macCMD = new ProcessBuilder("/bin/zsh",  "-c", "").start();

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
     * error.
     * @param user The UserData object refering to the user.
     * @param permission The task's permissions enum representing the requested task.
     * @return A (error) code referring to the success of the operation. For a list of the error codes, please refer to
     * the classes JavaDoc.
     */
    private static boolean authorise(UserData user, AppPermission permission) throws SQLException {
        UserGroup group = DBManager.getGroup(user.getGroup());

        if (group.getAdmin().equalsIgnoreCase("true")) {
            return true; // Because admins have access to all.
        } else {
            return switch (permission) {
                case GENERAL -> group.getGeneral().equalsIgnoreCase("true");
                case PROCESS -> group.getProcess().equalsIgnoreCase("true");
                case MONITORING -> group.getMonitoring().equalsIgnoreCase("true");
                case POWER -> group.getPower().equalsIgnoreCase("true");
                case NONE -> true; // if none are needed.
                default -> false; // In-case more are added in a future update.
            };
        }
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
