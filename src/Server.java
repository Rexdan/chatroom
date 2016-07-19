import	java.util.*;
import	java.io.*;
import	java.net.*;

public class Server implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	public static ArrayList<String> messages = new ArrayList<String>();
	public static ArrayList<SessionThread> sessions = new ArrayList<SessionThread>();
	public static ArrayList<User> users;
	private static File saveFile;
	private final static int MAX = 20;

	public static void main( String [] arg ) throws Exception
	{
		//After Server loads, we want to delete any previous users.
		resetAllUsers();
		loadHistory();
		ServerSocket	serverSocket = new ServerSocket( 8564, 20 );
		Socket		socket;
		serverSocket.setReuseAddress( true );
		
		System.out.println("Listening for clients...");
		
		while ( (socket = serverSocket.accept()) != null )
		{	
			if(sessions.size() == MAX)
			{
				System.out.println( "Server is full. No longer accepting other connections." );
				String message = "Server is full.";
				PrintWriter toClient = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ), true );
				toClient.println(message);
				socket.close();
			}
			else
			{
				System.out.println( "Accepted an incoming connection." );
				SessionThread sesh = new SessionThread( socket );
				sessions.add(sesh);
				int index = sessions.lastIndexOf(sesh);
				sessions.get(index).start();
				if(!sessions.get(index).joined)System.out.println( "Username already taken. Connection with client terminated." );
				sessions.get(index).sessionIndex = index;
			}
		}
		
		//After Server shuts down, we want to delete all of the users.
		//resetAllUsers();
		
		serverSocket.close();
	}

	public static void saveMessage(String message) throws IOException
	{
		messages.add(message);
	}

	public static void resetAllUsers()
	{
		users = new ArrayList<User>(20);
		for(int i = 0; i < 20; i++)
		{
			users.add(i, new User());
		}
	}

	@SuppressWarnings("unchecked")
	public static void loadHistory(){

	  	try {
			saveFile = new File("history.txt");
			BufferedReader br = new BufferedReader(new FileReader("history.txt"));
			String line = br.readLine();
			if (line == null) {
				 	FileInputStream fis = new FileInputStream("history.txt");
				    //ObjectInputStream ois = new ObjectInputStream(fis);
				    messages = new ArrayList<String>();
				    fis.close();
			}
			else if (line != null){
				FileInputStream fis = new FileInputStream("history.txt");
			    ObjectInputStream ois = new ObjectInputStream(fis);
			    messages = (ArrayList<String>)ois.readObject();
			    ois.close();
			}
			br.close();
		} catch(Exception e) {
		    e.printStackTrace();
		}
	}

	public static void saveHistory(){
		ArrayList<String> temp = new ArrayList<String>();
		
		temp.addAll(messages);
		
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