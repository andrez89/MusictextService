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
import db.DBUser;
import mockit.*;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class UserEditTest {
	@Mocked
	HttpServletRequest mockHttpServletRequest;
	@Mocked
	HttpServletResponse mockHttpServletResponse;

	@Test // test successful user insert 
	public void testEditAnagraficaSuccess() throws IOException {
		new MockUp<Utils>() {
			@Mock
			private String readText(HttpServletRequest request) {
				return "{\"id\": 3,\"gender\": \"m\",\"age\": 25}"; 
			}
		};

		new MockUp<DBAccess>() {
			@Mock
			public void editUser(DBUser input) {
				Assert.assertEquals(input.getArtists(), null);
				Assert.assertEquals(input.getGender(), "m");
				Assert.assertEquals(input.getAge().longValue(), 25);
				return; // success, user created and id 5 returned
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getContentType(); returns(Utils.jsonType);

				mockHttpServletResponse.setStatus(HttpServletResponse.SC_OK);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};


		UserServlet serv = new UserServlet();
		try {
			serv.doPost(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test // test successful user insert 
	public void testEditArtistaSuccess() throws IOException {
		new MockUp<Utils>() {
			@Mock
			private String readText(HttpServletRequest request) {
				return "{\"id\": 3, \"artists\":[{\"id\": 3}]}"; 
			}
		};

		new MockUp<DBAccess>() {
			@Mock
			public void editUser(DBUser input) {
				Assert.assertEquals(input.getArtists().size(), 1);
				Assert.assertEquals(input.getArtists().get(0).getId(), 3);
				Assert.assertEquals(input.getGender(), null);
				Assert.assertEquals(input.getAge(), null);
				return; // success, user created and id 5 returned
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getContentType(); returns(Utils.jsonType);

				mockHttpServletResponse.setStatus(HttpServletResponse.SC_OK);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};


		UserServlet serv = new UserServlet();
		try {
			serv.doPost(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test // test edit with wrong header content type
	public void testEditWrongHeader() throws IOException {
		new Expectations() {
			{
				mockHttpServletRequest.getContentType(); returns("abc");

				mockHttpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};


		UserServlet serv = new UserServlet();
		try {
			serv.doPost(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test // test successful user insert 
	public void testDBNotConnected() throws IOException {
		new MockUp<Utils>() {
			@Mock
			private String readText(HttpServletRequest request) {
				return "{\"id\": 3, \"artists\":[{\"id\": 3}]}"; 
			}
		};
		new MockUp<DBAccess>() {
			@Mock
			public void editUser(DBUser input) throws SQLTimeoutException {
				throw new SQLTimeoutException();
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getContentType(); returns(Utils.jsonType);

				mockHttpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		UserServlet serv = new UserServlet();
		try {
			serv.doPost(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test // test successful user insert 
	public void testInternalDBError() throws IOException {
		new MockUp<Utils>() {
			@Mock
			private String readText(HttpServletRequest request) {
				return "{\"id\": 3, \"artists\":[{\"id\": 3}]}"; 
			}
		};
		new MockUp<DBAccess>() {
			@Mock
			public void editUser(DBUser input) throws InternalServerErrorException {
				throw new InternalServerErrorException("");
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getContentType(); returns(Utils.jsonType);
				
				mockHttpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};

		UserServlet serv = new UserServlet();
		try {
			serv.doPost(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test // test wrong json
	public void testWrongJson1() throws IOException {
		new MockUp<Utils>() {
			@Mock
			private String readText(HttpServletRequest request) {
				return "{\"id\": 3,\"gender\": \"m\",\"age\": 25"; 
			}
		};

		new MockUp<DBAccess>() {
			@Mock
			public void editUser(DBUser input) {
				Assert.fail();
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getContentType(); returns(Utils.jsonType);

				mockHttpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};


		UserServlet serv = new UserServlet();
		try {
			serv.doPost(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test // test wrong json
	public void testWrongJson2() throws IOException {
		new MockUp<Utils>() {
			@Mock
			private String readText(HttpServletRequest request) {
				return "{\"id\": 3,\"gender\": \"m\",\"age\": 25, \"artists\":[\"aaa\"]}"; 
			}
		};

		new MockUp<DBAccess>() {
			@Mock
			public void editUser(DBUser input) {
				Assert.fail();
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getContentType(); returns(Utils.jsonType);

				mockHttpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};


		UserServlet serv = new UserServlet();
		try {
			serv.doPost(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test // test wrong age
	public void testWrongAge() throws IOException {
		new MockUp<Utils>() {
			@Mock
			private String readText(HttpServletRequest request) {
				return "{\"id\": 3,\"gender\": \"m\",\"age\": \"venticinque\"}"; 
			}
		};

		new MockUp<DBAccess>() {
			@Mock
			public void editUser(DBUser input) {
				Assert.fail();
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getContentType(); returns(Utils.jsonType);

				mockHttpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};


		UserServlet serv = new UserServlet();
		try {
			serv.doPost(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}

	@Test // test wrong artist list
	public void testWrongArtistList() throws IOException {
		new MockUp<Utils>() {
			@Mock
			private String readText(HttpServletRequest request) {
				return "{\"id\": 3,\"gender\": \"m\",\"age\": \"venticinque\"}"; 
			}
		};

		new MockUp<DBAccess>() {
			@Mock
			public void editUser(DBUser input) {
				Assert.fail();
			}
		};

		new Expectations() {
			{
				mockHttpServletRequest.getContentType(); returns(Utils.jsonType);

				mockHttpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				mockHttpServletResponse.setContentType(Utils.htmlType);
				mockHttpServletResponse.getWriter().print(anyString);
			}
		};


		UserServlet serv = new UserServlet();
		try {
			serv.doPost(mockHttpServletRequest, mockHttpServletResponse);
		} catch (IOException e) { Assert.fail(); }
	}
}
