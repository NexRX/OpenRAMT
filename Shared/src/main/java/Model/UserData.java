package Model;

//import java.io.FileInputStream;

import java.io.Serializable;

/**
 * The model of the userdata that will be held in memory to be used throughout the program.
 * It is Serializable and once done, should be stored in a database for long term storage.
 */
public class UserData implements Serializable {

	private final String host;
	private final int port;
	private final String username;
	private final String password;
	private String group = null;

	/**
	 * Constructs the user data by importing it via the hardcoded path "/application.data/userdata.bin".
	 */
	public UserData(String host, int port, String username, String password) {
			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password;
	}

	public UserData(String host, String username, String password) {
		this.host = host;
		this.port = 3069; //default port
		this.username = username;
		this.password = password;
	}

	public boolean connectable() {
		return this.host != null;
	}

	/// getters and setters

	public void setGroup(String group) {
		this.group = group;
	}

	public String getGroup() {
		return group;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
