package Model.User;

//import java.io.FileInputStream;

import java.io.Serializable;

/**
 * The model of the userdata that will be held in memory to be used throughout the program.
 * It is Serializable and once done, should be stored in a database for long term storage.
 */
public class UserData implements Serializable {
	//None final values are values which aren't known immediately by the client and thus must be changeable.
	private final String host;
	private final int port;
	private String id = null;
	private final String username;
	private final String password;
	private String group = null;
	private boolean suspended = false; //Default Value in DB.

	/**
	 * The minimum construction for userdata. The port is assumed and updatable values are assigned as null. All other
	 * params are assigned to this UserData object.
	 * @param host The OpenRAMT server's IP address.
	 * @param username The user's username.
	 * @param password The user's hashed password. Please insure it is hashed before assigning.
	 */
	public UserData(String host, String username, String password) {
		this.host = host;
		this.port = 3069; //default port
		this.username = username;
		this.password = password;
	}

	/**
	 * A lesser construction for userdata. The port is not default and updatable values are assigned as null. All other
	 * params are assigned to this UserData object.
	 * @param host The OpenRAMT server's IP address.
	 * @param port The port to connect to the OpenRAMT server.
	 * @param username The user's username.
	 * @param password The user's hashed password. Please insure it is hashed before assigning.
	 */
	public UserData(String host, int port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	/**
	 * The minimum construction for userdata with group. The port is assumed and updatable values are assigned as null.
	 * All other params are assigned to this UserData object.
	 * @param host The OpenRAMT server's IP address.
	 * @param username The user's username.
	 * @param password The user's hashed password. Please insure it is hashed before assigning.
	 * @param group The user's group in the server.
	 */
	public UserData(String host, String username, String password, String group) {
		this.host = host;
		this.port = 3069; //default port
		this.username = username;
		this.password = password;
		this.group = group;
	}

	/**
	 * This is more for the server, constructs a UserData object as normal but with a known ID.
	 * @param host The OpenRAMT server's IP address.
	 * @param port The port to connect to the OpenRAMT server.
	 * @param id The ID assigned to the user within the server.
	 * @param username The user's username.
	 * @param password The user's hashed password. Please insure it is hashed before assigning.
	 */
	public UserData(String host, int port, String id, String username, String password) {
			this.host = host;
			this.port = port;
			this.id = id;
			this.username = username;
			this.password = password;
	}

	/**
	 * This is more for the server but essentially is the full constructo. constructs a UserData object with all the
	 * information upfront.
	 * @param host The OpenRAMT server's IP address.
	 * @param port The port to connect to the OpenRAMT server.
	 * @param id The ID assigned to the user within the server.
	 * @param username The user's username.
	 * @param password The user's hashed password. Please insure it is hashed before assigning.
	 * @param group A group within the server that is also assigned to this user.
	 */
	public UserData(String host, int port, String id, String username, String password, String group) {
		this.host = host;
		this.port = port;
		this.id = id;
		this.username = username;
		this.password = password;
		this.group = group;
	}

	/**
	 * This is more for the server but essentially is the full constructo. constructs a UserData object with all the
	 * information upfront.
	 * @param host The OpenRAMT server's IP address.
	 * @param port The port to connect to the OpenRAMT server.
	 * @param id The ID assigned to the user within the server.
	 * @param username The user's username.
	 * @param password The user's hashed password. Please insure it is hashed before assigning.
	 * @param group A group within the server that is also assigned to this user.
	 * @param suspended Whether the user is suspended or not aka false.
	 */
	public UserData(String host, int port, String id, String username, String password, String group, boolean suspended) {
		this.host = host;
		this.port = port;
		this.id = id;
		this.username = username;
		this.password = password;
		this.group = group;
		this.suspended = suspended;
	}

	public boolean connectable() {
		return this.host != null;
	}

	/// getters and setters

	public void setID(String id) {
		this.id = id;
	}
	public String getID() {
		return this.id;
	}

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

	public Boolean isSuspended() { return suspended; }
	public void setSuspended(boolean suspended) { this.suspended = suspended; }
}
