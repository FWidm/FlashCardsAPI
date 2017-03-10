package repositories;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.User;
import models.UserGroup;
import play.Logger;
import play.data.validation.Constraints;
import util.JsonKeys;
import util.RequestKeys;
import util.UserOperations;
import util.crypt.PasswordUtil;
import util.exceptions.InvalidInputException;
import util.exceptions.NotAuthorizedException;
import util.exceptions.ParameterNotSupportedException;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Fabian Widmann
 */
public class UserRepository {
    /**
     * Parses the given json to a new User object, then saves it in the database.
     *
     * @param json body of the request
     * @return newly created user object
     * @throws InvalidInputException          if the password of the user is too short
     * @throws ParameterNotSupportedException if a group is passed as this is not supported here
     */
    public static User createUser(JsonNode json) throws InvalidInputException, ParameterNotSupportedException {

        ObjectMapper mapper = new ObjectMapper();

        if (json.has(JsonKeys.USER_GROUPS)) {
            throw new ParameterNotSupportedException("The user could not be created, a user group has to be set via PATCH or PUT. It may not be content of POST.");
        }
        User tmp = mapper.convertValue(json, User.class);
        if (json.has(JsonKeys.USER_AVATAR)) {
            tmp.setAvatar(json.get(JsonKeys.USER_AVATAR).asText());
        }

        String password = json.get(JsonKeys.USER_PASSWORD).asText();
        if (json.has(JsonKeys.USER_PASSWORD) && password.length() >= JsonKeys.USER_PASSWORD_MIN_LENGTH) {
            try {
                // format iterations:salt:hash
                String pwdGen = PasswordUtil.createHash(password);
                Logger.debug("Password generated from input is: " + pwdGen);

                Logger.debug("Validation of password and hash returns: " + PasswordUtil.validatePassword(password, pwdGen));
                tmp.setPassword(pwdGen);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        } else if (json.has(JsonKeys.USER_PASSWORD) && password.length() < JsonKeys.USER_PASSWORD_MIN_LENGTH)
            throw new InvalidInputException("The specified password is too short it has to be " + JsonKeys.USER_PASSWORD_MIN_LENGTH + " characters long.");

        //Checks if the constraints for @email are met via it's isValid method.
        Constraints.EmailValidator emailValidator = new Constraints.EmailValidator();
        if (JsonKeys.debugging) if (JsonKeys.debugging) Logger.debug("json=" + json + " - obj=" + tmp);
        if (emailValidator.isValid(tmp.getEmail()) && tmp.getName().length() >= JsonKeys.USER_NAME_MIN_LENGTH
                && tmp.getPassword().length() >= JsonKeys.USER_PASSWORD_MIN_LENGTH) {
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

    /**
     * Changes one user via it's id, it supports appending or replacing current user groups via url params and handles
     * partial and complete updates.
     *
     * @param id           of the user
     * @param email        of the modifying user
     * @param json         body of the request
     * @param urlParams    url parameters
     * @param updateMethod @return updated user object
     * @throws InvalidInputException
     */
    public static User changeUser(Long id, String email, JsonNode json, Map<String, String[]> urlParams, String updateMethod)
            throws InvalidInputException, NullPointerException, NotAuthorizedException {

        User author = User.find.where().eq(JsonKeys.USER_EMAIL, email).findUnique();
        // get the specific user we want to edit
        User editedUser = User.find.byId(id);
        if (!author.hasPermission(UserOperations.EDIT_USER, editedUser))
            throw new NotAuthorizedException("This user is not authorized to modify the user with this id.");

        boolean appendMode = false;

        if (urlParams.containsKey(RequestKeys.APPEND)) {
            appendMode = Boolean.parseBoolean(urlParams.get(RequestKeys.APPEND)[0]);
        }
        if (JsonKeys.debugging) Logger.debug("Appending mode enabled? " + appendMode);

        if (JsonKeys.debugging) Logger.debug("Update method=" + updateMethod);
        if (updateMethod.equals("PUT") && (!json.has(JsonKeys.USER_EMAIL) || !json.has(JsonKeys.RATING)
                || !json.has(JsonKeys.USER_NAME) || !json.has(JsonKeys.USER_GROUPS) || !json.has(JsonKeys.USER_PASSWORD))) {
            throw new InvalidInputException("Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS);
        }

        // check for new values
        Constraints.EmailValidator emailValidator = new Constraints.EmailValidator();

        if (json.has(JsonKeys.USER_EMAIL) && emailValidator.isValid(json.get(JsonKeys.USER_EMAIL).asText())) {
            User checkEmail = findUserByEmail(json.get(JsonKeys.USER_EMAIL).asText());
            Logger.debug("uCheck="+checkEmail);
            Logger.debug("author="+author);
            if (JsonKeys.debugging) Logger.debug("does email exist? " + checkEmail);
            if (checkEmail == null)
                editedUser.setEmail(json.get(JsonKeys.USER_EMAIL).asText());
            else if (author.getEmail().equals(checkEmail.getEmail())) {
                Logger.debug("same email");
            } else
                throw new InvalidInputException("The specified email can not be used to update this user.");

        }
        Constraints.MinLengthValidator minLengthValidator = new Constraints.MinLengthValidator();

        if (json.has(JsonKeys.USER_PASSWORD) && minLengthValidator.isValid(json.get(JsonKeys.USER_PASSWORD).asText())) {
            String password = json.get(JsonKeys.USER_PASSWORD).asText();
            Logger.debug("Pass=" + password);

            try {
                // format iterations:salt:hash
                String pwdGen = PasswordUtil.createHash(password);
                Logger.debug("Password generated from input is: " + pwdGen);

                Logger.debug("Validation of password and hash returns: " + PasswordUtil.validatePassword(password, pwdGen));
                editedUser.setPassword(pwdGen);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        } else if (json.has(JsonKeys.USER_PASSWORD) && !minLengthValidator.isValid(json.get(JsonKeys.USER_PASSWORD).asText()))
            throw new InvalidInputException("The specified password is too short it has to be " + JsonKeys.USER_PASSWORD_MIN_LENGTH + " characters long.");

        if (json.has(JsonKeys.RATING))
            editedUser.setRating(json.get(JsonKeys.RATING).asInt());

        if (json.has(JsonKeys.USER_NAME) && minLengthValidator.isValid(json.get(JsonKeys.USER_NAME).asText()))
            editedUser.setName(json.get(JsonKeys.USER_NAME).asText());
        if (json.has(JsonKeys.USER_GROUPS)) {
            if (appendMode) {
                if (JsonKeys.debugging) Logger.debug("Found group");
                List<UserGroup> mergedGroups = new ArrayList<UserGroup>();

                //add all old valid information
                mergedGroups.addAll(editedUser.getUserGroups());
                //retrieve new, check if not in list already
                for (UserGroup ug :
                        UserGroupRepository.retrieveGroups(json)) {
                    if (!mergedGroups.contains(ug)) {
                        mergedGroups.add(ug);
                    }
                }
                if (JsonKeys.debugging) Logger.debug("New groups: " + mergedGroups);
                editedUser.setUserGroups(mergedGroups);
            } else {
                editedUser.setUserGroups(UserGroupRepository.retrieveGroups(json));
            }
        }
        if (json.has(JsonKeys.USER_AVATAR)) {
            if (JsonKeys.debugging) if (JsonKeys.debugging) Logger.debug("avatar=" + json.get(JsonKeys.USER_AVATAR));
            editedUser.setAvatar(json.get(JsonKeys.USER_AVATAR).asText());
        }

        editedUser.update();
        return editedUser;
    }

    /**
     * Deletes a User object from the database if the user has the rights to do this.
     *
     * @param id    of the user
     * @param email of the modifying user
     */
    public static void deleteUserById(Long id, String email) throws NotAuthorizedException {
        User u = User.find.where().eq(JsonKeys.USER_EMAIL, email).findUnique();
        if (u.hasPermission(UserOperations.DELETE_USER, u))
            User.find.ref(id).delete();
        else
            throw new NotAuthorizedException("This user is not authorized to delete the user with this id.");
    }

    /**
     * Returns one User by email instead of id.
     *
     * @param email of the user we look for
     * @return user or null
     */
    public static User findUserByEmail(String email) {
        return User.find.where().eq(JsonKeys.USER_EMAIL, email).findUnique();
    }

    /**
     * Returns one user by id.
     *
     * @param id of the user
     * @return user or null
     */
    public static User findById(Long id) {
        return User.find.byId(id);
    }

    /**
     * Get all users, all users with a specific name or all users with the same email (should not happen).
     *
     * @param urlParams parameters of the requests url
     * @return list of users
     */
    public static List<User> getUsers(Map<String, String[]> urlParams) {
        if (urlParams.containsKey(RequestKeys.EMAIL)) {
            String email = urlParams.get(RequestKeys.EMAIL)[0];
            return User.find.where().eq(JsonKeys.USER_EMAIL, email).findList();
        }
        if (urlParams.containsKey(RequestKeys.NAME)) {
            String name = urlParams.get(RequestKeys.NAME)[0];
            Logger.debug("name=" + name);

            return User.find.where().eq(JsonKeys.USER_NAME, name).findList();
        } else {
            List<User> u = User.find.all();
            return User.find.all();
        }
    }

    /**
     * Retrieve users from one jsonnode.
     *
     * @param nodes body of the request
     * @return list of users or empty list
     */
    public static List<User> retrieveUsers(JsonNode nodes) {
        List<User> userList = new ArrayList<>();
        for (JsonNode node : nodes) {
            User tmpUser = parseUser(node);
            if (tmpUser != null)
                userList.add(tmpUser);
        }
        return userList;
    }

    /**
     * parse each node to a user object. Then find the user by id or email and return it.
     *
     * @param node one json node that contains either user id or email
     * @return User object
     */
    public static User parseUser(JsonNode node) {
        User u = null;
        if (node.has(JsonKeys.USER_ID))
            u = User.find.byId(node.get(JsonKeys.USER_ID).asLong());

        if (node.has(JsonKeys.USER_EMAIL))
            u = UserRepository.findUserByEmail(node.get(JsonKeys.USER_EMAIL).asText());
        return u;
    }
}
