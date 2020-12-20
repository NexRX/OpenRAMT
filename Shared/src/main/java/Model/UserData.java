package Model;

//import java.io.FileInputStream;

/**
 * The model of the userdata that will be held in memory to be used throughout the program.
 * It is Serializable and once done, should be stored in a database for long term storage.
 */
public class UserData {

	private String host;
	private String username;
	private String password;

	/**
	 * Constructs the user data by importing it via the hardcoded path "/application.data/userdata.bin".
	 */
	public UserData(String host, String username, String password) {
			this.host = host;
			this.username = username;
			this.password = password;
	}

	public boolean connectable() {
		if (this.host != null) {
			return true;
		}
		return false;
	}
	/// getters and setters

}
