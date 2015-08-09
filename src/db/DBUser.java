package db;

import java.util.ArrayList;
import java.util.List;

public class DBUser {
	private int id;
	private String name;
	private Integer age;
	private String gender;
	private List<DBArtist> artists = null;

	public DBUser(){}
	
	public DBUser(int id, String name) {
		super();
		this.id = id;
		this.name = name;
		this.age = null;
		this.gender = null;
	}
	
	public DBUser(int id, String name, int age, String gender) {
		super();
		this.id = id;
		this.name = name;
		if (age <= 0)
			this.age = null;
		else
			this.age = age;
		
		if (gender == "")
			this.gender = null;
		else
			this.gender = gender;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Integer getAge() {
		return age;
	}

	public String getGender() {
		return gender;
	}

	public List<DBArtist> getArtists() {
		return artists;
	}

	public void addArtists(DBArtist artist) {
		if (artists == null)
			artists = new ArrayList<DBArtist>();
		artists.add(artist);
	}
}
