import java.util.ArrayList;

public class User extends ArrayList<User>
{
	public enum Type
	{
		PRIVATE, NORMAL;
	}

	private String name;
	private String color;

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

		System.out.println("In equals method.");

		if(o == null)
		{
			System.out.println("Object is null.");
			return false;
		}

		else if(o instanceof User)
		{
			System.out.println("Object is an instance of String.");
			user = (User) o;
		}

		if(this.name.equals(user.name)) return true;

		else return false;
	}

	@Override
	public String toString()
	{
		return this.name;
	}
}