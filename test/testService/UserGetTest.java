package testService;

import java.io.IOException;
import java.sql.SQLTimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.*;
import org.junit.runner.RunWith;

import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.gson.Gson;

import service.UserServlet;
import service.Utils;
import db.DBAccess;
import db.DBArtist;
import db.DBUser;
import mockit.*;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class UserGetTest {
	@Mocked
	HttpServletRequest mockHttpServletRequest;
	@Mocked
	HttpServletResponse mockHttpServletResponse;

	DBUser testUserNoArtist, testUserOneArtist, testUserManyArtist;
	String jsonNoArtist, jsonOneArtist, jsonManyArtist;
	
	public UserGetTest(){
		super();
		Gson gson = new Gson();
		testUserNoArtist = new DBUser(2,"pippo");
		jsonNoArtist = gson.toJson(testUserNoArtist);

		testUserOneArtist = new DBUser(3,"mario");
		jsonOneArtist = gson.toJson(testUserOneArtist);
		testUserOneArtist.addArtists(new DBArtist(1, "883"));

		testUserManyArtist = new DBUser(4,"carlo");
		jsonManyArtist = gson.toJson(testUserManyArtist);
		testUserManyArtist.addArtists(new DBArtist(1, "883"));
		testUserManyArtist.addArtists(new DBArtist(2, "Queen"));
		testUserManyArtist.addArtists(new DBArtist(3, "Edoardo Bennato"));
		testUserManyArtist.addArtists(new DBArtist(4, "Pooh"));
	}
	
	@Test // test successful user get from name 
	public void testGetNameNoArtistSuccess() throws IOException {
		new MockUp<DBAccess>() {
			@Mock
			public DBUser getUserInfo(String input) {
				Assert.assertEquals(input, testUserNoArtist.getName());
				return testUserNoArtist;
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getParameter("user_name"); returns(testUserNoArtist.getName());
				mockHttpServletRequest.getParameter("user_id"); returns("");

				mockHttpServletResponse.setContentType(Utils.jsonType);
				mockHttpServletResponse.getWriter().print(jsonNoArtist);
			}
		};

		UserServlet serv = new UserServlet();
		try {
			serv.doGet(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}
	
	@Test // test successful user get from id 
	public void testGetIdNoArtistSuccess() throws IOException {
		new MockUp<DBAccess>() {
			@Mock
			public DBUser getUserInfo(int input) {
				Assert.assertEquals(input, testUserNoArtist.getId());
				return testUserNoArtist;
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getParameter("user_name"); returns("");
				mockHttpServletRequest.getParameter("user_id"); returns("" + testUserNoArtist.getId());
				
				mockHttpServletResponse.setContentType(Utils.jsonType);
				mockHttpServletResponse.getWriter().print(jsonNoArtist);
			}
		};

		UserServlet serv = new UserServlet();
		try {
			serv.doGet(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}
	
	@Test
	public void testNoParameter() throws IOException {
		new MockUp<DBAccess>() {
			@Mock
			public DBUser getUserInfo(int input) {
				Assert.assertEquals(input, testUserNoArtist.getId());
				return testUserNoArtist;
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getParameter("user_name"); returns("");
				mockHttpServletRequest.getParameter("user_id"); returns("");

				mockHttpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		UserServlet serv = new UserServlet();
		try {
			serv.doGet(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test
	public void testDisconnectedDBError() throws IOException {
		new MockUp<DBAccess>() {
			@Mock
			public DBUser getUserInfo(String input) throws SQLTimeoutException {
				throw new SQLTimeoutException("");
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getParameter("user_name"); returns(testUserNoArtist.getName());
				mockHttpServletRequest.getParameter("user_id"); returns("");
				
				mockHttpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		UserServlet serv = new UserServlet();
		try {
			serv.doGet(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test
	public void testInternalDBError() throws IOException {
		new MockUp<DBAccess>() {
			@Mock
			public DBUser getUserInfo(String input) throws InternalServerErrorException {
				throw new InternalServerErrorException("");
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getParameter("user_name"); returns(testUserNoArtist.getName());
				mockHttpServletRequest.getParameter("user_id"); returns("");
				
				mockHttpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		UserServlet serv = new UserServlet();
		try {
			serv.doGet(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test
	public void testUserNotFound() throws IOException {
		new MockUp<DBAccess>() {
			@Mock
			public DBUser getUserInfo(String input) throws NotFoundException {
				throw new NotFoundException("");
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getParameter("user_name"); returns(testUserNoArtist.getName());
				mockHttpServletRequest.getParameter("user_id"); returns("");
				
				mockHttpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		UserServlet serv = new UserServlet();
		try {
			serv.doGet(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}
}
