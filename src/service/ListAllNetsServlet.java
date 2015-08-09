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
import db.DBNetwork;

@SuppressWarnings("serial")
public class ListAllNetsServlet extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		try {
			resp.setContentType(Utils.jsonType);

			List<DBNetwork> nets = DBAccess.getNetworks();

			Gson gson = new Gson();
			resp.getWriter().print(gson.toJson(nets));
		} catch (SQLTimeoutException e) {
			Utils.serverError(resp, "Database cannot be reached");
		} catch (IllegalArgumentException e) {
			Utils.badRequest(resp, e.getMessage());
		} catch (InternalServerErrorException e) {
			Utils.serverError(resp, e.getMessage());
		}
	}

}
