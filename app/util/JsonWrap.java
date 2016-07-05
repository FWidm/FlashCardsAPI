package util;

import java.util.Map;

import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonWrap {
//	public final static String dateformat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * Wraps the object it receives in a json file
     *
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

    /**
     * Generates an objectnode that will contain a given statuscode and the
     * description in human readable form.
     *
     * @param statuscode
     * @param description
     * @return
     */
    public static ObjectNode prepareJsonStatus(int statuscode,
                                               String description, int id) {
        ObjectNode result = Json.newObject();
        result.put("statuscode", statuscode);
        result.put("description", description);
        result.put("id",id);
        return result;
    }

    /**
     * Wraps the Map given as parameter in json.
     * @param data - in the form of <key, obj>
     * @return json representation of the given data
     */
    public static ObjectNode convertToJsonNode(Map<String, Object> data){
        ObjectNode result=Json.newObject();

        for (String key: data.keySet()) {
            result.set(key,getJson(data.get(key)));
        }
        return result;
    }

    }
