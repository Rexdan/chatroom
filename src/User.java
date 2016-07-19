import java.util.ArrayList;

public class User extends ArrayList<User>
{
	private String name;
	private String color = "";
	private String endingColor = "\033[0m";
	private boolean exiting = false;
	public boolean pc = false;
	private int senderSessionIndex;

	public void setExiting(boolean exiting)
	{
		this.exiting = exiting;
	}
	
	public void setSenderSessionIndex(int senderSessionIndex)
	{
		this.senderSessionIndex = senderSessionIndex;
	}
	
	public int getSenderSessionIndex()
	{
		return this.senderSessionIndex;
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

		if(o == null)
		{
			return false;
		}

		else if(o instanceof User)
		{
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