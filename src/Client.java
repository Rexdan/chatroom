import	java.util.*;
import java.util.jar.Attributes.Name;

import org.omg.CosNaming.NamingContextExtPackage.AddressHelper;

import	java.io.*;
import	java.net.*;

public class Client {

	public static int port;

	public Socket socket;

	private static String UserName;

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

	public static void exit()
	{
		System.exit(0);
	}

	public static void main( String [] arg ) throws Exception
	{
		Socket		socket;
		SocketAddress address;
		BufferedReader	stdIn;
		BufferedReader	fromServer;
		PrintWriter	toServer;
		String		s;
		String		result;
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

		ip = InetAddress.getByName("cp.cs.rutgers.edu");
	    ipAddr = ip.getHostAddress();

	    //socket = connect("cd.cs.rutgers.edu");
	    //socket = connect(ipAddr);
	    //System.out.println(socket);

	    //socket = new Socket("cd.cs.rutgers.edu", 8564);

		do {
			socket = connect( ""/*ipAddr*/);
		} while ( socket == null );

		//For reading all keyboard input. Duh.
		stdIn = new BufferedReader( new InputStreamReader( System.in ) );

		//We need/want to get information from the Server.
		fromServer = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );

		//This is a give/take relationship.
		toServer = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ), true );

		/*String name = "";

		Scanner sc = new Scanner(System.in);
		String command = "";

		name = sc.nextLine();
		user = new User(name);

		if(name.length() > 100)
		{
			System.out.println("Username must be at most 100 characters long. Please enter a different username.");
			System.exit(1);
		}*/

		//Want a reference to the username for use in session thread!
		//toServer.println(name);

		boolean firstRun = true;
		String name = "";

		while ( (s = stdIn.readLine()) != null )
		{
			if(firstRun)
			{
				if(s.substring(0,5).equalsIgnoreCase("@name"))
				{
					toServer.println( s );
					result = fromServer.readLine();
					System.out.println("Inside of firstRun: " + result);
					if(result.equals("cameFromNameExists"))
					{
						result = "User already exists in chat. Please restart client with different username.";
						System.out.println( result );
						break;
					}
					else if(result.equals("cameFromNameExists"))
					{
						result = "User already exists in chat. Please restart client with different username.";
						System.out.println( result );
						break;
					}
					System.out.println( result );
					firstRun = false;
					continue;
				}
			}
			toServer.println( s );
			result = fromServer.readLine();
			if(result.equals("cameFromExit"))
			{
				result = "You have disconnected.";
				System.out.println( result );
				break;
			}
			System.out.println( result );
		}
		socket.close();
	}
}