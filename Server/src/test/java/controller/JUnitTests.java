package controller;

import Controller.CryptographyToolbox;
import Controller.Socket.Task.RAMTTaskLibrary;
import Model.Task.Task;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;
import Model.User.UserData;
import Model.User.UserGroup;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

import static Controller.Database.DBManager.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing class to test the current runtime environment for the DBManagement class with some tests also done on the
 * cryptographic toolbox as well.
 *
 * Warning! These test is designed to be executed by Gradle. This is because tests require a new working directory to
 * not conflict with potential project folders. Not only that but if a previous test directory is remaining after all
 * set of tests are complete then the setup test will fail as it cannot setup once it already has and will fail as is
 * expected. Either rewrite tests to follow general processes carried out by setup or run as is with gradle.
 *
 * While setup can create its own folders if needed. It is still recommend to use a empty working directory for tests
 * to be executed in as to ensure testing conditions.
 *
 * This test will also setup the Database by testing the method. This would normally be a unrecommended approach as
 * other tests and testing classes will either need to do something similar or depend on this test first but as the
 * setup test requires user input this method is preferred as to reduce complexity and ease maintainability. Allowing
 * test/code to be written for the working environment.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JUnitTests {
    /**
     * Asserts that true is true. Can be removed but here to test the environment itself.
     */
    @Test
    @DisplayName("Initial Test")
    @Order(1)
    void testInitTest() {
        assertTrue(true);
    }

    /**
     * Used for ease of maintainability to get the default username and password of a user in an array.
     * @return The username and password of the setup user. The root/default admin account. Index 0 is the username and
     * index 1 is the password (unhashed). The same should be used for the FTP details if required.
     */
    public static String[] getSetupUser() {

        return new String[]{"FirstUser", "FirstPassword"};
    }

    /**
     *  Tests not just weather a database setup can be done but also if its tools work as well. If the cryptographic
     *  toolbox cannot create and validate a hash then the test will fails with that related reason with a message.
     */
    @Test
    @Order(2)
    @DisplayName("Database Setup")
    void testStartDB() {
        String username = getSetupUser()[0];
        String password = getSetupUser()[1];

        String hashedPassword = null;
        try {
            hashedPassword = CryptographyToolbox.generatePBKDF2WithHmacSHA512(password);
            assertTrue(CryptographyToolbox.validatePBKDF2WithHmacSHA512(password, hashedPassword), "Password validation failed");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            fail("The (Java) environment doesn't meet the cryptographic requirements. Failed setup.");
        }

        assertNotNull(hashedPassword);

        setup(
                username,
                hashedPassword,
                true,
                3069,
                getSetupUser()[0],
                getSetupUser()[1]);

        assertTrue(isSetup(), "Setup incomplete for unknown reason.");
    }

    /**
     * Tests to see if the database has stopped after any test where it has start (in general).
     */
    @Test
    @DisplayName("StopDB")
    @Order(3)
    void testStopDB() {
        assertTrue(isStopped(), "Database wasn't shut down correctly.");
    }

    /**
     * Not only tests user insertion but consequently getting user as well. If the values retrieved aren't equal then
     * the test is failed.
     *
     * This test specifically is designed for the minimum extreme tolerable character amount. This is not within app
     * constraints but within DB constraints (as not to fail) on purpose.
     */
    @Test
    @DisplayName("InsertUser - Minimum")
    @Order(4)
    void testInsertUserMinimum() {
        UserData insertUser = new UserData("127.0.0.1", "a", "a", "Default");
        UserData getUser = null;
        try {
            int resultCode = addUser(insertUser);
            assertEquals(0, resultCode);
            getUser = getUser("a");
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Couldn't insert user into DB because SQL Exception.");
        }

        assertNotNull(getUser);
        assertEquals(insertUser.getUsername(), getUser.getUsername());
        assertNull(getUser.getPassword()); // Unless we change things, this should be null for security
    }

    /**
     * Not only tests user insertion but consequently getting user as well. If the values retrieved aren't equal then
     * the test is failed.
     *
     * This test specifically is designed for the typical tolerable character amount. Within app constraint.
     */
    @Test
    @DisplayName("InsertUser - Typical")
    @Order(5)
    void testInsertUser() {
        UserData insertUser = new UserData("127.0.0.1", "TypicalName", "TypicalName", "Default");
        UserData getUser = null;
        try {
            addUser(insertUser);
            getUser = getUser("TypicalName");
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Couldn't insert user into DB because SQL Exception.");
        }

        assertNotNull(getUser);
        assertEquals(insertUser.getUsername(), getUser.getUsername());
        assertNull(getUser.getPassword()); // Unless we change things, this should be null for security
    }

    /**
     * Not only tests user insertion but consequently getting user as well. If the values retrieved aren't equal then
     * the test is failed.
     *
     * This test specifically is designed for the maximum extreme tolerable character amount. This is not within app
     * constraints but within DB constraints (as not to fail) on purpose.
     */
    @Test
    @DisplayName("InsertUser - Maximum")
    @Order(6)
    void testInsertUserMaximum() {
        //64 characters
        String veryLongName = "6vL6uZLDHyOeC9vTw3ZO6OOcIbfpB8mUhFY7wEYq5VN72hyW350sRHUQqh5BAktc";
        //512 characters
        String veryLongPass = "WQZGyIavbEyZnvX4v1qoVfXazAoiFy2lweKJFZQAxSs2jDMbnWGEEYLkr1gdXi9IJLWddFXNEDESldomTTTnUjOU21LTXuF9dMVGXoYffcgdYCmLclfonOd2W4zpGifNbvAL2lxHXt4r3zAdtyx1eJtkR1vBUJRC70h83abGuzNy1AYHZbHZohfO9j0cr6tEwWUkVWhJNPHUbXbQm04qWOpSmHtTty2Y9CvDdXK9aU0h7MsNhTbo4JZxD6ZVdzAXFJAtdcUWyuqkL0k802zbjZ2RcsP5FEwn9osCOEL4jyhqWt4W6VaneZiIIAmc7U9ZGglgWrDzk8eyboW6A57FG5RBd33Hzlk1nk6du1Cu7CjdSk2ULF5aArFs3Xiupyteyhry7BRScXqlQInp85IrG6VxzAABXgVgIYyuE8PFy8qIc9PyGnmMfxOzrc2PpI0oStRjkklkxi0zzWJi3AJOy60V3Noc3Sx148V7XJcmyJgTDh8ViQgLFNuGFSNbp4UH";
        UserData insertUser = new UserData("127.0.0.1", veryLongName, veryLongPass, "Default");
        UserData getUser = null;
        try {
            addUser(insertUser);
            getUser = getUser(veryLongName);
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Couldn't insert user into DB because SQL Exception.");
        }

        assertNotNull(getUser);
        assertEquals(insertUser.getUsername(), getUser.getUsername());
        assertNull(getUser.getPassword()); // Unless we change things, this should be null for security
    }

    /**
     * This test asserts not only that the delete user was deleted but that it was inserted beforehand too.
     * Thus ensuring that a user was actually deleted.
     */
    @Test
    @DisplayName("Delete User")
    @Order(7)
    void testDeleteUser() {
        UserData insertUser = new UserData("127.0.0.1", "DeleteMe", "DeletePassword", "Default");

        try {
            int resultCode = addUser(insertUser);
            assertEquals(0, resultCode);

            int deleteResultCode = deleteUser(insertUser.getUsername());
            assertEquals(0, deleteResultCode);
        } catch (SQLException e) {
            e.printStackTrace();
            fail("SQL exception when adding or delete user. Was resultCode 0? If so delete failed.");
        }
    }

    /**
     * Asserts that a group was not only inserted but received to ensure that the group exists.
     */
    @Test
    @DisplayName("Add Group")
    @Order(8)
    void testAddGroup() {
        UserGroup insertGroup = new UserGroup("NewGroup", false, true, true, true, true);

        try {
            int resultCode = addGroup(insertGroup);
            assertEquals(0, resultCode);

            UserGroup getGroup = getGroup(insertGroup.getName());

            assertNotNull(getGroup);

            assertEquals(insertGroup.getName(), getGroup.getName());

            assertFalse(getGroup.isAdmin(),"isAdmin should be false but isn't.");
            assertTrue(getGroup.isGeneral(),"isGeneral should be true but isn't.");
            assertTrue(getGroup.isProcess(),"isProcess should be true but isn't.");
            assertTrue(getGroup.isMonitoring(),"isMonitoring should be true but isn't.");
            assertTrue(getGroup.isPower(),"isPower should be true but isn't.");
        } catch (SQLException e) {
            e.printStackTrace();
            fail("SQL exception when adding or delete user. Was resultCode 0? If so delete failed.");
        }
    }

    /**
     * Returns test settings values to easy maintainability for tests that test settings.
     * @return A string array with index 0 being "TestSetting" and index 0 being "TestValue".
     */
    private static String[] getTestSettingValues() {
        return new String[]{"TestSetting", "TestValue"};
    }

    /**
     * This test will try to add a new setting to the database, it doesn't receive it but tests the resulting int code.
     */
    @Test
    @DisplayName("Add Setting")
    @Order(9)
    void testAddSettings() {
        String setting = getTestSettingValues()[0];
        String settingValue = getTestSettingValues()[1];

        try {
            int resultCode = addSetting(setting, settingValue);
            assertEquals(0, resultCode);
        } catch (SQLException e) {
            e.printStackTrace();
            fail("SQL exception when adding or delete user. Was resultCode 0? If so delete failed.");
        }
    }

    /**
     * This test will try to see if a new setting was added to the database in the previous test.
     */
    @Test
    @DisplayName("Get Setting")
    @Order(10)
    void testGetSetting() {
        String setting = getTestSettingValues()[0];
        String settingValue = getTestSettingValues()[1];

        try {
            String result = getSetting(setting);
            assertEquals(settingValue, result);
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Failed to receive setting value correctly. SQL exception.");
        }
    }

    /**
     * Checks if the server is marked for wipe. Cannot do the wipe due to Java locking the file even after closing DB.
     */
    @Test
    @DisplayName("Mark For Wipe")
    @Order(11)
    void testWipeDataBase() {
        TaskResponse<Void> response = RAMTTaskLibrary.factoryReset(
                new TaskRequest<>(Task.FACTORYRESET,
                        new UserData(
                                "127.0.0.1",
                                getSetupUser()[0],
                                getSetupUser()[1],
                                "Administrator"
                        )
                ),
                true
        );

        assertEquals(0, response.getResponseCode());
    }

    /**
     * Starts a dummy process (text editor) and gets the processes PID to kill it. The resulting response code is then
     * tested to see if it was successful. If the test is unsuccessful the working directory may be locked as a result.
     *
     * Is largely platform dependant (Tested function(s) rely heavily on OS specific code).
     */
    @Test
    @DisplayName("Kill Process (Platform Dependant)")
    void testKillProcess() {
        String username = getSetupUser()[0];
        String password = getSetupUser()[1];

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
        String username = getSetupUser()[0];
        String password = getSetupUser()[1];

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
        String username = getSetupUser()[0];
        String password = getSetupUser()[1];
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
    @Order(12)
    void testStartFTP() {
        String username = getSetupUser()[0];
        String password = getSetupUser()[1];
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
    @Order(13)
    void testStopFTP() {
        String username = getSetupUser()[0];
        String password = getSetupUser()[1];
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
        String username = getSetupUser()[0];
        String password = getSetupUser()[1];
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
        String username = getSetupUser()[0];
        String password = getSetupUser()[1];
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
        String username = getSetupUser()[0];
        String password = getSetupUser()[1];
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
        String username = getSetupUser()[0];
        String password = getSetupUser()[1];
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
        String username = getSetupUser()[0];
        String password = getSetupUser()[1];
        UserData setupUser = new UserData(null, username, password, "Administrator");

        TaskRequest<Integer> request = new TaskRequest<>(Task.ENABLEBLUETOOTH, setupUser, 0);
        TaskResponse<Void> response = RAMTTaskLibrary.enableBluetooth(request);

        assertNotNull(response);
        assertEquals(0, response.getResponseCode());
    }
}
