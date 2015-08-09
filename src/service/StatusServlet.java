package service;

import java.io.IOException;
import java.sql.SQLTimeoutException;

import javax.servlet.http.*;

import com.google.api.server.spi.response.InternalServerErrorException;

import db.DBAccess;

@SuppressWarnings("serial")
public class StatusServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		try {
			resp.setContentType(Utils.htmlType);
			if (DBAccess.isConnected()) {
				Utils.ok(resp);
			} else {
				Utils.serverError(resp, "SQL disconnected");
			}

		} catch (SQLTimeoutException e) {
			Utils.serverError(resp, "Database cannot be reached");
		} catch (IllegalArgumentException e) {
			Utils.badRequest(resp, e.getMessage());
		} catch (InternalServerErrorException e) {
			Utils.serverError(resp, e.getMessage());
		}
	}
}
