import	java.util.*;
import	java.io.*;
import	java.net.*;

public class Server implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	public static ArrayList<String> messages = new ArrayList<String>();
	public static ArrayList<User> users;
	private static File saveFile;

	public static void main( String [] arg ) throws Exception
	{
		ServerSocket	serverSocket = new ServerSocket( 7777, 20 );
		Socket		socket;

		serverSocket.setReuseAddress( true );
		//loadUsers();
		while ( (socket = serverSocket.accept()) != null )
		{
			System.out.println( "Accepted an incoming connection" );
			new SessionThread( socket ).start();
		}
		//saveUsers();
		deleteAllUsers();
		serverSocket.close();
	}

	public static void deleteAllUsers()
	{
		users = new ArrayList<User>(20);
	}

	public static void loadUsers(){

	  	try {
			saveFile = new File("src/users.txt");
			BufferedReader br = new BufferedReader(new FileReader("src/users.txt"));
			String line = br.readLine();
			if (line == null) {
				//System.out.println("line is null and test exists");
				 	FileInputStream fis = new FileInputStream("src/users.txt");
				    //ObjectInputStream ois = new ObjectInputStream(fis);
				    users = new ArrayList<User>(20);
				    for(int i = 0; i < 20; i++)
					{
						Server.users.add(i,new User(""));
					}
				    //fis.close();
				    //ois.close();
			}
			else if (line != null){
				System.out.println("line is NOT null and users exist");
				FileInputStream fis = new FileInputStream("src/users.txt");
			    ObjectInputStream ois = new ObjectInputStream(fis);
			    users = (ArrayList<User>)ois.readObject();
			    ois.close();
			}
		} catch(Exception e) {
		    e.printStackTrace();
		}
	}

	public static void saveUsers(){
		ArrayList<User> temp = new ArrayList<User>();

		temp.addAll(users);
		try {
		    FileOutputStream fos = new FileOutputStream(saveFile);
		    ObjectOutputStream oos = new ObjectOutputStream(fos);
		    oos.writeObject(temp);
		    oos.flush();
		    oos.close();
		} catch(Exception e) {
		    e.printStackTrace();
		}
	}
}