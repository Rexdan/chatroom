import	java.util.*;
import java.util.concurrent.TimeUnit;
import	java.io.*;
import	java.net.*;

public class SessionThread extends Thread {

	private Socket	socket;
	
	private User user;

	private static boolean closed = false;

	private String message;
	BufferedReader	fromClient;
	PrintWriter	toClient;
	String	s;
	String	name = "";
	String command = "";
	StringBuffer buffer;
	boolean cameFromExit = false;
	boolean joined = false;
	boolean firstRun = true;
	boolean badRun = false;
	boolean inPrivateSession;
	boolean weird = false;
	int userIndex = 0;
	int sessionIndex;
	int pcs = 0;

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
	
	public void run()
	{
		try {
			fromClient = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );

			toClient = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ), true );
			
			addUser(fromClient);
			
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
			
			//Printing chat history on Client side.
			toClient.println(Server.messages.size());
			if(!Server.messages.isEmpty())
			{
				for(int i = 0; i < Server.messages.size(); i++)
				{
					toClient.println(Server.messages.get(i));
				}
			}
			
			while ( (s = fromClient.readLine()) != null )
			{	
				if(s == null)
				{
					socket.close();
					Server.users.set(userIndex, new User());
					return;
				}
				
				if(s.length() == 0) continue;
				
				buffer = new StringBuffer( s );

				command = buffer.toString();

				char c = command.charAt(0);
				command = command.substring(1);
				
				if(pcs == 0) inPrivateSession = false;

				if(c == '@')
				{	
					/*
					 * If a user exists, then these commands can be carried out.
					 */
					if(user != null)
					{
						if(command.equalsIgnoreCase("who"))
						{
							listUsers();
							continue;
						}
						else if(command.equalsIgnoreCase("exit"))
						{
							cameFromExit = true;
							broadcast(message, cameFromExit);
							System.out.println(user + " has exited the chat session.");
							socket.close();
							closed = true;
							return;
						}
						try
						{
							if(command.substring(0, 3).equals("end"))
							{
								endPrivateChat();
								continue;
							}
							else if(command.substring(0, 4).equals("name"))
							{
								toClient.println("You may not change your username.");
								continue;
							}
							else if(command.substring(0,7).equalsIgnoreCase("private"))
							{
								privateChat();
								continue;
							}
						}catch(Exception e)
						{
							
						}

					toClient.println("ERROR. This is not a command.");
					continue;
					}
				}
				else
				{
					message = user.toString();
					message = message.concat(": ").concat(buffer.toString());
					broadcast(message,cameFromExit);
				}
			}
		}
		catch ( Exception e )
		{
			closed = true;
			System.err.println( "The client has terminated prematurely..." + e.toString() );
		}
	}
	
	public void write(String input)
	{
		toClient.println(input);
	}
	
	public void receiverExited(int sessionIndex)
	{
		String msg = Server.sessions.get(sessionIndex).getUser() + " has exited the chat session.";
		Server.sessions.get(sessionIndex).getUser().pc = false;
		toClient.println(msg);
		pcs--;
	}
	
	private void broadcast(String message, boolean userExited) throws IOException
	{
		synchronized(this)
		{
			if(userExited)
			{
				message = user + " has exited the chat session.";
				Server.saveMessage(message);
				Server.saveHistory();
				
				if(this.user.pc)
				{
					Server.sessions.get(this.user.getSenderSessionIndex()).receiverExited(this.sessionIndex);
				}
				
				for(int i = 0; i < Server.sessions.size(); i++)
				{
					if(Server.sessions.get(i).getUser().pc == true && Server.sessions.get(i).getUser().getSenderSessionIndex() == this.sessionIndex)
					{
						Server.sessions.get(i).getUser().pc = false;
					}
					Server.sessions.get(i).write(message);
				}
				
				Server.users.set(userIndex, new User());
				Server.sessions.set(this.sessionIndex, null);
			}
			
			if(inPrivateSession)
			{
				for(int i = 0; i < Server.sessions.size(); i++)
				{
					if(Server.sessions.get(i) != null && Server.sessions.get(i).getUser() != null && Server.sessions.get(i).getUser().pc == true && Server.sessions.get(i).getUser().getSenderSessionIndex() == (this.sessionIndex))
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
					if(Server.sessions.get(i) != null && Server.sessions.get(i).getUser() != null && Server.sessions.get(i).inPrivateSession == false && !Server.sessions.get(i).getUser().equals(this.user))
					{
						Server.sessions.get(i).write(message);
					}
				}
			}
		}
	}
	
	public User getUser()
	{
		return this.user;
	}
	
	private void randomColorSetter()
	{
		synchronized(this)
		{
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
		//from 40 to 47
		String fourthPart = "";
		int forFourthPart = 40 + rn.nextInt(47 - 40 + 1);
		
		while(true)
		{
			/*
			 * If they're the same, then we can't see the text!
			 */
			if((forThirdPart + 10) != forFourthPart)
			{
				break;
			}
			else
			{
				forThirdPart = 30 + rn.nextInt(37 - 30 + 1);
				forFourthPart = 40 + rn.nextInt(47 - 40 + 1);
			}
		}
		
		thirdPart = thirdPart.concat(Integer.toString(forThirdPart));
		thirdPart = thirdPart.concat(";");
		resultingColor = resultingColor.concat(thirdPart);
		
		fourthPart = fourthPart.concat(Integer.toString(forFourthPart));
		fourthPart = fourthPart.concat("m");
		resultingColor = resultingColor.concat(fourthPart);
		
		for(int i = 0; i < Server.users.size(); i++)
		{
			if(Server.users.get(i).getColor().equals(resultingColor))
			{
				randomColorSetter();
			}
		}
		user.setColor(resultingColor);
		}
	}
	
	private void addUser(BufferedReader fromClient) throws IOException
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
											buffer = new StringBuffer("User already exists in chat. Please restart client with different username.");
											s = buffer.toString();
											return;
										}
										else if(Server.users.get(i).equals(""))
										{
											randomColorSetter();
											Server.users.set(i, user);
											userIndex = i;
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
	
	private void privateChat()
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
				return;
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
					return;
				}
				
				User otherUser = new User(command.substring(8));
				
				if(otherUser.equals(this.user))
				{
					toClient.println("Why would you want to private chat with yourself?");
					return;
				}
				
				int count = 0;
				int i = 0;
				int tries = 10;
				
				while(i < Server.sessions.size())
				{
					if(Server.sessions.get(i) != null && Server.sessions.get(i).getUser().equals(otherUser))
					{
						//To have a reference to the FULL user object, not just name.
						otherUser = Server.sessions.get(i).getUser();
						if(Server.sessions.get(i).getUser().pc == true)
						{
							//if(count == 11) return;
							TimeUnit.SECONDS.sleep(1);
							if(count == 10)
							{
								toClient.println("Could not initiate private chat with user. Please try again later.");
								return;
							}
							else if(count == 9)
							{
								toClient.println(otherUser + " is currently in a private chat session. " + "Retrying " + (tries - count) + " more time...");
							}
							else toClient.println(otherUser + " is currently in a private chat session. " + "Retrying " + (tries - count) + " more times...");
							
							count++;
							continue;
						}
						else
						{
							//Directly setting their status in arraylist.
							Server.sessions.get(i).getUser().pc = true;
							//We want a reference to the user/session that THIS user is allowed to send messages to.
							Server.sessions.get(i).getUser().setSenderSessionIndex(this.sessionIndex);
							toClient.println("You are now in a PRIVATE chat session with: " + otherUser);
							Server.sessions.get(i).write(getUser() + " has started to private chat with you.");
							Server.sessions.get(i).write("If you wish to send a message back to the user, please type '@private " + getUser() + "' and hit enter." + "\n" + "It will initiate a PRIVATE chat session with the sender.");
							inPrivateSession = true;
							pcs++;
							return;
						}
					}
					i++;
				}
				toClient.println(command.substring(8) + " does not exist in this chat session.");
			}
		}catch(Exception e)
		{
			
		}
	}
	
	private void endPrivateChat()
	{
		try
		{
			if(command.equalsIgnoreCase("end"))
			{
				buffer = new StringBuffer("You must specify a user to terminate the private conversation.");
				toClient.println(buffer.toString());
				return;
			}
			else if(command.substring(0, 4).equals("end "))
			{
				if(command.substring(4).length() == 0)
				{
					buffer = new StringBuffer("You must specify a user to terminate the private conversation.");
					toClient.println(buffer.toString());
					return;
				}
				
				User otherUser = new User(command.substring(4));
				
				if(otherUser.equals(this.user))
				{
					toClient.println("Why would you want to terminate the private chat with yourself?");
					return;
				}
				else
				{
					int i = 0;
					
					synchronized (this)
					{
						while(i < Server.sessions.size())
						{
							if(Server.sessions.get(i) != null && Server.sessions.get(i).getUser().equals(otherUser))
							{
								//To have a reference to the FULL user object, not just name.
								otherUser = Server.sessions.get(i).getUser();
								if(Server.sessions.get(i).getUser().getSenderSessionIndex() != this.sessionIndex)
								{
									toClient.println("You are not in a private chat session with " + otherUser);
									return;
								}
								else if(Server.sessions.get(i).getUser().pc == false)
								{
									toClient.println("You cannot terminate the private chat session with " + otherUser + " because that user is not currently set to receive any private messages...");
									return;
								}
								else if(Server.sessions.get(i).getUser().pc == true && Server.sessions.get(i).getUser().getSenderSessionIndex() == this.sessionIndex)
								{
									pcs--;
									if(pcs == 0) inPrivateSession = false;
									Server.sessions.get(i).getUser().pc = false;
									toClient.println("You have terminated your private chat session with: " + otherUser);
									Server.sessions.get(i).write(user + " has terminated their private session with you.");
									return;
								}
							}
							i++;
						}
						toClient.println(command.substring(4) + " does not exist in this chat session.");
					}
				}
			}
		}catch(Exception e)
		{
			System.err.println("Lost connection to the Client...");
		}
	}
	
	private void listUsers()
	{
		synchronized(this)
		{
			String active = "List of Active Users: ";
			String targeted = "List of Active Users who are being targeted for private messages";
			boolean first = true;
			boolean hadPriv = false;

			for(int i = 0; i < Server.users.size(); i++)
			{
				if(!Server.users.get(i).getName().equals(""))
				{
					if(first && Server.users.get(i).pc == false)
					{
						active = active.concat(Server.users.get(i).toString());
						first = false;
					}
					else if(Server.users.get(i).pc)
					{
						if(first)
						{
							targeted = targeted.concat(Server.users.get(i).toString());
							first = false;
						}
						else
						{
							targeted = targeted.concat(", " + Server.users.get(i).toString());
						}
						hadPriv = true;
					}
					else
					{
						active = active.concat(", " + Server.users.get(i).toString());
					}
				}
			}
			if(hadPriv)
			{
				targeted = targeted.concat(".");
				buffer = new StringBuffer(targeted);
				toClient.println(buffer.toString());
			}
			active = active.concat(".");
			buffer = new StringBuffer(active);
			toClient.println(buffer.toString());
		}
	}
	
	public int getSessionIndex()
	{
		return this.sessionIndex;
	}
	
	public Socket getSocket()
	{
		return this.socket;
	}
}