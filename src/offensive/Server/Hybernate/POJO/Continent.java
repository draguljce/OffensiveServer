package offensive.Server.Hybernate.POJO;

public class Continent {
	int id;
	String name;
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
	public short getBonus() {
		return bonus;
	}
	public void setBonus(short bonus) {
		this.bonus = bonus;
	}
	short bonus;
}
