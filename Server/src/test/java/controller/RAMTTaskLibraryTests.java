package controller;

import Controller.CryptographyToolbox;
import Controller.Socket.Task.RAMTTaskLibrary;
import Model.General.OSType;
import Model.Task.Response;
import Model.Task.Task;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;
import Model.User.UserData;
import com.profesorfalken.jpowershell.PowerShellResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

import static Controller.Database.DBManager.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RAMTTaskLibraryTests {

    /**
     * Starts a dummy process (text editor) and gets the processes PID to kill it. The resulting response code is then
     * tested to see if it was successful. If the test is unsuccessful the working directory may be locked as a result.
     *
     * Is largely platform dependant (Tested function(s) rely heavily on OS specific code).
     */
    @Test
    @DisplayName("Kill Process (Platform Dependant)")
    void testKillProcess() {
        String username = DBManagerTests.getSetupUser()[0];
        String password = DBManagerTests.getSetupUser()[1];

        TaskRequest<Integer> request;
        UserData setupUser = new UserData(null, username, password, "Administrator");
        int pid = 0;

        try {
            switch (RAMTTaskLibrary.getOS()) {
                case WINDOWS, WINDOWS_PS -> {
                    Process winCMD = new ProcessBuilder("cmd.exe", "-c", "notepad").start();
                    pid = (int) winCMD.pid();
                    System.out.println(pid);
                }
                case LINUX -> {
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "nano").start();
                    pid = (int) linuxCMD.pid();
                }
                case MAC -> {
                    Process macCMD = new ProcessBuilder("/bin/zsh", "-c", "nano").start();
                    pid =  (int) macCMD.pid();
                }
            }
        } catch (IOException e) {
            fail("Test couldn't start a dummy text editor process to kill.");
        }

        assertNotEquals(0, pid, "Dummy Process ID failed to be obtained.");

        request = new TaskRequest<>(Task.KILLPROCESS, setupUser, pid);
        TaskResponse<Void> response = RAMTTaskLibrary.killProcess(request);

        assertEquals(0, response.getResponseCode(), "Task failed to kill dummy process.");
    }

    /**
     * Starts a dummy process (text editor) and gets the processes PID to kill it. The resulting response code is then
     * tested to see if it was successful. If the test is unsuccessful the working directory may be locked as a result.
     *
     * Is largely platform dependant (Tested function(s) rely heavily on OS specific code).
     */
    @Test
    @DisplayName("Restart Process (Platform Dependant)")
    void testRestartProcess() {
        String username = DBManagerTests.getSetupUser()[0];
        String password = DBManagerTests.getSetupUser()[1];

        TaskRequest<Integer> request;
        UserData setupUser = new UserData(null, username, password, "Administrator");
        int pid = 0;

        try {
            switch (RAMTTaskLibrary.getOS()) {
                case WINDOWS, WINDOWS_PS -> {
                    Process winCMD = new ProcessBuilder("cmd.exe", "-c", "notepad").start();
                    pid = (int) winCMD.pid();
                    System.out.println(pid);
                }
                case LINUX -> {
                    Process linuxCMD = new ProcessBuilder("/bin/bash", "-c", "nano").start();
                    pid = (int) linuxCMD.pid();
                }
                case MAC -> {
                    Process macCMD = new ProcessBuilder("/bin/zsh", "-c", "nano").start();
                    pid =  (int) macCMD.pid();
                }
            }
        } catch (IOException e) {
            fail("Test couldn't start a dummy text editor process to kill.");
        }

        assertNotEquals(0, pid, "Dummy Process ID failed to be obtained.");

        request = new TaskRequest<>(Task.RESTARTPROCESS, setupUser, pid);
        TaskResponse<Void> response = RAMTTaskLibrary.restartProcess(request, true);

        assertEquals(0, response.getResponseCode(), "Task failed to kill dummy process.");
    }

    /**
     * Asserts that the fetchProcess task is successful and that the resulting response data is parsable as JSONArray.
     *
     * Is largely platform dependant (Tested function(s) rely heavily on OS specific code).
     */
    @Test
    @DisplayName("Fetch Process (Platform Dependant)")
    void testFetchProcess() {
        String username = DBManagerTests.getSetupUser()[0];
        String password = DBManagerTests.getSetupUser()[1];
        UserData setupUser = new UserData(null, username, password, "Administrator");

        TaskRequest<Void> request = new TaskRequest<>(Task.FETCHPROCESSES, setupUser);
        TaskResponse<String> response = RAMTTaskLibrary.fetchProcesses(request);

        assertNotNull(response);
        assertNotNull(response.getResponseData());
        assertEquals(0, response.getResponseCode());

        try {
            assertFalse(new JSONArray(response.getResponseData()).isNull(0));
        } catch (JSONException e) {
            fail("Invalid JSON retrieved.");
        }
    }

    /**
     * Asserts that the FTP server can start.
     */
    @Test
    @DisplayName("Start FTP")
    @Order(1)
    void testStartFTP() {
        String username = DBManagerTests.getSetupUser()[0];
        String password = DBManagerTests.getSetupUser()[1];
        UserData setupUser = new UserData(null, username, password, "Administrator");

        TaskRequest<Void> request = new TaskRequest<>(Task.STARTFTP, setupUser);
        TaskResponse<Void> response = RAMTTaskLibrary.startFTP(request);

        assertNotNull(response);
        assertEquals(0, response.getResponseCode());
    }

    /**
     * Asserts that the FTP server can be stopped after starting. Must run after testStartFTP.
     */
    @Test
    @DisplayName("Stop FTP")
    @Order(2)
    void testStopFTP() {
        String username = DBManagerTests.getSetupUser()[0];
        String password = DBManagerTests.getSetupUser()[1];
        UserData setupUser = new UserData(null, username, password, "Administrator");

        TaskRequest<Void> request = new TaskRequest<>(Task.STOPFTP, setupUser);
        TaskResponse<Void> response = RAMTTaskLibrary.stopFTP(request);

        assertNotNull(response);
        assertEquals(0, response.getResponseCode());
    }

    /**
     * This test will ensure that the disk cleaning function can succeed when targeting all disks or if there is
     * any bluetooth devices.
     *
     * Is largely platform dependant (Tested function(s) rely heavily on OS specific code).
     */
    @Test
    @DisplayName("Clean Disk (Platform Dependant)")
    void testCleanDisk() {
        String username = DBManagerTests.getSetupUser()[0];
        String password = DBManagerTests.getSetupUser()[1];
        UserData setupUser = new UserData(null, username, password, "Administrator");

        TaskRequest<Integer> request = new TaskRequest<>(Task.CLEANDISK, setupUser, 0);
        TaskResponse<Integer> response = RAMTTaskLibrary.cleanDisk(request);

        assertNotNull(response);
        assertEquals(0, response.getResponseCode());
    }

    /**
     * This test will ensure that the disable WiFi function can succeed regardless of current mode or if there is
     * any bluetooth devices.
     * Is largely platform dependant (Tested function(s) rely heavily on OS specific code).
     */
    @Test
    @DisplayName("Disable WiFi (Platform Dependant)")
    void testDisableWiFi() {
        String username = DBManagerTests.getSetupUser()[0];
        String password = DBManagerTests.getSetupUser()[1];
        UserData setupUser = new UserData(null, username, password, "Administrator");

        TaskRequest<Integer> request = new TaskRequest<>(Task.DISABLEWIFI, setupUser);
        TaskResponse<Void> response = RAMTTaskLibrary.disableWifi(request);

        assertNotNull(response);
        assertEquals(0, response.getResponseCode());
    }

    /**
     * This test will ensure that the enable WiFi function can succeed regardless of current mode.
     *
     * Is largely platform dependant (Tested function(s) rely heavily on OS specific code).
     */
    @Test
    @DisplayName("Enable WiFi (Platform Dependant)")
    void testEnableWiFi() {
        String username = DBManagerTests.getSetupUser()[0];
        String password = DBManagerTests.getSetupUser()[1];
        UserData setupUser = new UserData(null, username, password, "Administrator");

        TaskRequest<Integer> request = new TaskRequest<>(Task.ENABLEWIFI, setupUser, 0);
        TaskResponse<Void> response = RAMTTaskLibrary.enableWifi(request);

        assertNotNull(response);
        assertEquals(0, response.getResponseCode());
    }

    /**
     * This test will ensure that the disable Bluetooth function can succeed regardless of current mode or if there is
     * any bluetooth devices.
     *
     * Is largely platform dependant (Tested function(s) rely heavily on OS specific code).
     */
    @Test
    @DisplayName("Disable Bluetooth (Platform Dependant)")
    void testDisableBluetooth() {
        String username = DBManagerTests.getSetupUser()[0];
        String password = DBManagerTests.getSetupUser()[1];
        UserData setupUser = new UserData(null, username, password, "Administrator");

        TaskRequest<Integer> request = new TaskRequest<>(Task.DISABLEWIFI, setupUser);
        TaskResponse<Void> response = RAMTTaskLibrary.disableWifi(request);

        assertNotNull(response);
        assertEquals(0, response.getResponseCode());
    }

    /**
     * This test will ensure that the enable Bluetooth function can succeed regardless of current mode or if there is
     * any bluetooth devices.
     *
     * Is largely platform dependant (Tested function(s) rely heavily on OS specific code).
     */
    @Test
    @DisplayName("Enable Bluetooth (Platform Dependant)")
    void testEnableBluetooth() {
        String username = DBManagerTests.getSetupUser()[0];
        String password = DBManagerTests.getSetupUser()[1];
        UserData setupUser = new UserData(null, username, password, "Administrator");

        TaskRequest<Integer> request = new TaskRequest<>(Task.ENABLEBLUETOOTH, setupUser, 0);
        TaskResponse<Void> response = RAMTTaskLibrary.enableBluetooth(request);

        assertNotNull(response);
        assertEquals(0, response.getResponseCode());
    }
}
