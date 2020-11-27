package Model;

//import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Path;

public class UserData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5359182082764197492L;
	//private boolean initStatus; 
	private Path modPath;
	private String serverID;

	public UserData() {

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

	/// getters and setters
	// Mod Path 
	private Path getModPath() {
		return modPath;
	}

	public void setModPath(Path path) {
		this.modPath = path;
	}

	// Server ID
	private String getServerID() {
		return serverID;
	}

	public void setServerID(String host) {
		this.serverID = host;
	}
	///

}
