import	java.util.*;
import	java.io.*;
import	java.net.*;

public class Server implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	public static ArrayList<String> privateMessages = new ArrayList<String>();
	public static ArrayList<String> messages = new ArrayList<String>();
	public static ArrayList<SessionThread> sessions = new ArrayList<SessionThread>();
	public static ArrayList<User> users;
	public static String message = "";
	private static File saveFile;
	
	//May have to subtract one from this variable to display messages correctly
	private static int count = 0;
	private static int MAX = 20;

	public static void main( String [] arg ) throws Exception
	{
		//After Server loads, we want to delete any previous users.
		resetAllUsers();
		ServerSocket	serverSocket = new ServerSocket( 8564, 20 );
		Socket		socket;

		serverSocket.setReuseAddress( true );
		
		while ( (socket = serverSocket.accept()) != null )
		{
			System.out.println( "Accepted an incoming connection" );
			if(sessions.size() == MAX)
			{
				String message = "full";
				PrintWriter toClient = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ), true );
				toClient.println(message);
				socket.close();
			}
			SessionThread sesh = new SessionThread( socket );
			sessions.add(sesh);
			int index = sessions.lastIndexOf(sesh);
			sessions.get(index).start();
		}
		//After Server shuts down, we want to delete all of the users.
		resetAllUsers();
		serverSocket.close();
	}

	public static void incrCounter()
	{
		count++;
	}

	public static int getCount()
	{
		return count;
	}

	public static void saveMessage(String message) throws IOException
	{
		messages.add(message);
	}

	public static String getMessage()
	{
		message = messages.get(count);
		return message;
	}

	public static void resetAllUsers()
	{
		//saveFile = new File("src/users.txt");
		users = new ArrayList<User>(20);
		for(int i = 0; i < 20; i++)
		{
			users.add(i, new User());
		}
		//saveUsers();
	}

	/*public static void loadUsers(){

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
						users.add(i,new User());
					}
				    //fis.close();
				    //ois.close();
			}
			else if (line != null){
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
	}*/
}