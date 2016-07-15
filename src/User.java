import java.util.ArrayList;

public class User extends ArrayList<User>
{
	private String name;
	private String color;
	private String endingColor = "\033[0m";
	private boolean exiting = false;
	public boolean pc = false;
	private int senderIndex;

	public void setExiting(boolean exiting)
	{
		this.exiting = exiting;
	}
	
	public void setSenderIndex(int senderIndex)
	{
		this.senderIndex = senderIndex;
	}
	
	public int getSenderIndex()
	{
		return this.senderIndex;
	}

	public boolean isExiting()
	{
		return exiting;
	}

	public User()
	{
		this.name = "";
	}

	public User(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	public void setColor(String color)
	{
		this.color = color;
	}

	public String getColor()
	{
		return this.color;
	}

	@Override
	public boolean equals(Object o)
	{
		User user = new User();

		//System.out.println("In equals method.");

		if(o == null)
		{
			//System.out.println("Object is null.");
			return false;
		}

		else if(o instanceof User)
		{
			//System.out.println("Object is an instance of User.");
			user = (User) o;
		}

		if(this.name.equals(user.name)) return true;

		else return false;
	}

	@Override
	public String toString()
	{
		return this.color.concat(this.name).concat(this.endingColor);
	}
}