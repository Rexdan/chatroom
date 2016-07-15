import	java.util.*;
import	java.io.*;
import	java.net.*;

public class Client implements Runnable{

	public static int port;

	public Socket socket;

	private static String UserName;
	
	public ArrayList<Socket> sockets = new ArrayList<Socket>();

	public static String getUserName()
	{
		return UserName;
	}

	private static Socket connect( String host ) throws Exception
	{
		try
		{
			return new Socket( host, 8564 );
		}
		catch ( ConnectException ce )
		{
			return null;
		}
	}
	
	static String s, result;
	static BufferedReader stdIn, fromServer;
	static PrintWriter	toServer;
	static boolean inSession = true;
	
	public static void main( String [] arg ) throws Exception
	{
		Socket		socket;
		SocketAddress address;
		InetAddress ip;
		String ipAddr = "";
		User user;

		/*try
		{
		      ip = InetAddress.getByName(arg[0]);
		      ipAddr = ip.getHostAddress();
			{
			};
		      System.out.println("IP address: " + ip.getHostAddress());
		} catch ( UnknownHostException e1 ) {
		      System.out.println("Could not find IP address for: " + arg[0]);
	    }

		try
		{
			port = Integer.parseInt(arg[1]);
			System.out.println(port);
		} catch (Exception e2)
		{
				System.out.println("You fucked up.");
				System.exit(1);
		}

		try
		{
			user = new User(arg[2]);
			System.out.println(user.getName().toString());
		} catch (Exception e3)
		{
			// TODO: handle exception
		}*/

		/*
		 * We have this here because the ArrayList of users MUST
		 * have each index equal to some sort of object before we can
		 * even check to see if a user exists upon adding one.
		 */

		//ip = InetAddress.getByName("cp.cs.rutgers.edu");
	    //ipAddr = ip.getHostAddress();

	    //socket = connect("cd.cs.rutgers.edu");
	    //socket = connect(ipAddr);
	    //System.out.println(socket);

	    //socket = new Socket("cd.cs.rutgers.edu", 8564);
	    
	  //For reading all keyboard input. Duh.
	  	stdIn = new BufferedReader( new InputStreamReader( System.in ) );
	  	s = stdIn.readLine();
	  	String name = "";
	  	
	  	/*
	  	 * DO MORE ERROR CHECKING AFTER THIS POINT.
	  	 * MILAN IS BEING A CUNT ABOUT MULTITHREADING.
	  	 */
	  	try
	  	{
	  		if(s.length() < 6)
			{
	  			s = "ERROR. You must specify a username with the @name command.";
				s = s.concat("\n");
				s = s.concat("Please restart the client.");
				System.out.println(s);
				System.exit(1);
			}
	  		else if(s.equalsIgnoreCase("@name"))
	  		{
	  			s = "ERROR. You cannot have an empty username.";
				s = s.concat("\n");
				s = s.concat("Please restart the client.");
				System.out.println(s);
				System.exit(1);
	  		}
	  		else
	  		{
	  			name = s.substring(6);
	  			if(name.length() > 100)
	  			{
	  				s = "The username that you entered exceeds 100 characters.";
					s = s.concat("\n");
					s = s.concat("Please restart the client with a shorter username.");
					System.out.println(s);
					System.exit(1);
	  			}
	  		}
	  	}catch(Exception e)
	  	{
	  		
	  	}
	  	
	  	String temp = "192.168.1.126";
	  	temp = "";
		do {
			socket = connect( /*""*/temp);
		} while ( socket == null );

		

		//We need/want to get information from the Server.
		fromServer = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );

		//This is a give/take relationship.
		toServer = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ), true );

		//Want a reference to the username for use in session thread!
		//toServer.println(name);

		//s = stdIn.readLine();
		System.out.println("This is the input: " + s);
		
		try
		{
			toServer.println(s);
			result = fromServer.readLine();
			
			if(result.equals("full"))
			{
				System.out.println("Server at MAX capacity. Please try again later.");
				socket.close();
				System.exit(0);
			}
			
			String nameExists = "User already exists in chat. Please restart client with different username.";

			if(result.equals(nameExists))
			{
				System.out.println( result );
				socket.close();
				System.exit(0);
			}
					
		System.out.println( result );
			
		}catch(Exception e)
		{
			
		}
		
		String search = "";
		int count = 0;
		search = fromServer.readLine();
		count = Integer.parseInt(search);
		String fromSearch = "";
		
		/*
		 * For printing out the chat history after the Client process starts.
		 */
		if(count > 0)
		{
			fromSearch = fromSearch.concat(("................START OF CHAT HISTORY................" + "\n"));
			for(int i = 0; i < count; i++)
			{
				result = fromServer.readLine();
				fromSearch = fromSearch.concat(result + "\n");
			}
			fromSearch = fromSearch.concat("................END OF CHAT HISTORY................");
		}
		
		if(fromSearch.length() > 0) System.out.println(fromSearch);
		
		new Thread(new Client()).start();
		
		while((s = stdIn.readLine()) != null && inSession)
		{
			toServer.println( s );
			if(s.equals("@exit"))
			{
				inSession = false;
				break;
			}
		}
		
		socket.close();
	}

	@Override
	public void run()
	{
		synchronized (this)
		{
			try
			{
				while((result = fromServer.readLine()) != null)
				{	
					System.out.println( result );
				}
			} catch (IOException e)
			{
				if(inSession == false)
				{
					System.out.println("You have exited the chat session.");
					System.exit(0);
				}
				else
				{
					System.err.println("Houston, we have a problem.");
					System.exit(1);
				}
			}
		}
	}
}