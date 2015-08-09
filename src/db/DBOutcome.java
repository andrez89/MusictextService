package db;

public class DBOutcome {
	private final int id;
	private final String name;

	public DBOutcome(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}