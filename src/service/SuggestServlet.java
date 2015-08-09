package service;

import java.io.IOException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jess.JessException;
import jesswrapper.JessEngine;
import jesswrapper.NotSimpleIDResultsException;
import jesswrapper.Observation;

import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.gson.Gson;

import db.DBAccess;
import db.DBArtist;
import db.DBOsservazione;
import db.DBRequestSugg;
import db.DBSuggerimento;
import db.DBTag;
import db.DBUser;

@SuppressWarnings("serial")
public class SuggestServlet extends HttpServlet {

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {

			DBRequestSugg request = (DBRequestSugg) Utils.parseJSON(req, DBRequestSugg.class);
			int id_user = request.getId();
			int id_net = 3; // musictext network

			// una volta con le regole statiche - lista tag statici(artisti)
			String queryNode1 = "Tag";

			// get lista artisti dell'utente
			DBUser user = DBAccess.getUserInfo(id_user);
			if (user.getArtists() == null || user.getArtists().size()==0)
				throw new InternalServerErrorException("L'utente non ha artisti preferiti");
			ArrayList<Observation> osservazioni = new ArrayList<Observation>();
			for (DBArtist a:user.getArtists())
				osservazioni.add(new Observation("Artist", a.getId() + ""));

			// get regole statiche 0
			List<String> rules1 = DBAccess.getRules(id_net, id_user, false);
			JessEngine je = new JessEngine();

			je.addTemplate(queryNode1);
			for(String r: rules1){
				je.addRule(r);
			}

			// uso inferIds (["artist", id], "tag") -> lista tag
			List<Integer> resultTag1 = je.inferIDs(osservazioni, queryNode1);

			// --------------------------------------
			// una volta con le regole dinamiche dell'utente - lista tag contestuali 
			String queryNode2 = "Tag_cluster";

			// get regole dinamiche dell'utente
			List<String> rules2 = DBAccess.getRules(id_net, id_user, true);
			JessEngine je2 = new JessEngine();

			// get lista osservazioni  [temperatura, condizioni_meteo, mezzo, km_to_run, momento_viaggio]
			ArrayList<Observation> input = new ArrayList<Observation>();
			for(DBOsservazione o:request.getOsservazioni()){
				input.add(new Observation(o.getName(),o.getValue()));
			}

			// uso infer (["osserv", "valore"], tag_cluster) -> 1 tag_cluster
			String resultCluterTag = "";
			try {
				resultCluterTag = je2.infer(rules2, input, queryNode2);
			} catch (JessException e1) {
				e1.printStackTrace();
			}

			// ottenere lista tag da tag_cluster
			List<Integer> resultTag2;
			try {
				resultTag2 = DBAccess.getTags(resultCluterTag);
			} catch(IllegalArgumentException e) {
				throw new InternalServerErrorException(e.getMessage());
			}

			// semplice intersezione di liste
			List<Integer> result = Utils.intersection(resultTag1, resultTag2);

			// restituzione suggerimento
			DBSuggerimento sugg = new DBSuggerimento();
			for (Integer t:result)
				sugg.addTag(new DBTag(t));

			Gson gson = new Gson();
			resp.setContentType(Utils.jsonType);
			resp.addHeader("cluster", resultCluterTag);
			resp.getWriter().print(gson.toJson(sugg));
		} catch (SQLTimeoutException e) {
			Utils.serverError(resp, "Database cannot be reached");
		} catch (IllegalArgumentException e) {
			Utils.badRequest(resp, e.getMessage());
		} catch (InternalServerErrorException e) {
			Utils.serverError(resp, e.getMessage());
		} catch (NotFoundException e) {
			Utils.notFound(resp, e.getMessage());
		} catch (NotSimpleIDResultsException e) {
			Utils.serverError(resp, e.getMessage());
		}
	}
}
