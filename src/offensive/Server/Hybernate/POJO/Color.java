package offensive.Server.Hybernate.POJO;

public class Color {
	private int id;
	private String name;
	
	public Color() {};
	
	public Color(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}