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
		InetAddress ip;
		String ipAddr = "";
		
		try
	  	{
			ipAddr = arg[0];
	  		//ip = InetAddress.getByName(arg[0]);
	  		if(ipAddr.isEmpty())
	  		{
	  			System.err.println("You must specify a host address.");
	  			System.exit(1);
	  		}
	  	}
	  	catch(Exception e)
	  	{
	  		System.err.println("You dun goofed.");
	  	}
	  	
	  	try
		{
	  		if(arg[1].isEmpty())
	  		{
	  			System.err.println("You must provide a port number as the second argument.");
	  			System.exit(1);
	  		}
	  		else
	  		{
	  			for(int i = 0; i < arg[1].length(); i++)
	  			{
	  				if(!Character.isDigit(arg[1].charAt(i)))
	  				{
	  					System.err.println("You must provide a port number as the second argument.");
	  		  			System.exit(1);
	  					break;
	  				}
	  			}
	  		}
	  		port = Integer.parseInt(arg[1]);
		} catch (Exception e2)
		{
				System.out.println("You dun goofed.");
				System.exit(1);
		}

	  	stdIn = new BufferedReader( new InputStreamReader( System.in ) );
	  	s = stdIn.readLine();
	  	String name = "";
	  	
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
			socket = connect(ipAddr);
		} while ( socket == null );

		

		//We need/want to get information from the Server.
		fromServer = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );

		//This is a give/take relationship.
		toServer = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ), true );

		//Want a reference to the username for use in session thread!
		//toServer.println(name);

		//s = stdIn.readLine();
		//System.out.println("This is the input: " + s);
		
		try
		{
			toServer.println(s);
			result = fromServer.readLine();
			name = fromServer.readLine();
			
			String nameExists = "User already exists in chat. Please restart client with different username.";

			if(result.equals(nameExists))
			{
				System.out.println( result );
				socket.close();
				System.exit(0);
			}
			
			if(result.equals("Server is full."))
			{
				System.out.println("Server at MAX capacity. Please try again later.");
				socket.close();
				System.exit(0);
			}
					
		System.out.println( result );
			
		}catch(Exception e)
		{
			
		}
	
		//To have the name appended at the beginning of each new message by user.
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
		
		//For first run.
		System.out.print(name + ": ");
		
		while((s = stdIn.readLine()) != null && inSession)
		{
			System.out.print(name + ": ");
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