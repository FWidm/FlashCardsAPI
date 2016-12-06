package repositories;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.CardDeck;
import models.User;
import models.UserGroup;
import play.Logger;
import util.JsonKeys;
import util.RequestKeys;
import util.exceptions.InvalidInputException;
import util.exceptions.ObjectNotFoundException;
import util.exceptions.PartiallyModifiedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
public class UserGroupRepository {
    /**
     * Retrieve all Users from one group.
     * @param id
     * @return list of Users
     */
    public static List<User>  getUsers(Long id)throws NullPointerException{
        List<User> users=UserGroup.find.byId(id).getUsers();
        return  users;
    }

    /**
     * Retrieve all Carddecks from one group.
     * @param id
     * @return list of CardDecks
     */
    public static List<CardDeck>  getDecks(Long id) throws NullPointerException{
        List<CardDeck> decks=UserGroup.find.byId(id).getDecks();
        return  decks;
    }
    /**
     * Returns all models of type group, this method will return a filtered list if the RequestKeys.EMPTY url parameter is sent with value true or false.
     *
     * @param urlParams
     * @return List of UserGroups
     */
    public static List<UserGroup> getGroups(Map<String, String[]> urlParams) {
        if (urlParams.keySet().contains(RequestKeys.EMPTY)) {
            if (JsonKeys.debugging)
                Logger.debug("only print empty or nonempty groups");
            //only print the first val we get for the key, this is possible as /groups?empty=true&empty=false could return
            //multiple values.
            if (urlParams.get(RequestKeys.EMPTY)[0].equals("true")) {
                List<UserGroup> nonEmptyGroups = UserGroup.find.where().isNotNull(JsonKeys.GROUP_USERS).findList();
                List<UserGroup> emptyGroups = UserGroup.find.all();
                emptyGroups.removeAll(nonEmptyGroups);
                return emptyGroups;
            } else {
                return UserGroup.find.where().isNotNull(JsonKeys.GROUP_USERS).findList();
            }
        }
        return UserGroup.find.all();
    }

    /**
     * Returns the user grip with a given ID
     * @param id
     * @return
     */
    public static UserGroup getGroup(long id) {
        return UserGroup.find.byId(id);
    }

    /**
     * Adds a new UserGroup. Can throw an exception when users should be added that either do not exist or have no id in their json.
     * @param json
     * @return
     * @throws ObjectNotFoundException
     * @throws IllegalArgumentException
     */
    public static UserGroup addUserGroup(JsonNode json) throws ObjectNotFoundException, IllegalArgumentException {
            ObjectMapper mapper = new ObjectMapper();

            UserGroup requestGroup = mapper.convertValue(json, UserGroup.class);
            //we do not want the app to send complete users, thus the mapper cant create the list from itself.
            List<User> userList;
            UserGroup group = new UserGroup(requestGroup);
            if (json.has(JsonKeys.GROUP_USERS)) {
                //create a new list of users
                userList = new ArrayList<>();
                //get the specific nods in the json
                JsonNode users = json.findValue(JsonKeys.GROUP_USERS);
                if (users != null) {
                    if (JsonKeys.debugging) Logger.debug("Users=" + users);

                    // Loop through all objects in the values associated with the
                    // JsonKeys.GROUP_USERS key.
                    for (JsonNode n : users) {
                        if (n.has(JsonKeys.USER_ID)) {
                            User u = User.find.byId(n.get(JsonKeys.USER_ID).asLong());
                            if (u != null)
                                userList.add(u);
                            else
                                throw new ObjectNotFoundException("User does not exist: ",n.get(JsonKeys.USER_ID).asLong());
                        } else
                            throw new ObjectNotFoundException("One user that was specified did not contain an id.");
                    }
                    //set the list for the group created from the content of the json body
                    if (JsonKeys.debugging) Logger.debug("Adding users to the group: " + userList);
                    group.setUsers(userList);

                }
            }
            group.save();
            if (JsonKeys.debugging) Logger.debug("group=" + group);

            return group;
    }

