import	java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.webkit.ThemeClient;

import	java.io.*;
import	java.net.*;
import java.nio.file.ClosedWatchServiceException;
import java.rmi.activation.ActivationGroupDesc.CommandEnvironment;

public class SessionThread extends Thread {

	private Socket		socket;
	
	//private ReentrantLock lock;

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
	BufferedReader	fromClients;
	PrintWriter	toClient;
	String		s;
	String		name = "";
	StringBuffer	buffer;
	boolean cameFromExit = false;
	boolean searching = true;
	boolean joined = false;
	boolean firstRun = true;
	boolean badRun = false;
	boolean accepting = false;
	int userIndex = 0;
	
	public void run()
	{
		try {
			
			fromClient = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );

			toClient = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ), true );

			boolean badCommand = true;
			
			addUser(fromClient, toClient);
			
			if(joined)
			{
				toClient.println(s);
			}
			else
			{
				toClient.println(s);
				socket.close();
				return;
			}
			
			toClient.println(Server.messages.size());
			if(!Server.messages.isEmpty())
			{
				for(int i = 0; i < Server.messages.size(); i++)
				{
					toClient.println(Server.messages.get(i));
				}
			}
			
			while ( true )
			{
				s = fromClient.readLine();
				
				if(s == null)
				{
					socket.close();
					Server.users.set(userIndex, new User());
					return;
				}
				
				//if(s.length() == 0) continue;
				
				buffer = new StringBuffer( s );

				String command = buffer.toString();

				char c = command.charAt(0);
				command = command.substring(1);

				if(c == '@')
				{
					System.out.println("This is the command: " + command);
					
					/*
					 * If a user exists, then these commands can be carried out.
					 */
					if(user != null)
					{
						try
						{
							/*
							 * Case where user simply types "@private"
							 */
							if(command.equalsIgnoreCase("private"))
							{
								buffer = new StringBuffer("You must specify a user to initiate the private conversation.");
								toClient.println(buffer.toString());
								continue;
							}
							/*
							 * Case where user types "@private " where the space can be either empty
							 * or with a username.
							 */
							else if(command.substring(0,8).equalsIgnoreCase("private "))
							{
								String otherUser = "";
								otherUser = otherUser.concat(command.substring(8));
								if(otherUser.length() == 0)
								{
									buffer = new StringBuffer("You must specify a user to initiate the private conversation.");
									toClient.println(buffer.toString());
									continue;
								}
								
								/*
								 * HANDLE FOR WHEN THE USER DOES WANT TO INTERACT WITH A SEPARATE USER.
								 * MAY NEED SYNCRHONIZATION.
								 */
								
								System.out.println("JKHJKSDHFJKHSDFKJKJF");
								buffer = new StringBuffer("JKHJKSDHFJKHSDFKJKJF");
								toClient.println(buffer.toString());
								badCommand = false;
								continue;
							}
						}catch(Exception e)
						{
							
						}finally
						{
							if(command.equalsIgnoreCase("who"))
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
								badCommand = false;
								continue;
							}
							else if(command.equalsIgnoreCase("exit"))
							{
								cameFromExit = true;
								exit();
								socket.close();
								badCommand = false;
								return;
							}
						}
					}
					if(badCommand)
					{
						toClient.println("ERROR. This is not a command.");
						System.out.println("ERROR. This is not a command.");
						continue;
					}
				}

					else
					{
						message = user.toString();
						message = message.concat(": ").concat(buffer.toString());
						synchronized(this)
						{
							Server.saveMessage(message);
							
							for(int i = 0; i < Server.sessions.size(); i++)
							{
								if(Server.sessions.get(i) != null && Server.sessions.get(i).getUser() != null)
								{
									Server.sessions.get(i).write(Server.getMessage());
								}
							}
							
							//toClient.println(Server.getMessage());
							
							Server.incrCounter();
							//this.counter = Server.getCount();
						}
					}
			}
		}
		catch ( Exception e )
		{
			if(closed) return;
			System.out.println( "The client has terminated prematurely..." + e.toString() );
			e.printStackTrace();
		}
	}
	
	public void write(String input)
	{
		toClient.println(input);
	}
	
	public User getUser()
	{
		return this.user;
	}

	public void exit()
	{
		String message = "";
		if(cameFromExit)
		{
			message = "cameFromExit";
			for(int i = 0; i < Server.users.size(); i++)
			{
				if(Server.users.get(i).equals(user))
				{
					Server.users.set(i, new User());
					//Server.saveUsers();
					break;
				}
			}
			//CHANGE TO PRINT INTO CHAT HISTORY.
			System.out.println(user + " has disconnected.");
		}

		buffer = new StringBuffer(message);
		//System.out.println("RIGHT BEFORE BUFFER: " + buffer.toString());
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
	
	public void addUser(BufferedReader fromClient, PrintWriter toClient) throws IOException
	{
		synchronized (this)
		{
			try
			{
				name = fromClient.readLine();
					try
					{
						this.name = name.substring(1);

						if(name.substring(0, 5).equalsIgnoreCase("name ") && user == null)
						{
							//We have a reference to the username!
							//Using substring to get rid of the @name bit.
							name = name.substring(5);
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
											buffer = new StringBuffer("User already exists in chat. Please restart client with different username.");
											s = buffer.toString();
											return;
										}
										else if(Server.users.get(i).equals(""))
										{
											randomColorSetter();
											Server.users.set(i, user);
											userIndex = i;
											//Server.saveUsers();
											System.out.println(user + " has joined the chat session.");
											buffer = new StringBuffer("You have joined the chat session.");
											s = buffer.toString();
											joined = true;
											break;
										}
									}
								}
							}
						}
					}catch(Exception e)
					{
						System.err.println("ERROR. Something went wrong when we tried to add a user.");
					}

			}catch(Exception e){
				System.err.println("ERROR. Something went wrong when we tried to add a user.");
			}
		}
	}
	public Socket getSocket()
	{
		return this.socket;
	}
}