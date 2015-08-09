package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

public class Utils {
	public static final String jsonType = "application/json";
	public static final String htmlType = "text/html";

	public static int parseInt(String strValue) throws IllegalArgumentException{
		try {  
			return Integer.parseInt(strValue);  
		} catch(NumberFormatException nfe) { 
			throw new IllegalArgumentException("La stringa non contiene un numero valido"); 
		}
	}

	public static int parseInt(String strValue, int def){
		try {  
			return Integer.parseInt(strValue);  
		} catch(NumberFormatException nfe) { 
			return def; 
		}
	}

	public static String checkText(String toCheck, char charToReplace){
		return toCheck.replace(charToReplace, ' ');
	}

	public static boolean areSameEvidences(ArrayList<String> previous, ArrayList<String> actual){
		if(previous == null || actual == null)
			return false;
		if(previous.size() != actual.size())
			return false;
		for(int i=0; i<actual.size(); i++){
			if(!isContained(previous,actual.get(i)))
				return false;
		}

		return true;
	}

	private static boolean isContained(ArrayList<String> list, String toCheck){
		for(int i=0; i<list.size();i++){
			if(list.get(i).equals(toCheck))
				return true;
		}
		return false;
	}

	public static Object parseJSON(HttpServletRequest request, Class<?> c) 
			throws IllegalArgumentException{
		String json;
		try {
			json = readText(request);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error reading the input JSON");
		}

		Gson gson = new Gson();
		Object user;
		try {
			user = gson.fromJson(json, c);
			return user;
		}catch(Exception ex){
			throw new IllegalArgumentException("Content request must be an appropriate json structure");
		}
	}

	private static String readText(HttpServletRequest request) throws IOException{
		String json = "";
		BufferedReader br = request.getReader();
		String sCurrentLine;
		while ((sCurrentLine = br.readLine()) != null) {
			json += sCurrentLine;
		}
		return json;
	}

	public static void notFound(HttpServletResponse response, String object) throws IOException{
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		response.setContentType(htmlType);
		response.getWriter().print("<h1>404 - " + object + ": not found!</h1>");
	}

	public static void badRequest(HttpServletResponse response, String note) throws IOException{
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.setContentType(htmlType);
		response.getWriter().print("<h1>400 - Bad Request!</h1> " + note);
	}

	public static void serverError(HttpServletResponse response, String note) throws IOException{
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		response.setContentType(htmlType);
		response.getWriter().print("<h1>500 - Internal server error!</h1> " + note);
	}

	public static void created(HttpServletResponse response, String object, int id) throws IOException{
		response.setStatus(HttpServletResponse.SC_CREATED);
		response.setContentType(htmlType);
		response.addIntHeader("id", id);
		response.getWriter().print("<h1>201 - " + object + " created!</h1> ");
	}

	public static void ok(HttpServletResponse response) throws IOException{
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(htmlType);
		response.getWriter().print("<h1>200 - OK!</h1> ");
	}

	public static List<Integer> intersection(List<Integer> l1,List<Integer> l2) {
		List<Integer> inters = new ArrayList<Integer>();
		for (Integer el:l1){
			if (l2.contains(el))
				inters.add(el);
		}
		// no common elements
		if (inters.size()==0){
			if (l1.size()>0)
				inters.add(l1.get(0));
			if (l2.size()>0)
				inters.add(l2.get(0));
		}

		return inters;
	}
}
