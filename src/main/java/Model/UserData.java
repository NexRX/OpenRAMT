package Model;

//import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Path;

/**
 * The model of the userdata that will be held in memory to be used throughout the program.
 * It is Serializable and once done, should be stored in a database for long term storage.
 */
public class UserData implements Serializable {
	private static final long serialVersionUID = 5359182082764197492L;

	/**
	 * The Mod's path.
	 */
	private Path modPath;
	/**
	 * Servers ID.
	 */
	private String serverID;

	private String host;
	private String username;
	private String password;

	/**
	 * Constructs the user data by importing it via the hardcoded path "/application.data/userdata.bin".
	 */
	public UserData(String host, String username, String password) {

		// Importing data from serialised file of this class.
		try
		{    
			InputStream is = this.getClass().getResourceAsStream("/application.data/userdata.bin");
			ObjectInputStream in = new ObjectInputStream(is); 

			UserData userDataInput = (UserData)in.readObject(); 

			in.close(); 
			is.close(); 

			this.modPath = userDataInput.getModPath();
			this.serverID = userDataInput.getServerID();
			this.host = host;
			this.username = username;
			this.password = password;
		} 

		catch(IOException ex) 
		{ 
			System.out.println("IOException caught"); 
		} 

		catch(ClassNotFoundException ex) 
		{ 
			System.out.println("ClassNotFoundException caught"); 
		} 

	}

	public boolean connectable() {
		if (this.host != null) {
			return true;
		}
		return false;
	}
	/// getters and setters

	/**
	 * Get Mod's Path.
	 * @return Mod Path.
	 */
	public Path getModPath() {
		return modPath;
	}

	/**
	 * Sets the Mod Path
	 * @param path Mod path.
	 */
	public void setModPath(Path path) {
		this.modPath = path;
	}

	/**
	 * Get Server's ID.
	 * @return Server ID.
	 */
	public String getServerID() {
		return serverID;
	}

	/**
	 * Sets the Server ID
	 * @param host Servers ID.
	 */
	public void setServerID(String host) {
		this.serverID = host;
	}
}
