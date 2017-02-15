package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import java.util.List;
import java.util.Map;

public class JsonUtil {
//	public final static String dateformat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * Wraps the object it receives in a json file
     *
     * @param o object we want to convert
     * @return JsonNode containing params of the object
     */
    public static JsonNode toJson(Object o) {
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
     * @param statuscode  we want to display in the json
     * @param description text
     * @return Json Object node
     */
    public static ObjectNode prepareJsonStatus(int statuscode,
                                               String description) {
        ObjectNode result = Json.newObject();
        result.put(JsonKeys.STATUS_CODE, statuscode);
        result.put("description", description);
        return result;
    }

    /**
     * Generates an objectnode that will contain a given statuscode and the
     * description in human readable form.
     *
     * @param statuscode  we want to display in the json
     * @param description text
     * @param id          we want to return matching the description text
     * @return Json Object node
     */
    public static ObjectNode prepareJsonStatus(int statuscode, String description, Long id) {
        ObjectNode result = Json.newObject();
        result.put(JsonKeys.STATUS_CODE, statuscode);
        result.put("description", description);
        result.put("id", id);
        return result;
    }


    /**
     * Generates an objectnode that will contain a given statuscode and the
     * description in human readable form. In addition to that we want to return an additional key/value pair where value is an objectlist.
     *
     * @param statuscode  we want to return
     * @param description text
     * @param name        of the returned key
     * @param objectList  list of returned objects in an array (json)
     * @return Objectnode
     */
    public static ObjectNode prepareJsonStatus(int statuscode, String description, String name, List<Object> objectList) {
        ObjectNode result = Json.newObject();
        result.put("statuscode", statuscode);
        result.put("description", description);
        result.set(name, toJson(objectList));
        return result;
    }

    /**
     * Wraps the Map given as parameter in json.
     *
     * @param data - in the form of <key, obj>
     * @return json representation of the given data
     */
    public static ObjectNode convertToJsonNode(Map<String, Object> data) {
        ObjectNode result = Json.newObject();

        for (String key : data.keySet()) {
            result.set(key, toJson(data.get(key)));
        }
        return result;
    }


}
