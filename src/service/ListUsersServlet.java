package service;

import java.io.IOException;
import java.sql.SQLTimeoutException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.gson.Gson;

import db.DBAccess;
import db.DBUser;

@SuppressWarnings("serial")
public class ListUsersServlet extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		try {
			List<DBUser> users = DBAccess.getUserList();

			resp.setContentType(Utils.jsonType);
			Gson gson = new Gson();
			resp.getWriter().print(gson.toJson(users));
		} catch (SQLTimeoutException e) {
			Utils.serverError(resp, "Database cannot be reached");
		} catch (InternalServerErrorException e) {
			Utils.serverError(resp, e.getMessage());
		}
	}

}
