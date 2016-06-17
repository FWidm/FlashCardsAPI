package util;

import java.text.SimpleDateFormat;

import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonWrap {
//	public final static String dateformat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	
	/**
	 * Wraps the object it receives in a json file
	 * @param o
	 * @return
	 */
	public static JsonNode getJson(Object o) {
		ObjectMapper mapper = new ObjectMapper();
//		SimpleDateFormat outputFormat = new SimpleDateFormat(dateformat);
//		mapper.setDateFormat(outputFormat);
//		Json.setObjectMapper(mapper);
		return Json.toJson(o);
	}

	/**
	 * Generates an objectnode that will contain a given statuscode and the
	 * description in human readable form.
	 * 
	 * @param statuscode
	 * @param description
	 * @return
	 */
	public static ObjectNode prepareJsonStatus(int statuscode,
			String description) {
		ObjectNode result = Json.newObject();
		result.put("statuscode", statuscode);
		result.put("description", description);
		return result;
	}
}
