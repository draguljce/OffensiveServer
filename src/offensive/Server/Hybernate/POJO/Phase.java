package offensive.Server.Hybernate.POJO;

public class Phase {
	private int id;
	private String name;
	
	public Phase() {};
	
	public Phase(int id) {
		this.id = id;
	};
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int nextPhaseId() {
		int nextId = (this.id + 1) % 5;
		
		if(nextId == 0) {
			nextId++;
		}
		
		return nextId;
	}
}
