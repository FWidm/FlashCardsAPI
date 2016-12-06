package util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.*;
import models.rating.AnswerRating;
import models.rating.CardRating;
import play.Logger;
import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import util.exceptions.ObjectNotFoundException;
import util.exceptions.ParameterNotSupportedException;

// TODO: 10.09.2016 Restructure
public class JsonUtil {
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
    public static ObjectNode prepareJsonStatus(int statuscode, String description, Long id) {
        ObjectNode result = Json.newObject();
        result.put("statuscode", statuscode);
        result.put("description", description);
        result.put("id", id);
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
    public static ObjectNode prepareJsonStatus(int statuscode, String description,String name, List<Object> objectList) {
        ObjectNode result = Json.newObject();
        result.put("statuscode", statuscode);
        result.put("description", description);
        result.set(name, getJson(objectList));
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
            result.set(key, getJson(data.get(key)));
        }
        return result;
    }

    /**
     * Retrieves the parent category from the given category. If the id of the parent object cant be found in the database, throw the exception.
     * @param receivedCategory
     * @return the category from db or null if null is received
     * @throws ObjectNotFoundException
     */
    public static Category parseParent(Category receivedCategory) throws ObjectNotFoundException {
        if(receivedCategory.getParent()!=null){
            Category parent = Category.find.byId(receivedCategory.getParent().getId());
            Logger.debug("got parent="+parent);
            if(parent!=null){
                return parent;
            }
            else
                throw new ObjectNotFoundException("Parent does not exist with the id="+receivedCategory.getParent().getId());
        }
        else
            return null;
    }
}
