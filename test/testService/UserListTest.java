package testService;

import java.io.IOException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.*;
import org.junit.runner.RunWith;

import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.gson.Gson;

import service.ListUsersServlet;
import service.Utils;
import db.DBAccess;
import db.DBArtist;
import db.DBUser;
import mockit.*;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class UserListTest {
	@Mocked
	HttpServletRequest mockHttpServletRequest;
	@Mocked
	HttpServletResponse mockHttpServletResponse;

	String jsonList;
	List<DBUser> userList;
	
	public UserListTest(){
		super();
		Gson gson = new Gson();
		DBUser testUserNoArtist, testUserOneArtist, testUserManyArtist;
		testUserNoArtist = new DBUser(2,"pippo");

		testUserOneArtist = new DBUser(3,"mario");
		testUserOneArtist.addArtists(new DBArtist(1, "883"));

		testUserManyArtist = new DBUser(4,"carlo");
		testUserManyArtist.addArtists(new DBArtist(1, "883"));
		testUserManyArtist.addArtists(new DBArtist(2, "Queen"));
		testUserManyArtist.addArtists(new DBArtist(3, "Edoardo Bennato"));
		testUserManyArtist.addArtists(new DBArtist(4, "Pooh"));
		
		userList = new ArrayList<DBUser>();
		userList.add(testUserNoArtist);
		userList.add(testUserOneArtist);
		userList.add(testUserManyArtist);
		jsonList = gson.toJson(userList);
	}
	
	@Test
	public void testGetListSuccess() throws IOException {
		new MockUp<DBAccess>() {
			@Mock
			public List<DBUser> getUserList() {
				return userList;
			}
		};

		new Expectations() {
			{
				mockHttpServletResponse.setContentType(Utils.jsonType);
				mockHttpServletResponse.getWriter().print(jsonList);
			}
		};

		ListUsersServlet serv = new ListUsersServlet();
		try {
			serv.doGet(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}
	
	@Test // empty list or SQL error
	public void testInternalError() throws IOException {
		new MockUp<DBAccess>() {
			@Mock
			public List<DBUser> getUserList() throws InternalServerErrorException {
				throw new InternalServerErrorException("");
			}
		};

		new Expectations() {
			{
				mockHttpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		ListUsersServlet serv = new ListUsersServlet();
		try {
			serv.doGet(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}
	
	@Test
	public void testDisconnectedDBError() throws IOException {
		new MockUp<DBAccess>() {
			@Mock
			public List<DBUser> getUserList() throws SQLTimeoutException {
				throw new SQLTimeoutException("");
			}
		};

		new Expectations() {
			{
				mockHttpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		ListUsersServlet serv = new ListUsersServlet();
		try {
			serv.doGet(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}
}
