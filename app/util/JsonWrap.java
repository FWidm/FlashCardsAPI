package util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.avaje.ebean.Model;
import models.Answer;
import models.User;
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

//    /**
//     * Generates an objectnode that will contain a given statuscode and the
//     * description in human readable form.
//     *
//     * @param statuscode
//     * @param description
//     * @return
//     */
//    public static ObjectNode prepareJsonStatus(int statuscode,
//                                               String description, User u) {
//        ObjectNode result = Json.newObject();
//        result.put("statuscode", statuscode);
//        result.put("description", description);
//        result.put("user", getJson(u));
//        return result;
//    }
    /*public static <T> List<T> getListFromJSONArray(Class<T> tClass, String jsonKey, JsonNode json) {

        System.out.println("Extends ebean model? ");
        try {
            T inst = tClass.newInstance();
            System.out.println(Model.class.isAssignableFrom(tClass));
            //if the objects class extends the Ebean.Model class, we can use the find.
            if (Model.class.isAssignableFrom(tClass)) {

                List<T> list = new ArrayList<T>();
                if (json.has(jsonKey)) {
                    list = new ArrayList<T>();
                    //get the specific nods in the json
                    JsonNode answerRoot = json.findValue(jsonKey);
                    // Loop through all objects in the values associated with the
                    // "answers" key.
                    for (JsonNode tmp : answerRoot) {
                        // when an answer with <id> is found we will get the object and add them to the list.
                        if (tmp.has("id")) {
                            if(
                            T answer = T.tClass.getDeclaredField("find")).byId(tmp.get("id").asLong());
//                        list.add(answer);
                        }
                    }
                    System.out.println(list);
                    return list;
                }

            }
            }catch(InstantiationException e){
                e.printStackTrace();
            }catch(IllegalAccessException e){
                e.printStackTrace();
            }


            return null;
        }*/
    }
