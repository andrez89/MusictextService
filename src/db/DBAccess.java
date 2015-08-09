package db;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.utils.SystemProperty;

import java.sql.Statement;

public class DBAccess {
	private static String url = "jdbc:google:mysql://musictext-979:musictext1?user=root";
	private static Connection conn;

	private static void openConnection() throws SQLTimeoutException, InternalServerErrorException{
		try {
			if (SystemProperty.environment.value() ==
					SystemProperty.Environment.Value.Production) {
				Class.forName("com.mysql.jdbc.GoogleDriver");
			} else {
				Class.forName("com.mysql.jdbc.Driver");
				url = "jdbc:mysql://localhost:3306/musictext?user=root";
			} 
		} catch (ClassNotFoundException ex) {
			throw new InternalServerErrorException("Internal jdbc library not found", ex);
		}
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException ex){
			throw new SQLTimeoutException("DB not found");
		}
	}

	public static List<DBNetwork> getNetworks() 
			throws InternalServerErrorException, SQLTimeoutException {

		List<DBNetwork> nets = new ArrayList<DBNetwork>();
		openConnection();
		try {
			ResultSet rs = conn.createStatement().executeQuery(
					"SELECT id, network from musictext.B_Net;");
			while(rs.next()){
				nets.add(new DBNetwork(rs.getInt("id") , rs.getString("network")));
			}
			conn.close();
		} catch (SQLException e) {
			throw new InternalServerErrorException("Internal SQL Error", e);
		}
		return nets;
	}

	public static DBNetwork getFullNetworks(int idNet, int idUser)
			throws SQLTimeoutException, IllegalArgumentException, 
			InternalServerErrorException {

		DBNetwork net = null;
		openConnection();
		try{
			ResultSet rs = conn.createStatement().executeQuery(
					"SELECT network from musictext.B_Net where id = " + idNet + ";");
			if (rs.first())
				net = new DBNetwork(idNet, rs.getString("network"));
			else
				throw new IllegalArgumentException("Network with id " + idNet + " does not exists");

			String query = "SELECT id, node from musictext.B_Net_Node " + 
					" where id_net = " + idNet + " order by id;";
			String query2;
			ResultSet rs1 = conn.createStatement().executeQuery(query);
			if (! rs1.first())
				throw new InternalServerErrorException("Network with id " + idNet + " has no node");
			while(rs1.next()){
				DBNode n = new DBNode(rs1.getInt("id"), rs1.getString("node"));

				query2 = "Select id, outcome from musictext.B_Net_Outcome " +
						" where id_node = " + rs1.getInt("id")  + " order by position;";
				ResultSet rs2 = conn.createStatement().executeQuery(query2);

				if (! rs2.first())
					throw new InternalServerErrorException("Node with id " + n.getId() + " has no outcome");
				while(rs2.next()){
					n.addOutcome(new DBOutcome(rs2.getInt("id") , rs2.getString("outcome")));
				}

				query2 = "Select id_parent, node from musictext.B_Net_Parent p " +
						" inner join musictext.B_Net_Node n on id_parent=n.id " +
						" where id_node = " + rs1.getInt("id")  + " order by position;";
				rs2 = conn.createStatement().executeQuery(query2);
				while(rs2.next()){
					n.addParent(new DBNode(rs2.getInt("id_parent") , rs2.getString("node")));
				}

				query2 = "Select probability from musictext.B_Net_Prob " +
						" where id_node = " + rs1.getInt("id")  + " and id_user = " + 
						idUser  + " order by position;";
				rs2 = conn.createStatement().executeQuery(query2);

				if (! rs2.first())
					throw new InternalServerErrorException("Node with id " + n.getId() + " has no probability for user " + idUser);
				while(rs2.next()){
					n.addProbability(rs2.getDouble("probability"));
				}
				net.addNodes(n);
			}
			conn.close();
		} catch (SQLException e) {
			throw new InternalServerErrorException("Internal SQL Error", e);
		}
		return net;
	}

	public static List<DBNode> getNodesOutcomes(int idNet)
			throws SQLTimeoutException, IllegalArgumentException, 
			InternalServerErrorException{

		List<DBNode> nodes = new ArrayList<DBNode>();
		openConnection();
		String query = "SELECT id, node from musictext.B_Net_Node " + 
				" where id_net = " + idNet + ";";
		String query2;
		DBNode n;
		try {
			ResultSet rs = conn.createStatement().executeQuery(query);
			if (! rs.first())
				throw new IllegalArgumentException("Network with id " + idNet + " has no nodes");

			while(rs.next()){
				query2 = "Select id, outcome from musictext.B_Net_Outcome " +
						" where id_node = " + rs.getInt("id")  + " order by position;";
				n = new DBNode(rs.getInt("id") , rs.getString("node"));
				ResultSet rs2 = conn.createStatement().executeQuery(query2);

				if (! rs.first())
					throw new InternalServerErrorException("Network with id " + idNet + " has no nodes");
				while(rs2.next()){
					n.addOutcome(new DBOutcome(rs2.getInt("id") , rs2.getString("outcome")));
				}
				nodes.add(n);
			}
			conn.close();
		} catch (SQLException e) {
			throw new InternalServerErrorException("Internal SQL Error", e);
		}
		return nodes;
	}

	public static List<String> getNodes(int idNet)
			throws SQLTimeoutException, IllegalArgumentException, 
			InternalServerErrorException {

		List<String> nodes = new ArrayList<String>();
		openConnection();
		String query = "SELECT id, node from musictext.B_Net_Node " + 
				" where id_net = " + idNet + ";";

		ResultSet rs;
		try {
			rs = conn.createStatement().executeQuery(query);
			if (! rs.first())
				throw new IllegalArgumentException("Network with id " + idNet + " has no nodes");

			while(rs.next()){
				nodes.add(rs.getString("node"));
			}
			conn.close();
		} catch (SQLException e) {
			throw new InternalServerErrorException("Internal SQL Error", e);
		}
		return nodes;
	}

	public static List<String> getRules(int idNet, int idUser, boolean dynamic) 
			throws SQLTimeoutException, IllegalArgumentException,
			InternalServerErrorException{
		List<String> rules = new ArrayList<String>();
		openConnection();
		try {
			String str = "SELECT id, rule from musictext.Rules where id_net = " +
					idNet + " and (id_user = " + idUser + " and dynamic = ";
			str += dynamic ? "1" : "0";
			str += ")";
			ResultSet rs = conn.createStatement().executeQuery(str);
			if (! rs.first())
				throw new IllegalArgumentException("No rule for network " + idNet + " and user " + idUser);

			while(rs.next()){
				rules.add(rs.getString("rule"));
			}
			conn.close();
		} catch (SQLException e) {
			throw new InternalServerErrorException("Internal SQL Error", e);
		}
		return rules;
	}

	public static void setRules(int idNet, int idUser, List<String> rules) 
			throws SQLTimeoutException, IllegalArgumentException, 
			InternalServerErrorException{

		if (rules.size()==0 || rules.contains(null))
			throw new IllegalArgumentException("Rule list empty or invalid");

		openConnection();
		try {
			String queryDel = "delete from musictext.Rules where id_user = " + idUser + 
					" and id_net = " + idNet + " and dynamic = 1;";

			String queryIns = "insert into musictext.Rules (id_net,id_user,position,dynamic,rule) values ";

			for (int i = 0; i<rules.size(); i++)
				queryIns += new StringBuilder("(").append(idNet).append(",").append(idUser)
				.append(",").append(i).append(",1,'").append(rules.get(i)).append("'),");

			queryIns =  queryIns.substring(0, queryIns.length()-1) + ";";

			conn.createStatement().executeUpdate(queryDel);
			conn.createStatement().executeUpdate(queryIns);
			conn.close();
		} catch (SQLException e) {
			throw new InternalServerErrorException("Internal SQL Error", e);
		}
	}

	public static DBUser getUserInfo(String name)
			throws SQLTimeoutException, IllegalArgumentException, 
			InternalServerErrorException, NotFoundException {

		if (name.contains("'"))
			throw new IllegalArgumentException("User name contains special characters");

		return getUser("name = '" + name + "'");
	}

	public static DBUser getUserInfo(int id) 
			throws SQLTimeoutException, NotFoundException, 
			InternalServerErrorException{
		return getUser("id = " + id);
	}

	private static DBUser getUser(String filter) 
			throws SQLTimeoutException, NotFoundException, 
			InternalServerErrorException {

		DBUser user;
		openConnection();
		try {
			String query = "SELECT id, name, age, gender from musictext.User where " + filter + ";";
			ResultSet rs = conn.createStatement().executeQuery(query);
			if(rs.next()){
				user = new DBUser(rs.getInt("id"), rs.getString("name"),
						rs.getInt("age"), rs.getString("gender"));

				query = "SELECT a.id, a.name from musictext.Top_Artists a inner join " + 
						" musictext.User_Artists u on a.id = id_artist" +
						" where id_user = " + rs.getInt("id") + ";";
				ResultSet rs1 = conn.createStatement().executeQuery(query);
				while(rs1.next()){
					user.addArtists(new DBArtist(rs1.getInt("id"), rs1.getString("name")));
				}
			}else{
				throw new NotFoundException("User " + filter);
			}
			conn.close();
		} catch (SQLException e) {
			throw new InternalServerErrorException("Internal SQL Error", e);
		}
		return user;
	}

	public static List<DBUser> getUserList() 
			throws SQLTimeoutException, InternalServerErrorException{
		List<DBUser> users = new ArrayList<DBUser>();
		openConnection();
		try {
			String query = "SELECT id, name from musictext.User order by name;";
			ResultSet rs = conn.createStatement().executeQuery(query);
			if (rs.first())
				while(rs.next()){
					users.add(new DBUser(rs.getInt("id"), rs.getString("name")));
				}
			else {
				conn.close();
				throw new InternalServerErrorException("No user in the DB");
			}
			conn.close();
		} catch (SQLException e) {
			throw new InternalServerErrorException("Internal SQL Error", e);
		}
		return users;
	}

	/**
	 * Adds the user with <i>name</i> to the database  
	 * @param name - The name of the user to be added
	 * @return The id of the user in the database.
	 * @throws InternalServerErrorException 
	 * @throws SQLTimeoutException 
	 * @throws NotFoundException 
	 * @throws Exception 
	 */
	public static int addUser(String name) 
			throws SQLTimeoutException, IllegalArgumentException,
			InternalServerErrorException {

		if (name.contains("'"))
			throw new IllegalArgumentException("User name contains special characters");

		openConnection();
		try {
			String query = "call musictext.SP_new_user ('" + name + "');";
			ResultSet rs = conn.createStatement().executeQuery(query);
			int id;
			if(rs.next()){
				id = rs.getInt(1);
				conn.close();

				if (id == -1)
					throw new IllegalArgumentException("User already exists");
				return id;
			} else {
				conn.close();
				throw new InternalServerErrorException("SQL procedure internal error");
			}
		} catch (SQLException e) {
			throw new InternalServerErrorException("Internal SQL Error\n" + e.getMessage(), e);
		}
	}

	public static void editUser(DBUser user) 
			throws SQLTimeoutException, InternalServerErrorException {

		openConnection();
		try {
			Statement stmnt = conn.createStatement();
			String query = "";

			if (user.getAge() != null) {
				query = "update musictext.User SET age = " + user.getAge()
						+ " where id = " + user.getId() + ";";
				stmnt.addBatch(query);
			}
			if (user.getGender() != null){
				query = "update musictext.User SET gender ='" + user.getGender() 
						+ "' where id = " + user.getId() + ";";
				stmnt.addBatch(query);
			}

			query = "delete from musictext.User_Artists where id_user = " + user.getId() + ";";
			stmnt.addBatch(query);

			if (user.getArtists() != null && user.getArtists().size()>0)
				for (DBArtist a:user.getArtists()){
					query = "insert into musictext.User_Artists (id_artist,id_user) values (" + 
							a.getId() + ", " + user.getId() + ");";
					stmnt.addBatch(query);
				} else {
					throw new IllegalArgumentException("At least an artist in the list");
				}
			stmnt.executeBatch();
			conn.close();
		} catch (SQLException e) {
			throw new InternalServerErrorException("Internal SQL Error", e);
		}
	}

	public static List<Integer> getTags(String tagCluster) 
			throws SQLTimeoutException, IllegalArgumentException, 
			InternalServerErrorException, NotFoundException {

		List<Integer> tags = new ArrayList<Integer>();

		if (tagCluster == null || tagCluster== "" || tagCluster.contains("'"))
			throw new IllegalArgumentException("Non valid tag cluster");
		String query = "";
		openConnection();
		try {
			query = "SELECT id_tag from musictext.Cluster_Tag " + 
					" where cluster_tag like '" + tagCluster + "';";
			ResultSet rs = conn.createStatement().executeQuery(query);

			if (rs.first())
				while(rs.next())
					tags.add(rs.getInt("id_tag"));
			else
				throw new NotFoundException("Cluster tag " + tagCluster + "\n" + query);

			conn.close();
		} catch (SQLException e) {
			throw new InternalServerErrorException("Internal SQL Error", e);
		}
		return tags;
	}

	public static boolean isConnected() 
			throws SQLTimeoutException, InternalServerErrorException {
		openConnection();
		try {
			conn.close();
		} catch (SQLException e) { }
		return true;
	}
}
