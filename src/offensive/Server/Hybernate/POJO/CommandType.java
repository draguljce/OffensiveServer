package offensive.Server.Hybernate.POJO;

public class CommandType {
	private int id;
	private String name;
	
	public CommandType() {};
	
	public CommandType(int id) {
		this.id = id;
	};
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}	
}