    /**
     * Updates a Usergroup depending on the method used and the contents of the json. Returns the updated resource.
     * @param id
     * @param json
     * @param urlParams
     * @param method
     * @return updated UserGroup object
     * @throws InvalidInputException
     * @throws NullPointerException
     * @throws PartiallyModifiedException
     */
    public static UserGroup changeUserGroup(long id, JsonNode json, Map<String, String[]> urlParams, String method)
            throws InvalidInputException, NullPointerException, PartiallyModifiedException {
        String information = "";

        ObjectMapper mapper = new ObjectMapper();

        //Check whether the request was a put and if it was check if a param is missing, if that is the case --> bad req.
        if (method.equals("PUT") && (!json.has(JsonKeys.GROUP_NAME) || !json.has(JsonKeys.GROUP_DESCRIPTION) || !json.has(JsonKeys.GROUP_USERS))) {
            throw new InvalidInputException("The Update method needs all details of the group, such as name, " +
                    "description and a user group (array of users or null). An attribute was missing.");
        }
        UserGroup requestGroup = mapper.convertValue(json, UserGroup.class);
        UserGroup toUpdate = UserGroup.find.byId(id);

        if (JsonKeys.debugging) Logger.debug("Update group with details: " + requestGroup
                + "\n JSON Size=" + json.size());

        if (json.size() == 0)
            return null;

        // check for new values
        if (json.has(JsonKeys.GROUP_NAME)) {
            toUpdate.setName(requestGroup.getName());
        }
        if (json.has(JsonKeys.GROUP_DESCRIPTION)) {
            toUpdate.setDescription(requestGroup.getDescription());
        }
        if (json.has(JsonKeys.GROUP_USERS)) {
            JsonNode users = json.findValue(JsonKeys.GROUP_USERS);

            if (JsonKeys.debugging) Logger.debug("Users=" + users + " isArray? "
                    + users.isArray());
            // Loop through all objects in the values associated with the
            // JsonKeys.GROUP_USERS key.
            for (JsonNode n : users) {
                // when a user id is found we will get the object and
                // update the usergroup.
                if (n.has(JsonKeys.USER_ID) && User.find.byId(n.get(JsonKeys.USER_ID).asLong())!=null) {
                    User u = User.find.byId(n.get(JsonKeys.USER_ID).asLong());
                    //u.setUserGroups(toUpdate);
                    u.update();
                } else {
                    information += "No ID found or does not exist in json=" + n
                            + ". ";
                }

            }
        }
        toUpdate.update();
        //if we somehow have gotten users that do not exist, we can throw a PartiallyModifiedException to use in the controller.
        if(information.length()>0){
            throw new PartiallyModifiedException("Updated the Group as expected, but some Users passed do not exist. "+information);
        }
        return toUpdate;

    }

    /**
     * Parses the given json to gain access to the given Usergroups.
     *
     * @param json
     * @return
     */
    public static List<UserGroup> retrieveGroups(JsonNode json) {
        List<UserGroup> userGroups = new ArrayList<>();

        //get the specific nods in the json
        JsonNode answersNode = json.findValue(JsonKeys.USER_GROUPS);
        // Loop through all objects in the values associated with the
        // "users" key.
        for (JsonNode node : answersNode) {
            // when a user id is found we will get the object and add them to the userList.
//            System.out.println("Node=" + node);
            if (node.has(JsonKeys.GROUP_ID)) {
                UserGroup found = UserGroup.find.byId(node.get(JsonKeys.GROUP_ID).asLong());
//                System.out.println(">> group: " + found);
                userGroups.add(found);
            } else {
                UserGroup tmpG = parseGroup(node);
//                System.out.println(">> group: " + tmpG);
                userGroups.add(tmpG);
                tmpG.save();
            }
        }
        return userGroups;
    }

    /**
     * Deletes a UserGroup by it's id.
     * @param id
     * @throws NullPointerException
     */
    public static void deleteUserGroup(long id) throws NullPointerException {
        UserGroup group = UserGroup.find.byId(id);
        group.update();
        group.delete();
    }

    /**
     * Create a new UserGroup with the given name and description.
     * @param json
     * @return the usergroup from the json node
     */
    public static UserGroup parseGroup(JsonNode json) {
//        System.out.println("json=" + json);
        String name = null, description = null;

        if (json.has(JsonKeys.GROUP_NAME)) {
            name = json.get(JsonKeys.GROUP_NAME).asText();
        }
        if (json.has(JsonKeys.GROUP_DESCRIPTION)) {
            description = json.get(JsonKeys.GROUP_DESCRIPTION).asText();
        }
        UserGroup group = new UserGroup(name, description, null);
        return group;
    }
}
