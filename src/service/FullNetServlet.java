package service;
// fullnet?net_id=2&format=old

import java.io.IOException;
import java.sql.SQLTimeoutException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.gson.Gson;

import db.DBAccess;
import db.DBNetwork;

@SuppressWarnings("serial")
public class FullNetServlet extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			int id_net = Utils.parseInt(req.getParameter("id_net"));
			int id_user = Utils.parseInt(req.getParameter("id_user"));

			Gson gson = new Gson();
			DBNetwork net;
			net = DBAccess.getFullNetworks(id_net, id_user);

			resp.setContentType(Utils.jsonType);
			resp.getWriter().print(gson.toJson(net));
		} catch (SQLTimeoutException e) {
			Utils.serverError(resp, "Database cannot be reached");
		} catch (IllegalArgumentException e) {
			Utils.badRequest(resp, e.getMessage());
		} catch (InternalServerErrorException e) {
			Utils.serverError(resp, e.getMessage());
		}
	}

}
