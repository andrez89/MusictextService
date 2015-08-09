package db;

public class DBTag {
	private int id;
	private String name = null;

	public DBTag(){}
	public DBTag(int id){ this.id = id; }

	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
