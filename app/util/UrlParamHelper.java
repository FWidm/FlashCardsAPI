package util;

import models.Category;
import play.Logger;
import play.mvc.Controller;

import java.util.List;
import java.util.Map;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 * This class accesses the current Controller's request query string and performs convenience checking methods.
 */
public class UrlParamHelper {
    /**
     * Checks whether the given key is in the url and determines the boolean value sent.
     * @param key
     * @return true if parameters bool val is true, else false
     */
    public static boolean checkBool(String key){
        Map<String, String[]> urlParams = Controller.request().queryString();
        Logger.debug("params contain "+key+"? "+urlParams.keySet().contains(key));

        if(urlParams.keySet().contains(key)){
            return Boolean.parseBoolean(urlParams.get(key)[0]);
        }
        return false;
    }

    /**
     * Returns the Value for the key. It is null if the key does not exist.
     * @param key
     * @return value or null
     */
    public static String getValue(String key){
        Map<String, String[]> urlParams = Controller.request().queryString();
        Logger.debug("params contain "+key+"? "+urlParams.keySet().contains(key));

        if(urlParams.keySet().contains(key)){
            return urlParams.get(key)[0];
        }
        return null;
    }
    /**
     * Checks whether the given key exists in the url parameters
     * @param key
     * @return true if the key exists, else false
     */
    public static boolean checkForKey(String key){
        Map<String, String[]> urlParams = Controller.request().queryString();
        Logger.debug("params contain "+key+"? "+urlParams.keySet().contains(key));

        if(urlParams.keySet().contains(key)){
                return true;
        }
        return false;
    }
}
