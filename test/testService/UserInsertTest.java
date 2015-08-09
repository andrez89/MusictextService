package testService;

import java.io.IOException;
import java.sql.SQLTimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.*;
import org.junit.runner.RunWith;

import com.google.api.server.spi.response.InternalServerErrorException;

import service.UserServlet;
import service.Utils;
import db.DBAccess;
import mockit.*;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class UserInsertTest {
	@Mocked
	HttpServletRequest mockHttpServletRequest;
	@Mocked
	HttpServletResponse mockHttpServletResponse;

	@Test // test successful user insert 
	public void testInsertSuccess() throws IOException {
		new MockUp<DBAccess>() {
			@Mock
			public int addUser(String input) {
				return 5; // success, user created and id 5 returned
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getParameter("user_name"); returns("pippo");
				mockHttpServletResponse.setStatus(HttpServletResponse.SC_CREATED);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.addIntHeader("id", 5);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		UserServlet serv = new UserServlet();
		try {
			serv.doPut(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}
	
	@Test // test input string empty
	public void testNewUserEmpty() throws IOException {
		new Expectations() {
			{
				mockHttpServletRequest.getParameter("user_name"); returns("");

				mockHttpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		UserServlet serv = new UserServlet();
		try {
			serv.doPut(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}
	
	@Test // test input string null
	public void testNewUserNull() throws IOException {
		new Expectations() {
			{
				mockHttpServletRequest.getParameter("user_name"); returns(null);

				mockHttpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		UserServlet serv = new UserServlet();
		try {
			serv.doPut(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test // test successful user insert 
	public void testDBNotConnected() throws IOException {
		new MockUp<DBAccess>() {
			@Mock
			public int addUser(String input) throws SQLTimeoutException {
				throw new SQLTimeoutException();
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getParameter("user_name"); returns("pippo");
				mockHttpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		UserServlet serv = new UserServlet();
		try {
			serv.doPut(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test // test successful user insert 
	public void testUserExists() throws IOException {
		new MockUp<DBAccess>() {
			@Mock
			public int addUser(String input) throws SQLTimeoutException {
				throw new IllegalArgumentException();
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getParameter("user_name"); returns("pippo");
				mockHttpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		UserServlet serv = new UserServlet();
		try {
			serv.doPut(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test
	public void testInternalDBError() throws IOException {
		new MockUp<DBAccess>() {
			@Mock
			public int addUser(String input) throws InternalServerErrorException {
				throw new InternalServerErrorException("");
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getParameter("user_name"); returns("pippo");
				mockHttpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		UserServlet serv = new UserServlet();
		try {
			serv.doPut(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}
}
