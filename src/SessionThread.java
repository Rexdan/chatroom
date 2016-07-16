import	java.util.*;
import	java.io.*;
import	java.net.*;

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
	boolean inPrivateSession = false;
	public ArrayList<SessionThread> privateSessions = new ArrayList<SessionThread>();
	int userIndex = 0;
	int pcs = 0;
	
	public void run()
	{
		try {
			
			//Server.loadHistory();
			
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
				
				if(s.length() == 0) continue;
				
				buffer = new StringBuffer( s );

				String command = buffer.toString();

				char c = command.charAt(0);
				command = command.substring(1);

				if(c == '@')
				{	
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
								if(command.substring(8).length() == 0)
								{
									buffer = new StringBuffer("You must specify a user to initiate the private conversation.");
									toClient.println(buffer.toString());
									continue;
								}
								
								User otherUser = new User(command.substring(8));
								
								if(otherUser.equals(this.user))
								{
									toClient.println("Why would you want to private chat with yourself?" + "\n" + "Going back to Public Chat...");
									continue;
								}
								
								int count = 0;
								int i = 0;
								
								while(i < Server.sessions.size())
								{
									if(Server.sessions.get(i).getUser().equals(otherUser))
									{
										//To have a reference to the FULL user object, not just name.
										otherUser = Server.sessions.get(i).getUser();
										if(Server.sessions.get(i).getUser().pc == true && count <= 10)
										{
											int currentCount = count + 10 + count--;
											toClient.println(otherUser + " is currently in a private chat session. " + "Retrying " + (currentCount) + " more times.");
											count++;
											continue;
										}
										else
										{
											//Directly setting their status in arraylist.
											Server.sessions.get(i).getUser().pc = true;
											//We want a reference to the user/session that THIS user is allowed to send messages to.
											Server.sessions.get(i).getUser().setSenderIndex(this.userIndex);
											toClient.println("You are now in a PRIVATE chat session with: " + otherUser);
											Server.sessions.get(i).write(getUser() + " has started to private chat with you.");
											inPrivateSession = true;
											pcs++;
											break;
										}
									}
									i++;
								}
								badCommand = false;
								continue;
							}
						}catch(Exception e)
						{
							
						}
						
						try
						{
							if(command.equalsIgnoreCase("end"))
							{
								buffer = new StringBuffer("You must specify a user to terminate the private conversation.");
								toClient.println(buffer.toString());
								continue;
							}
							else if(command.substring(0, 4).equals("end "))
							{
								if(command.substring(4).length() == 0)
								{
									buffer = new StringBuffer("You must specify a user to terminate the private conversation.");
									toClient.println(buffer.toString());
									continue;
								}
								
								User otherUser = new User(command.substring(4));
								
								if(otherUser.equals(this.user))
								{
									toClient.println("Why would you want to terminate the private chat with yourself?" + "\n" + "Going back to Public Chat...");
									continue;
								}
								else
								{
									int i = 0;
									
									synchronized (this)
									{
										while(i < Server.sessions.size())
										{
											if(Server.sessions.get(i).getUser().equals(otherUser))
											{
												//To have a reference to the FULL user object, not just name.
												otherUser = Server.sessions.get(i).getUser();
												if(Server.sessions.get(i).getUser().getSenderIndex() != this.userIndex)
												{
													toClient.println("You are not in a private chat session with " + otherUser);
													break;
												}
												else if(Server.sessions.get(i).getUser().pc == false)
												{
													toClient.println("You cannot terminate the private chat session with " + otherUser + " because that user is not currently set to receive any private messages...");
													break;
												}
												else if(Server.sessions.get(i).getUser().pc == true && Server.sessions.get(i).getUser().getSenderIndex() == this.userIndex)
												{
													pcs--;
													Server.sessions.get(i).getUser().pc = false;
													toClient.println("You have terminated your private chat session with: " + otherUser);
													Server.sessions.get(i).write(user + " has terminated their private session with you.");
													break;
												}
											}
											i++;
										}
									}
									badCommand = false;
									continue;
								}
							}
						}catch(Exception e)
						{
							
						}

						//finally
						//{
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
								broadcast(message, cameFromExit);
								//exit();
								System.out.println(user + " has exited the chat session.");
								socket.close();
								closed = true;
								badCommand = false;
								return;
							}
							
						//}
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
						broadcast(message,cameFromExit);
						/*synchronized(this)
						{
							Server.saveMessage(message);
							
							for(int i = 0; i < Server.sessions.size(); i++)
							{
								if(Server.sessions.get(i) != null && Server.sessions.get(i).getUser() != null)
								{
									Server.sessions.get(i).write(Server.getMessage());
								}
							}
							Server.incrCounter();
						}*/
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
	
	public void receiverExited(int userIndex)
	{
		String msg = Server.users.get(userIndex) + " has exited the chat session.";
		toClient.println(msg);
		pcs--;
	}
	
	public void broadcast(String message, boolean blah) throws IOException
	{
		synchronized(this)
		{
			if(pcs == 0)
			{
				//System.out.println("NO LONGER IN PRIVATE SESSION");
				inPrivateSession = false;
			}
			if(blah)
			{
				String exitString = user + " has exited the chat session.";
				Server.saveMessage(exitString);
				Server.saveHistory();
				Server.users.set(userIndex, new User());
				if(this.user.pc)
				{
					for(int i = 0; i < Server.sessions.size(); i++)
					{
						if(Server.sessions.get(i).userIndex == (this.user.getSenderIndex()))
						{
							Server.sessions.get(i).receiverExited(this.userIndex);
							//Server.sessions.get(i).write(Server.getMessage());
						}
					}
				}
			}
			
			if(inPrivateSession)
			{
				for(int i = 0; i < Server.sessions.size(); i++)
				{
					if(Server.sessions.get(i) != null && Server.sessions.get(i).getUser() != null && Server.sessions.get(i).getUser().pc == true && Server.sessions.get(i).getUser().getSenderIndex() == (this.userIndex))
					{
						Server.sessions.get(i).write(message);
					}
				}
			}
			else
			{

				Server.saveMessage(message);
				Server.saveHistory();
				
				for(int i = 0; i < Server.sessions.size(); i++)
				{
					if(Server.sessions.get(i) != null && Server.sessions.get(i).getUser() != null && Server.sessions.get(i).inPrivateSession == false)
					{
						Server.sessions.get(i).write(Server.getMessage());
					}
				}
				Server.incrCounter();
			}
		}
	}
	
	public User getUser()
	{
		return this.user;
	}

	public void exit()
	{

		for(int i = 0; i < Server.users.size(); i++)
		{
			if(Server.users.get(i).equals(user))
			{
				Server.users.set(i, new User());
				//Server.saveUsers();
				break;
			}
		}
		System.out.println(user + " has disconnected.");
		
		buffer = new StringBuffer(message);
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