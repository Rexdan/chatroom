import	java.util.*;

import com.sun.webkit.ThemeClient;

import	java.io.*;
import	java.net.*;
import java.nio.file.ClosedWatchServiceException;
import java.rmi.activation.ActivationGroupDesc.CommandEnvironment;

public class SessionThread extends Thread {

	private Socket		socket;

	private User user;

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

	BufferedReader	fromClient;
	PrintWriter	toClient;
	String		s;
	String		name = "";
	StringBuffer	buffer;
	boolean cameFromNameExists = false;
	boolean cameFromNameLong = false;
	boolean cameFromExit = false;

	public void run()
	{
		Server.loadUsers();

		try {
			fromClient = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );

			toClient = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ), true );

			Server.loadUsers();

			boolean joined = false;

			while ( (s = fromClient.readLine()) != null )
			{
				buffer = new StringBuffer( s );

				String command = buffer.toString();

				char c = command.charAt(0);
				command = command.substring(1);

				if(c == '@')
				{
					//System.out.println("This is the command: " + command);
					try
					{
						/*
						 * This will always be the first thing to run when a client
						 * process starts. So, no need to worry about the user object
						 * being null afterwards.
						 */
						if(command.substring(0, 4).equalsIgnoreCase("name"))
						{
							//We have a reference to the username!
							//Using substring to get rid of the @name bit.
							name = command.substring(5);
							if(name.length() > 100)
							{
								cameFromNameLong = true;
								exit();
							}
							user = new User(name);
							System.out.println("After being added: " + user);
							if(!Server.users.isEmpty())
							{
								for(int i = 0; i < Server.users.size(); i++)
								{
									if(Server.users.get(i) instanceof User)
									{
										if(Server.users.get(i).equals(user))
										{
											//Server.saveUsers();
											cameFromNameExists = true;
											exit();
										}
										else if(Server.users.get(i).equals(""))
										{
											Server.users.set(i, user);
											Server.saveUsers();
											//System.out.println(name + " has joined the chat session.");
											//buffer = new StringBuffer("You have joined the chat session.");
											//toClient.println(buffer.toString());
											joined = true;
											break;
										}
									}
								}
							}
						}
						else if(command.equalsIgnoreCase("private"))
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

							/* If there is more than one user, we append a period at the end.
							 * This is purely for formatting.
							 */
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
							/*String message = "You have disconnected.";
							buffer = new StringBuffer(message);
							toClient.println(buffer.toString());
							for(int i = 0; i < Server.users.size(); i++)
							{
								if(Server.users.get(i).equals(user))
								{
									Server.users.set(i, new User());
									Server.saveUsers();
									break;
								}
							}
							closed = true;
							System.out.println(user + " has disconnected.");
							socket.close();
							return;*/
							cameFromExit = true;
							exit();
							socket.close();
							return;
						}
						else
						{
							System.out.println("ERROR. This is not a command.");
						}
					} catch (Exception e)
					{

					}
				}
				/*
				 * This is for allowing the client and server to communicate
				 * NSYNC (Backstreet Boys are better). Needed a boolean to allow for switching.
				 */
				if(joined)
				{
					System.out.println(name + " has joined the chat session.");
					buffer = new StringBuffer("You have joined the chat session.");
					toClient.println(buffer.toString());
					joined = false;
					continue;
				}
				if(cameFromNameExists)
				{
					socket.close();
					return;
				}
				if(cameFromNameLong)
				{
					socket.close();
					return;
				}
				//This is how ANYTHING gets sent to the Server, i.e. CHAT HISTORY.
				System.out.println(user + ": " + buffer.toString());
				//This is how ANYTHING gets sent back to the current client.
				toClient.println( user + ": "+ buffer.toString() );
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

	public void exit()
	{
		String message = "";

		if(cameFromNameLong)
		{
			message = "cameFromNameLong";
			cameFromNameLong = false;
		}
		else if(cameFromNameExists)
		{
			message = "cameFromNameExists";
			System.out.println("IN CAMEFROMNAMEEXISTS");
			cameFromNameExists = false;
		}
		else if(cameFromExit)
		{
			message = "cameFromExit";
			for(int i = 0; i < Server.users.size(); i++)
			{
				if(Server.users.get(i).equals(user))
				{
					Server.users.set(i, new User());
					Server.saveUsers();
					break;
				}
			}
			cameFromExit = false;
			System.out.println(user + " has disconnected.");
		}

		buffer = new StringBuffer(message);
		System.out.println("RIGHT BEFORE BUFFER: " + buffer.toString());
		toClient.println(buffer.toString());
		closed = true;
	}
}