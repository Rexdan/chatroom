import	java.util.*;

import org.omg.CosNaming.NamingContextExtPackage.AddressHelper;

import	java.io.*;
import	java.net.*;

public class Client {

	public static int port;
	
	

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
		String ipAddr = null;
		User user;

		try
		{
		      ip = InetAddress.getByName(arg[0]);
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
		}

		do {
			socket = connect(arg[0]);
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