import	java.util.*;
import	java.io.*;
import	java.net.*;
import java.nio.file.ClosedWatchServiceException;
import java.rmi.activation.ActivationGroupDesc.CommandEnvironment;

public class SessionThread extends Thread {

	private Socket		socket;

	private static boolean closed = false;

	public ArrayList<String> messages = new ArrayList<String>();

	public SessionThread( Socket s )
	{
		socket = s;
	}

	public static boolean isClosed()
	{
		return closed;
	}

	public void run()
	{
		BufferedReader	fromClient;
		PrintWriter	toClient;
		String		s;
		StringBuffer	buffer;
		int		i, limit;

		try {
			fromClient = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			toClient = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ), true );
			while ( (s = fromClient.readLine()) != null )
			{
				buffer = new StringBuffer( s );
				limit = buffer.length()/2;

				System.out.println(buffer.toString());

				String command = buffer.toString();

				char c = command.charAt(0);

				command = command.substring(1);

				if(c == '@')
				{
					try
					{
						if(command.equalsIgnoreCase("private"))
						{
							System.out.println("IN YOUR PRIVATES.");
						}
						else if(command.equalsIgnoreCase("who"))
						{
							System.out.println("WHO ARE YOU PEOPLE.");
						}
						else if(command.equalsIgnoreCase("exit"))
						{
							System.out.println("GTFO.");
							socket.close();
							closed = true;
							break;
						}
						else
						{
							System.out.println("ERROR. This is not a command.");
						}
					} catch (Exception e)
					{
						// TODO: handle exception
					}
				}

				for ( i = 0 ; i < limit ; i++ )
				{
					Server.messages.add(buffer.toString());
				}
				toClient.println( buffer );
			}
			socket.close();
		}
		catch ( Exception e )
		{
			if(closed) return;
			System.out.println( "Exception in SessionThread:" + e.toString() );
			e.printStackTrace();
		}
	}
}