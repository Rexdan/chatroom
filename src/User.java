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
}
