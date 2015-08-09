package testService;

import java.io.IOException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.*;
import org.junit.runner.RunWith;

import com.google.api.server.spi.response.NotFoundException;
import com.google.gson.Gson;

import service.SuggestServlet;
import service.Utils;
import db.DBAccess;
import db.DBArtist;
import db.DBOsservazione;
import db.DBRequestSugg;
import db.DBSuggerimento;
import db.DBTag;
import db.DBUser;
import mockit.*;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class SuggestTest {
	@Mocked
	HttpServletRequest mockHttpServletRequest;
	@Mocked
	HttpServletResponse mockHttpServletResponse;

	DBUser testNoArtist, testUserManyArtist;
	DBRequestSugg testWrong1Request, testWrong2Request, testCorrectRequest;
	String jsonNoArtist, jsonUserManyArtist, tagCluster;
	String jsonWrong1Request, jsonWrong2Request, jsonCorrectRequest, jsonResponse, jsonResponse2;
	List<String> dynRules, staRules;
	List<Integer> tagContestuali, tagContestuali2;
	DBSuggerimento tagFinali;

	public SuggestTest(){
		super();
		Gson gson = new Gson();

		// prepare mock Requests
		testWrong1Request = new DBRequestSugg(2);
		testWrong1Request.addOsservazione(new DBOsservazione("temperatura", "5_15"));
		jsonWrong1Request = gson.toJson(testWrong1Request);
		jsonWrong1Request = jsonWrong1Request.replace("[", "");

		testWrong2Request = new DBRequestSugg(2);
		testWrong2Request.addOsservazione(new DBOsservazione("temperatura", "5_15"));
		testWrong2Request.addOsservazione(new DBOsservazione("Condizioni_meteo", "Temporale"));
		testWrong2Request.addOsservazione(new DBOsservazione("mezzo", "Auto"));
		testWrong2Request.addOsservazione(new DBOsservazione("Km_to_run", "10_30"));
		testWrong2Request.addOsservazione(new DBOsservazione("Momento_viaggio", "Giorno_feriale"));

		jsonWrong2Request = gson.toJson(testWrong2Request);

		testCorrectRequest = new DBRequestSugg(2);
		testCorrectRequest.addOsservazione(new DBOsservazione("Temperatura", "5_15"));
		testCorrectRequest.addOsservazione(new DBOsservazione("Condizioni_meteo", "Pioggia"));
		testCorrectRequest.addOsservazione(new DBOsservazione("Mezzo", "Moto"));
		testCorrectRequest.addOsservazione(new DBOsservazione("Km_to_run", "maj100"));
		testCorrectRequest.addOsservazione(new DBOsservazione("Momento_viaggio", "Notte_festivo"));

		jsonCorrectRequest = gson.toJson(testCorrectRequest);

		// prepare Mock DB users
		testNoArtist = new DBUser(2,"carlo");
		jsonNoArtist = gson.toJson(testNoArtist);

		testUserManyArtist = new DBUser(2,"carlo");
		testUserManyArtist.addArtists(new DBArtist(1, "883"));
		testUserManyArtist.addArtists(new DBArtist(2, "Queen"));
		testUserManyArtist.addArtists(new DBArtist(3, "Edoardo Bennato"));
		testUserManyArtist.addArtists(new DBArtist(4, "Pooh"));
		jsonUserManyArtist = gson.toJson(testUserManyArtist);

		// prepare mock rules
		tagCluster = "Ballabile";
		dynRules = new ArrayList<String>();
		dynRules.add("(defrule DepositoCluster  (Result_Tag_cluster (Tag_cluster ?tc) (Reliability ?r)) => (store \"Tag_cluster\" ?tc))");
		dynRules.add("(deftemplate Result_Mood_meteo (slot Mood_meteo) (slot Reliability) )");
		dynRules.add("(deftemplate Result_Travel_mood (slot Travel_mood) (slot Reliability) )");
		dynRules.add("(deftemplate Result_Tag_cluster (slot Tag_cluster) (slot Reliability) )");

		dynRules.add("(defrule Tag_clusterBallabile5706 (Temperatura 5_15)  (Condizioni_meteo Pioggia) "+
				"(Mezzo Moto)  (Km_to_run maj100)  (Momento_viaggio Notte_festivo) => " +
				"(assert (Result_Tag_cluster (Tag_cluster Ballabile) (Reliability 0.5308778496087105))) )");

		dynRules.add("(defrule Mood_meteoTriste1866 (Temperatura 5_15)  (Condizioni_meteo Pioggia) "+
				"(Mezzo Moto)  (Km_to_run maj100)  (Momento_viaggio Notte_festivo) =>" +
				"(assert (Result_Mood_meteo (Mood_meteo Triste) (Reliability 0.5384615384615385))) )");

		dynRules.add("(defrule Tag_clusterBallabile5706 (Temperatura 5_15)  (Condizioni_meteo Pioggia) "+
				"(Mezzo Moto)  (Km_to_run maj100)  (Momento_viaggio Notte_festivo) => " +
				"(assert (Result_Tag_cluster (Tag_cluster Ballabile) (Reliability 0.5308778496087105))) )");

		staRules = new ArrayList<String>();
		staRules.add("(defrule ArtistToArtistRule1 (Artist 2) => (assert (Tag 14,15,104,374,507,559,2143))) ");

		// prepare mock tag from cluster 
		tagContestuali = new ArrayList<Integer>();
		tagContestuali.add(150);
		tagContestuali.add(149);
		tagContestuali.add(1024);
		tagContestuali.add(15);
		tagContestuali.add(304);
		
		tagContestuali2 = new ArrayList<Integer>();
		tagContestuali2.add(150);
		tagContestuali2.add(149);
		tagContestuali2.add(1024);
		tagContestuali2.add(304);

		tagFinali = new DBSuggerimento();
		tagFinali.addTag(new DBTag(15));
		jsonResponse = gson.toJson(tagFinali);

		tagFinali = new DBSuggerimento();
		tagFinali.addTag(new DBTag(14));
		tagFinali.addTag(new DBTag(150));
		jsonResponse2 = gson.toJson(tagFinali);
	}

	@Test  
	public void testSuggestSuccess() throws IOException {
		new MockUp<Utils>() {
			@Mock
			private String readText(HttpServletRequest request) {
				return jsonCorrectRequest; 
			}
		};

		new MockUp<DBAccess>() {
			@Mock
			public DBUser getUserInfo(int id){
				Assert.assertEquals(testUserManyArtist.getId(), id);
				return testUserManyArtist;
			}
			@Mock
			public List<String> getRules(int idNet, int idUser, boolean dynamic){
				Assert.assertEquals(testUserManyArtist.getId(), idUser);
				if (dynamic)
					return dynRules; 
				else
					return staRules;
			}
			@Mock
			public List<Integer> getTags(String cluster){
				Assert.assertEquals(cluster, tagCluster);
				return tagContestuali;
			}
		};

		new Expectations() {
			{
				//mockHttpServletRequest.getContentType(); returns(Utils.jsonType);

				mockHttpServletResponse.setContentType(Utils.jsonType);
				mockHttpServletResponse.addHeader("cluster", tagCluster);
				mockHttpServletResponse.getWriter().print(jsonResponse);
			}
		};

		SuggestServlet serv = new SuggestServlet();
		try {
			serv.doPost(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test  
	public void testSuggestNoIntersectionSuccess() throws IOException {
		new MockUp<Utils>() {
			@Mock
			private String readText(HttpServletRequest request) {
				return jsonCorrectRequest; 
			}
		};

		new MockUp<DBAccess>() {
			@Mock
			public DBUser getUserInfo(int id){
				Assert.assertEquals(testUserManyArtist.getId(), id);
				return testUserManyArtist;
			}
			@Mock
			public List<String> getRules(int idNet, int idUser, boolean dynamic){
				Assert.assertEquals(testUserManyArtist.getId(), idUser);
				if (dynamic)
					return dynRules; 
				else
					return staRules;
			}
			@Mock
			public List<Integer> getTags(String cluster){
				Assert.assertEquals(cluster, tagCluster);
				return tagContestuali2;
			}
		};

		new Expectations() {
			{
				//mockHttpServletRequest.getContentType(); returns(Utils.jsonType);

				mockHttpServletResponse.setContentType(Utils.jsonType);
				mockHttpServletResponse.addHeader("cluster", tagCluster);
				mockHttpServletResponse.getWriter().print(jsonResponse2);
			} //"[{\"tagSuggeriti\":[{\"id\":14},{\"id\":150}]}]"
		};

		SuggestServlet serv = new SuggestServlet();
		try {
			serv.doPost(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test  
	public void testNoArtist() throws IOException {
		new MockUp<Utils>() {
			@Mock
			private String readText(HttpServletRequest request) {
				return jsonCorrectRequest; 
			}
		};

		new MockUp<DBAccess>() {
			@Mock
			public DBUser getUserInfo(int id){
				Assert.assertEquals(testNoArtist.getId(), id);
				return testNoArtist;
			}
			@Mock
			public List<String> getRules(int idNet, int idUser, boolean dynamic){
				Assert.fail();
				return dynRules;
			}
		};

		new Expectations() {
			{
				//mockHttpServletRequest.getContentType(); returns(Utils.jsonType);

				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		SuggestServlet serv = new SuggestServlet();
		try {
			serv.doPost(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test  
	public void testDBNotConnected() throws IOException {
		new MockUp<Utils>() {
			@Mock
			private String readText(HttpServletRequest request) {
				return jsonCorrectRequest; 
			}
		};

		new MockUp<DBAccess>() {
			@Mock
			public DBUser getUserInfo(int id) throws SQLTimeoutException{
				throw new SQLTimeoutException();
			}
			@Mock
			public List<String> getRules(int idNet, int idUser, boolean dynamic){
				Assert.fail();
				return dynRules;
			}
		};

		new Expectations() {
			{
				//mockHttpServletRequest.getContentType(); returns(Utils.jsonType);

				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		SuggestServlet serv = new SuggestServlet();
		try {
			serv.doPost(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test  
	public void testTagClusterVuoto() throws IOException {
		new MockUp<Utils>() {
			@Mock
			private String readText(HttpServletRequest request) {
				return jsonCorrectRequest; 
			}
		};

		new MockUp<DBAccess>() {
			@Mock
			public DBUser getUserInfo(int id){
				Assert.assertEquals(testUserManyArtist.getId(), id);
				return testUserManyArtist;
			}
			@Mock
			public List<String> getRules(int idNet, int idUser, boolean dynamic){
				Assert.assertEquals(testUserManyArtist.getId(), idUser);
				if (dynamic)
					return dynRules; 
				else
					return staRules;
			}
			@Mock
			public List<Integer> getTags(String cluster) throws IllegalArgumentException{
				throw new IllegalArgumentException();
			}
		};

		new Expectations() {
			{
				//mockHttpServletRequest.getContentType(); returns(Utils.jsonType);

				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		SuggestServlet serv = new SuggestServlet();
		try {
			serv.doPost(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test  
	public void testTagClusterNotFound() throws IOException {
		new MockUp<Utils>() {
			@Mock
			private String readText(HttpServletRequest request) {
				return jsonCorrectRequest; 
			}
		};

		new MockUp<DBAccess>() {
			@Mock
			public DBUser getUserInfo(int id){
				Assert.assertEquals(testUserManyArtist.getId(), id);
				return testUserManyArtist;
			}
			@Mock
			public List<String> getRules(int idNet, int idUser, boolean dynamic){
				Assert.assertEquals(testUserManyArtist.getId(), idUser);
				if (dynamic)
					return dynRules; 
				else
					return staRules;
			}
			@Mock
			public List<Integer> getTags(String cluster) throws NotFoundException{
				throw new NotFoundException("");
			}
		};

		new Expectations() {
			{
				//mockHttpServletRequest.getContentType(); returns(Utils.jsonType);

				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		SuggestServlet serv = new SuggestServlet();
		try {
			serv.doPost(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test  
	public void testWrongJson1() throws IOException {
		new MockUp<Utils>() {
			@Mock
			private String readText(HttpServletRequest request) {
				System.out.print(jsonWrong1Request);
				return jsonWrong1Request; 
			}
		};

		new MockUp<DBAccess>() {
			@Mock
			public DBUser getUserInfo(int id){
				Assert.fail();
				return testUserManyArtist;
			}
		};

		new Expectations() {
			{
				//mockHttpServletRequest.getContentType(); returns(Utils.jsonType);

				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		SuggestServlet serv = new SuggestServlet();
		try {
			serv.doPost(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}
}
