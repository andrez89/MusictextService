package service;

/*
 * Struttura chiamata
 * /computeRules?net_name=nome&evidence=queue&evidence=agent_Type&threshold=0.7
 * ?id_net=2&evidence=queue&evidence=agent_Type&threshold=0.7
 */

import java.io.IOException;
import java.sql.SQLTimeoutException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.server.spi.response.InternalServerErrorException;

import db.DBAccess;

@SuppressWarnings("serial")
public class GetRulesServlet extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		try {
			int id_net = Utils.parseInt(req.getParameter("id_net"));
			int id_user = Utils.parseInt(req.getParameter("id_user"));

			List<String> rules;
			rules = DBAccess.getRules(id_net, id_user,false);
			for(String r: rules){
				resp.getWriter().println(r);
			}
		} catch (SQLTimeoutException e) {
			Utils.serverError(resp, "Database cannot be reached");
		} catch (IllegalArgumentException e) {
			Utils.badRequest(resp, e.getMessage());
		} catch (InternalServerErrorException e) {
			Utils.serverError(resp, e.getMessage());
		}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		try {
			int id_net = Utils.parseInt(req.getParameter("id_net"));
			int id_user = Utils.parseInt(req.getParameter("id_user"));

			List<String> rules;
			rules = DBAccess.getRules(id_net, id_user,true);
			for(String r: rules){
				resp.getWriter().println(r);
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
