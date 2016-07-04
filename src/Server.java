import	java.util.*;
import	java.io.*;
import	java.net.*;

public class Server {

	public static ArrayList<String> messages = new ArrayList<String>();
	public static ArrayList<User> users = new ArrayList<User>(20);

	public static void main( String [] arg ) throws Exception
	{
		ServerSocket	serverSocket = new ServerSocket( 6667, 20 );
		Socket		socket;

		serverSocket.setReuseAddress( true );
		while ( (socket = serverSocket.accept()) != null )
		{
			System.out.println( "Accepted an incoming connection" );
			new SessionThread( socket ).start();
		}
		serverSocket.close();
	}
}