import	java.util.*;
import	java.io.*;
import	java.net.*;
import java.nio.file.ClosedWatchServiceException;
import java.rmi.activation.ActivationGroupDesc.CommandEnvironment;

public class SessionThread extends Thread {

	private Socket		socket;

	private static boolean closed = false;

	public ArrayList<String> privateMessages = new ArrayList<String>();

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
		String		name;
		StringBuffer	buffer;
		Server.loadUsers();

		try {
			fromClient = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			
			//We have a reference to the username!
			name = fromClient.readLine();
			//System.out.println("After reading: " + name);
			
			toClient = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ), true );
		
			while ( (s = fromClient.readLine()) != null )
			{
				buffer = new StringBuffer( s );

				String command = buffer.toString();

				char c = command.charAt(0);

				command = command.substring(1);

				if(c == '@')
				{
					System.out.println("This is the command: " + command);
					try
					{
						if(command.equalsIgnoreCase("private"))
						{
							buffer = new StringBuffer("IN YOUR PRIVATES.");
							System.out.println("IN YOUR PRIVATES.");
						}
						else if(command.equalsIgnoreCase("who"))
						{
	
							String result = "List of Active Users: ";
							
							boolean first = true;
							boolean singleUser = true;
							
							for(int i = 0; i < Server.users.size(); i++)
							{
								if(!Server.users.get(i).getName().equals(""))
								{
									if(first == true)
									{
										result = result.concat(Server.users.get(i).getName());
										first = false;
									}
									else
									{
										result = result.concat(", " + Server.users.get(i).getName());
										singleUser = false;
									}
								}
							}
							
							if(singleUser = false)
							{
								result = result.substring(0, result.length() - 2);
								result = result.concat(".");
							}
							else
							{
								result = result.concat(".");
							}
							
							buffer = new StringBuffer(result);
							toClient.println(buffer.toString());

						}
						else if(command.equalsIgnoreCase("exit"))
						{
							buffer = new StringBuffer(command);
							toClient.println(buffer.toString());
							if(Client.getUserName().equals(name))
							{
								System.out.println("blah blah");
								Client.exit();
							}
							closed = true;
							break;
						}
						else
						{
							System.out.println("ERROR. This is not a command.");
						}
					} catch (Exception e)
					{
						
					}
				}
				
				System.out.println(buffer.toString());
				toClient.println( buffer.toString() );	
			}
			socket.close();
		}
		catch ( Exception e )
		{
			if(closed) return;
			System.out.println( "The client has terminated prematurely..." + e.toString() );
			e.printStackTrace();
		}
	}
}