package repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigException;
import models.User;
import models.UserGroup;
import play.Logger;
import play.data.validation.Constraints;
import util.JsonKeys;
import util.JsonUtil;
import util.RequestKeys;
import util.exceptions.InvalidInputException;
import util.exceptions.ObjectNotExistingException;
import util.exceptions.ParameterNotSupportedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
public class UserRepository {
    public static User createUser(JsonNode json) throws InvalidInputException,ParameterNotSupportedException {

        ObjectMapper mapper = new ObjectMapper();

        if (json.has(JsonKeys.USER_GROUPS)) {
            throw new ParameterNotSupportedException("The user could not be created, a user group has to be set via PATCH or PUT. It may not be content of POST.");
        }
        User tmp = mapper.convertValue(json, User.class);
        if (json.has(JsonKeys.USER_AVATAR)) {
            tmp.setAvatar(json.get(JsonKeys.USER_AVATAR).asText());
        }
        //Checks if the constraints for @email are met via it's isValid method.
        Constraints.EmailValidator emailValidator = new Constraints.EmailValidator();
        Constraints.MinLengthValidator minLengthValidator = new Constraints.MinLengthValidator();
        if(JsonKeys.debugging)if(JsonKeys.debugging) Logger.debug("json=" + json + " - obj=" + tmp);
        if (emailValidator.isValid(tmp.getEmail()) && minLengthValidator.isValid(tmp.getName())
                && minLengthValidator.isValid(tmp.getPassword())) {
            // if this entry with specified email does not exist, create, else
            // throw an error.
            if (User.find.where().eq(JsonKeys.USER_EMAIL, tmp.getEmail()).findUnique() == null) {
                User u = new User(tmp);

                u.save();
                return u;
            }
        }
        throw new InvalidInputException("The user could not be created, please specify email, name, password. " +
                "Email has to be valid (e.g. a@b.com)");

    }

    public static User changeUser(Long id, JsonNode json, Map<String, String[]> urlParams, String updateMethod)
    throws NullPointerException, InvalidInputException, ParameterNotSupportedException, ObjectNotExistingException{
        boolean appendMode = false;

        if (urlParams.containsKey(RequestKeys.APPEND)) {
            appendMode = Boolean.parseBoolean(urlParams.get(RequestKeys.APPEND)[0]);
        }
        if(JsonKeys.debugging) Logger.debug("Appending mode enabled? " + appendMode);

        if(JsonKeys.debugging)Logger.debug("Update method=" + updateMethod);
        if (updateMethod.equals("PUT") && (!json.has(JsonKeys.USER_EMAIL) || !json.has(JsonKeys.RATING)
                || !json.has(JsonKeys.USER_NAME) || !json.has(JsonKeys.USER_GROUPS) || !json.has(JsonKeys.USER_PASSWORD)))
        {
            throw new InvalidInputException("The Update method needs all details of the user, such as email, " +
                    "rating, name, group and password! An attribute was missing for id="
                    + id + ".");
        }


        // get the specific user
        User u = User.find.byId(id);

        // check for new values
        Constraints.EmailValidator emailValidator = new Constraints.EmailValidator();

        if (json.has(JsonKeys.USER_EMAIL) && emailValidator.isValid(json.get(JsonKeys.USER_EMAIL).asText())) {
            User checkEmail = findUserByEmail(json.get(JsonKeys.USER_EMAIL).asText());

            if(JsonKeys.debugging)Logger.debug("does email exist? " + checkEmail);
            if (checkEmail == null)
                u.setEmail(json.get(JsonKeys.USER_EMAIL).asText());
            else
                throw new InvalidInputException("The specified email can not be used to update this user.");

        }
        Constraints.MinLengthValidator minLengthValidator = new Constraints.MinLengthValidator();
        if (json.has(JsonKeys.USER_PASSWORD) && minLengthValidator.isValid(json.get(JsonKeys.USER_PASSWORD).asText())) {
            u.setPassword(json.get(JsonKeys.USER_PASSWORD).asText());
        }
        if (json.has(JsonKeys.RATING))
            u.setRating(json.get(JsonKeys.RATING).asInt());
        if (json.has(JsonKeys.USER_NAME) && minLengthValidator.isValid(json.get(JsonKeys.USER_NAME).asText()))
            u.setName(json.get(JsonKeys.USER_NAME).asText());
        if (json.has(JsonKeys.USER_GROUPS)) {
            if (appendMode) {
                if(JsonKeys.debugging)Logger.debug("Found group");
                List<UserGroup> mergedGroups = new ArrayList<UserGroup>();

                //add all old valid information
                mergedGroups.addAll(u.getUserGroups());
                //retrieve new, check if not in list already
                for (UserGroup ug :
                        JsonUtil.retrieveGroups(json)) {
                    if (!mergedGroups.contains(ug)) {
                        mergedGroups.add(ug);
                    }
                }
                if(JsonKeys.debugging)Logger.debug("New groups: " + mergedGroups);
                u.setUserGroups(mergedGroups);
            } else {
                u.setUserGroups(JsonUtil.retrieveGroups(json));
            }
        }
        if (json.has(JsonKeys.USER_AVATAR)) {
            if(JsonKeys.debugging)if(JsonKeys.debugging)Logger.debug("avatar="+json.get(JsonKeys.USER_AVATAR));
            u.setAvatar(json.get(JsonKeys.USER_AVATAR).asText());
        }
        u.update();
        return u;
    }

    public static void deleteUserById(Long id) {
        User.find.ref(id).delete();
    }

    public static User findUserByEmail(String email) {
        return User.find.where().eq(JsonKeys.USER_EMAIL, email).findUnique();
    }

    public static User findById(Long id) {
        return User.find.byId(id);
    }

    /**
     * 
     * @param urlParams
     * @return
     */
    public static List<User> getUsers(Map<String, String[]> urlParams) {
        if (urlParams.containsKey(RequestKeys.EMAIL)) {
            String email = urlParams.get(RequestKeys.EMAIL)[0];
            return User.find.where().eq(JsonKeys.USER_EMAIL, email).findList();
        }
        if (urlParams.containsKey(RequestKeys.NAME)) {
            String name = urlParams.get(RequestKeys.NAME)[0];
            Logger.debug("name="+name);

            return User.find.where().eq(JsonKeys.USER_NAME, name).findList();
        } else {
            List<User> u = User.find.all();
            return User.find.all();
        }
    }
}
