import java.util.concurrent.TimeUnit;
import	java.io.*;
import	java.net.*;

public class Client implements Runnable{

	public static int port;

	public Socket socket;

	private static Socket connect( String host ) throws Exception
	{
		try
		{
			return new Socket( host, port );
		}
		catch ( ConnectException ce )
		{
			return null;
		}
	}
	
	static String s, result;
	static BufferedReader stdIn, fromServer;
	static PrintWriter	toServer;
	static String name = "";
	static boolean inSession = true;
	
	public static void main( String [] arg ) throws Exception
	{
		Socket		socket;
		String ipAddr = "";
		System.out.println("Please specify a username with the @name command.");
		System.out.println("Type -h for a list of commands.");
		try
	  	{
			ipAddr = arg[0];
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
		
		while((s = stdIn.readLine()) != null)
		{
			try
			{
				if(s.charAt(0) == '@')
				{
					if(s.length() < 6)
					{
						s = "ERROR. You must first specify a username with the @name command.";
						System.err.println(s);
						continue;
					}
					else if(s.equalsIgnoreCase("@name") || s.substring(6).length() == 0)
					{
						s = "ERROR. You cannot have an empty username.";
						System.err.println(s);
						continue;
					}
					else if(s.substring(6).length() > 100)
					{
						s = "ERROR. The username that you entered exceeds 100 characters.";
						System.err.println(s);
						continue;
					}
					else
					{
						name = s.substring(6);
						break;
					}
				}
				else if(s.equals("-h"))
				{
					help();
					continue;
				}
				else if(s.charAt(0) != '@' || !s.equals("-h"))
				{
					s = "ERROR. You entered neither a username after '@name' or '-h' for help.";
					System.err.println(s);
					continue;
				}
			}catch(Exception e)
			{
				
			}
		}
		
	  	int count = 0;
		int tries = 10;
		do {
			System.out.println("Waiting to connect to server...");
			if(count == 10)
			{
				System.out.println("Client Timed Out.");
				System.exit(0);
			}
			else if(count == 9)
			{
				System.out.println("Retrying connection " + (tries - count) + " more time...");
			}
			else System.out.println("Retrying connection " + (tries - count) + " more times...");
			
			TimeUnit.SECONDS.sleep(1);
			count++;
			socket = connect(ipAddr);
		} while ( socket == null );

		//We need/want to get information from the Server.
		fromServer = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );

		//This is a give/take relationship.
		toServer = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ), true );
		
		try
		{
			toServer.println(s);
			result = fromServer.readLine();
			
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
		count = 0;
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
			if(s.equals("-h"))
			{
				help();
				continue;
			}
			toServer.println( s );	
			if(s.equals("@exit"))
			{
				inSession = false;
				break;
			}
		}
		socket.close();
	}
	
	private static void help()
	{
			System.out.println("   @name: To tell the Server what username you wish to use. Can only be used once during your session.");
			System.out.println("          The username cannot exceed 100 Characters.");
			System.out.println("    @who: Will output a list of Active Users as well as a list of Active Users who are receiving private messages.");
			System.out.println("@private: This will initiate a private conversation with another Active User assuming that they are not receiving private messages from another user.");
			System.out.println("          Must specify the user after '@private' is typed on the same line.");
			System.out.println("    @end: This will terminate a private conversation with the Active User who you initially started a private conversation with.");
			System.out.println("          Must specify the user after '@end' is typed on the same line.");
			System.out.println("   @exit: This will terminate your session with the chatroom.");
	}

	@Override
	public void run()
	{
		synchronized (this)
		{
			try
			{
				while(true)
				{
					result = fromServer.readLine();
					if(result != null) System.out.println( result );
					else
					{
						System.err.println("We have lost connection to the Server.");
						System.exit(1);
					}
				}

			} catch (IOException e)
			{
				if(inSession == false)
				{
					System.out.println("You have exited the chat session.");
					System.exit(0);
				}
			}
		}
	}
}