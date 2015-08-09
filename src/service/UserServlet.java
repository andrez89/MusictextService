package service;

import java.io.IOException;
import java.sql.SQLTimeoutException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.gson.Gson;

import db.DBAccess;
import db.DBUser;

@SuppressWarnings("serial")
public class UserServlet extends HttpServlet {

	/**
	 * Get existing user
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String user_name = req.getParameter("user_name");
		String user_id = req.getParameter("user_id");
		try {
			int id = Utils.parseInt(user_id, 0);
			DBUser user;
			if (user_name == null || user_name.length() < 3){
				if (id == 0){
					throw new IllegalArgumentException("Parameter user_name or user_id required.");
				} else {
					user = DBAccess.getUserInfo(id);
				}
			} else {
				user = DBAccess.getUserInfo(user_name);
			}

			Gson gson = new Gson();
			resp.setContentType(Utils.jsonType);
			resp.getWriter().print(gson.toJson(user));

		} catch (SQLTimeoutException e) {
			Utils.serverError(resp, "Database cannot be reached");
		} catch (IllegalArgumentException e) {
			Utils.badRequest(resp, e.getMessage());
		} catch (InternalServerErrorException e) {
			Utils.serverError(resp, e.getMessage());
		} catch (NotFoundException e) {
			Utils.notFound(resp, "User not found");
		}
	}

	/**
	 * Edit existing user
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			String cType = req.getContentType();
			if (! cType.equalsIgnoreCase(Utils.jsonType)){
				throw new IllegalArgumentException(
						"Content type must be 'application/json' instead of '" 
								+ cType + "'");
			} 

			DBUser user = (DBUser) Utils.parseJSON(req, DBUser.class);

			DBAccess.editUser(user);
			Utils.ok(resp);
		} catch (SQLTimeoutException e) {
			Utils.serverError(resp, "Database cannot be reached");
		} catch (IllegalArgumentException e) {
			Utils.badRequest(resp, e.getMessage());
		} catch (InternalServerErrorException e) {
			Utils.serverError(resp, e.getMessage());
		} 
	}

	/**
	 * Insert new user
	 */
	public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String user_name = req.getParameter("user_name");

		try {
			if (user_name == null || user_name.length() < 3){
				throw new IllegalArgumentException("Parameter user_name of at least 3 characters.");
			}

			int id = DBAccess.addUser(user_name);
			Utils.created(resp, "User", id);
		} catch (SQLTimeoutException e) {
			Utils.serverError(resp, "Database cannot be reached");
		} catch (IllegalArgumentException e) {
			Utils.badRequest(resp, e.getMessage());
		} catch (InternalServerErrorException e) {
			Utils.serverError(resp, e.getMessage());
		} 
	}
}
