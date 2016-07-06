import	java.util.*;
import java.util.jar.Attributes.Name;

import org.omg.CosNaming.NamingContextExtPackage.AddressHelper;

import	java.io.*;
import	java.net.*;

public class Client {

	public static int port;
	
	

	private static Socket connect( String host ) throws Exception
	{
		try
		{
			return new Socket( host, 7777 );
		}
		catch ( ConnectException ce )
		{
			return null;
		}
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

		String name;

		Scanner sc = new Scanner(System.in);
		System.out.print("Enter name: ");
		name = sc.nextLine();
		user = new User(name);

		/*
		 * We have this here because the ArrayList of users MUST
		 * have each index equal to some sort of object before we can
		 * even check to see if a user exists upon adding one.
		 */
		Server.loadUsers();
		System.out.println("ArrayList size: " + Server.users.size());

		if(!Server.users.isEmpty())
		{
			for(int i = 0; i < Server.users.size(); i++)
			{
				if(Server.users.get(i) instanceof User)
				{
					if(Server.users.get(i).equals(user))
					{
						System.out.println("User already exists in chat.");
						System.out.println("Please restart client with different username.");
						Server.saveUsers();
						System.exit(0);
					}
					else if(Server.users.get(i).equals(""))
					{
						System.out.println("In for loop for adding user.");
						Server.users.set(i, user);
						Server.saveUsers();
						break;
					}

				}
			}
		}

		System.out.println("ArrayList size: " + Server.users.size());
		//System.out.println("First user in Server's UserList is: " + Server.users.get(0).getName());

		for(int i = 0; i < Server.users.size(); i++)
		{
			System.out.println("User no. " + i + ": " + Server.users.get(i));
		}

		do {
			socket = connect( "" /*ipAddr*/);
		} while ( socket == null );
		stdIn = new BufferedReader( new InputStreamReader( System.in ) );
		fromServer = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
		toServer = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ), true );
		while ( (s = stdIn.readLine()) != null )
		{
			toServer.println( s );
			result = fromServer.readLine();
			System.out.println( "message: " + result );
		}
		socket.close();
	}
}