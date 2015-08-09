package db;

import java.util.ArrayList;
import java.util.List;

public class DBSuggerimento {
	private List<DBTag> tagSuggeriti;
	
	public DBSuggerimento(){
		tagSuggeriti = new ArrayList<DBTag>();
	}

	public List<DBTag> getTagSuggeriti() {
		return tagSuggeriti;
	}

	public void addTag(DBTag sugg) {
		this.tagSuggeriti.add(sugg);
	}
	
	
}
