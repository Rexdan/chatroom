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

	private String message;

	public SessionThread( Socket s )
	{
		socket = s;
	}

	public static boolean isClosed()
	{
		return closed;
	}

	public String getMessage()
	{
		return this.message;
	}

	BufferedReader	fromClient;
	PrintWriter	toClient;
	String		s;
	String		name = "";
	StringBuffer	buffer;
	boolean cameFromNameExists = false;
	boolean cameFromNameLong = false;
	boolean cameFromExit = false;
	boolean searching = true;

	public void run()
	{
		Server.loadUsers();

		try {
			fromClient = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );

			toClient = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ), true );

			Server.loadUsers();

			boolean joined = false;
			
			toClient.println(Server.messages.size());
			if(!Server.messages.isEmpty())
			{
				System.out.println("................START OF CHAT HISTORY................");

				for(int i = 0; i < Server.messages.size(); i++)
				{
					toClient.println(Server.messages.get(i));
				}
				System.out.println("................END OF CHAT HISTORY................");
			}

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
							//System.out.println(name);
							if(name.length() > 100)
							{
								cameFromNameLong = true;
								exit();
								socket.close();
								return;
							}
							user = new User(name);
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
											socket.close();
											return;
										}
										else if(Server.users.get(i).equals(""))
										{
											randomColorSetter();
											Server.users.set(i, user);
											Server.saveUsers();
											joined = true;
											break;
										}
									}
								}
							}
						}
						else if(command.equalsIgnoreCase("who"))
						{
							buffer = new StringBuffer("COMMAND: " + command);
							System.out.println("COMMAND: " + command);
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
						else if(command.equalsIgnoreCase("private"))
						{
							buffer = new StringBuffer("IN YOUR PRIVATES.");
							System.out.println("IN YOUR PRIVATES.");
						}
						else if(command.equalsIgnoreCase("exit"))
						{
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

				//This is how ANYTHING gets sent to the Server, i.e. CHAT HISTORY.

				//System.out.println(user + ": " + buffer.toString());

				message = user.toString();
				message = message.concat(": ").concat(buffer.toString());

				Server.saveMessage(message);

				//This is how ANYTHING gets sent back to the current client.
				toClient.println( Server.getMessage() );
				Server.incrCounter();
				System.out.println("Current Count: " + Server.getCount());
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
		}
		else if(cameFromNameExists)
		{
			message = "cameFromNameExists";
			System.out.println("IN CAMEFROMNAMEEXISTS");
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
			System.out.println(user + " has disconnected.");
		}

		buffer = new StringBuffer(message);
		System.out.println("RIGHT BEFORE BUFFER: " + buffer.toString());
		toClient.println(buffer.toString());
		closed = true;
	}

	public void randomColorSetter()
	{
		/*
		 * This is for giving the username a color and it does it randomly.
		 */

		String resultingColor = "";

		Random rn = new Random();

		String firstPart = "\033[";

		resultingColor = resultingColor.concat(firstPart);

		//0,1,2,3,4,7,8
		String secondPart = "";

		int toSwitch = 1 + rn.nextInt(2 - 1 + 1);

		if(toSwitch == 1)
		{
			int forSecondPart1 = 0 + rn.nextInt(4 - 0 + 1);
			secondPart = secondPart.concat(Integer.toString(forSecondPart1));
			secondPart = secondPart.concat(";");
			resultingColor = resultingColor.concat(secondPart);
		}
		else if(toSwitch == 2)
		{
			int forSecondPart2 = 7 + rn.nextInt(8 - 7 + 1);
			secondPart = secondPart.concat(Integer.toString(forSecondPart2));
			secondPart = secondPart.concat(";");
			resultingColor = resultingColor.concat(secondPart);
		}

		//from 30 to 37
		String thirdPart = "";
		int forThirdPart = 30 + rn.nextInt(37 - 30 + 1);
		thirdPart = thirdPart.concat(Integer.toString(forThirdPart));
		thirdPart = thirdPart.concat(";");
		resultingColor = resultingColor.concat(thirdPart);

		//40 to 47
		String fourthPart = "";
		int forFourthPart = 40 + rn.nextInt(47 - 40 + 1);
		fourthPart = fourthPart.concat(Integer.toString(forFourthPart));
		fourthPart = fourthPart.concat("m");
		resultingColor = resultingColor.concat(fourthPart);

		user.setColor(resultingColor);

	}
}