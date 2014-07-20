package offensive.Server.Hybernate.POJO;

public class Connection {
	private int id;
	
	private Field field1;
	private Field field2;
	
	private long version;
	
	public Connection() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Field getField1() {
		return field1;
	}

	public void setField1(Field field1) {
		this.field1 = field1;
	}

	public Field getField2() {
		return field2;
	}

	public void setField2(Field field2) {
		this.field2 = field2;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	};
}