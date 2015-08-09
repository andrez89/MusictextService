package db;

import java.util.ArrayList;
import java.util.List;

public class DBNetwork {
	private final int id;
	private final String name;
	private List<DBNode> nodes = null;

	public DBNetwork(int id, String name) {
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

	public List<DBNode> getNodes() {
		return nodes;
	}

	public void addNodes(DBNode node) {
		if (nodes == null)
			this.nodes = new ArrayList<DBNode>();
		this.nodes.add(node);
	}

}
