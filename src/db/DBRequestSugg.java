package db;

import java.util.ArrayList;
import java.util.List;

public class DBRequestSugg {
	private int idUser;
	private List<DBOsservazione> osservazioni;

	public DBRequestSugg(){
		osservazioni = new ArrayList<DBOsservazione>();
	}

	public DBRequestSugg(int idUser){
		this.idUser = idUser;
		osservazioni = new ArrayList<DBOsservazione>();
	}

	public int getId() {
		return idUser;
	}

	public List<DBOsservazione> getOsservazioni() {
		return osservazioni;
	}

	public void addOsservazione(DBOsservazione oss) {
		this.osservazioni.add(oss);
	}

}
