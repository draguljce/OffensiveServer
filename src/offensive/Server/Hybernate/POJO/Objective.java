package offensive.Server.Hybernate.POJO;

public class Objective {
	private int id;
	private String description;
	
	public Objective() {};
	
	public Objective(int id) {
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
}
