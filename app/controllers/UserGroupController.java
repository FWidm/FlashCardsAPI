package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.User;
import models.UserGroup;
import play.Logger;
import play.mvc.*;
import util.JsonKeys;
import util.JsonUtil;
import util.RequestKeys;

public class UserGroupController extends Controller {

	/**
	 * Returns all groups in the JSON format. Optional URL Paramerer is "?empty" which can either be true or false and
	 * only returns groups that contain or do not contain users.
	 *
	 * @return ok including a json node that contains all groups
	 */
	public Result getUserGroupList() {
		Map<String, String[]> urlParams = Controller.request().queryString();
		if(urlParams.keySet().contains(RequestKeys.EMPTY)){
			if(JsonKeys.debugging)Logger.debug("only print empty or nonempty groups");
			//only print the first val we get for the key, this is possible as /groups?empty=true&empty=false could return
			//multiple values.
			if(urlParams.get(RequestKeys.EMPTY)[0].equals("true")){
				// TODO: 27/06/16 check why the opposite of UserGroup.find.where().isNotNull(JsonKeys.GROUP_USERS).findList() does not work for this. Always returns 0.
				List<UserGroup> nonEmptyGroups=UserGroup.find.where().isNotNull(JsonKeys.GROUP_USERS).findList();
				List<UserGroup> emptyGroups=UserGroup.find.all();
				emptyGroups.removeAll(nonEmptyGroups);
				return ok(JsonUtil.getJson(emptyGroups));
			}
			else{
				return ok(JsonUtil.getJson(UserGroup.find.where().isNotNull(JsonKeys.GROUP_USERS).findList()));
			}
		}
		for(UserGroup ug:UserGroup.find.all()){
			if(JsonKeys.debugging)Logger.debug("id="+ug.getId()+" | users size="+ug.getUsers().size()+" | users is null? "+(ug.getUsers()==null)+" | users=");
			if(JsonKeys.debugging)ug.getUsers().forEach(user -> Logger.debug("["+user+"];"));
		}
		return ok(JsonUtil.getJson(UserGroup.find.all()));
	}

	/**
	 * Returns a specific UserGroup in the JSON format.
	 *
	 * @param id
	 *            - GroupID of the group we want to get.
	 * @return ok plus the group or notFound when the group does not exist.
	 */
	public Result getUserGroup(long id) {
		UserGroup group = UserGroup.find.byId(id);
		if (group == null)
			return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND,
					"The group with the id=" + id + " could not be found."));

		return ok(JsonUtil.getJson(group));
	}

	/**
	 * Updates the specified UserGroup. The Json Body can contain "name",
	 * JsonKeys.GROUP_DESCRIPTION as Strings or JsonKeys.GROUP_USERS as array of UserIds. If anything else
	 * is sent no update will be made. Example Body: { "name": "345",
	 * JsonKeys.GROUP_DESCRIPTION: "345", JsonKeys.GROUP_USERS: [{"JsonKeys.USER_ID": 4}, {"JsonKeys.USER_ID": 5}, ...] }
	 *
	 * @param id GroupID of the group we want to update
	 * @return either ok or bad_request with an explanation
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public Result updateUserGroup(Long id) {
		if(JsonKeys.debugging)Logger.debug(request().method());
		String information = "";
		JsonNode json = request().body().asJson();
		ObjectMapper mapper = new ObjectMapper();

		//Check whether the request was a put and if it was check if a param is missing, if that is the case --> bad req.
		if(request().method().equals("PUT") && (!json.has(JsonKeys.GROUP_NAME) || !json.has(JsonKeys.GROUP_DESCRIPTION) || !json.has(JsonKeys.GROUP_USERS))){
			return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST,
					"The Update method needs all details of the group, such as name, " +
							"description and a user group (array of users or null). An attribute was missing for id="
							+ id + "."));
		}

		try {
			UserGroup requestGroup = mapper.convertValue(json, UserGroup.class);
			UserGroup toUpdate = UserGroup.find.byId(id);

			if(JsonKeys.debugging)Logger.debug("Update group with details: " + requestGroup
					+ "\n JSON Size=" + json.size());

			if (json.size() == 0)
				return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST,
						"No Json body was found. This is required to update the group with id="
								+ id + "."));
			// check for new values
			if (json.has(JsonKeys.GROUP_NAME)) {
				toUpdate.setName(requestGroup.getName());
			}
			if (json.has(JsonKeys.GROUP_DESCRIPTION)) {
				toUpdate.setDescription(requestGroup.getDescription());
			}
			if (json.has(JsonKeys.GROUP_USERS)) {
				JsonNode users = json.findValue(JsonKeys.GROUP_USERS);

				if(JsonKeys.debugging)Logger.debug("Users=" + users + " isArray? "
						+ users.isArray());
				// Loop through all objects in the values associated with the
				// JsonKeys.GROUP_USERS key.
				for (JsonNode n : users) {
					// when a user id is found we will get the object and
					// update the usergroup.
					if (n.has(JsonKeys.USER_ID)) {
						User u = User.find.byId(n.get(JsonKeys.USER_ID).asLong());
						//u.setUserGroups(toUpdate);
						u.update();
					} else {
						information += "No ID found in the json node=" + n
								+ ". ";
					}

				}
			}

			toUpdate.update();
			return ok(JsonUtil.prepareJsonStatus(200, "Group has been successfully changed. " + information,id));
		} catch (IllegalArgumentException e) {
			return badRequest(JsonUtil
					.prepareJsonStatus(
							BAD_REQUEST, "Body did contain elements that are not allowed/expected. A group can contain: " + JsonKeys.GROUP_JSON_ELEMENTS));
		}
		catch (NullPointerException e){
			return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND,"Error, group does not exist",id));
		}
	}

	/**
	 * Adds a new UserGroup and modifies the groups of all users that were found in the body's JsonKeys.GROUP_USERS array element via id.
	 * Result is the creation of the group and changes to all users that will new be connected to this group instead.
	 * @return either ok with the id of the group, or bad_request with an explanation
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public Result addUserGroup() {
		try {
			JsonNode json = request().body().asJson();
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
				if(users!=null){
				    if(JsonKeys.debugging)Logger.debug("Users=" + users);

				// Loop through all objects in the values associated with the
				// JsonKeys.GROUP_USERS key.
				for (JsonNode n : users) {
					if (n.has(JsonKeys.USER_ID)) {
						User u = User.find.byId(n.get(JsonKeys.USER_ID).asLong());
						if(u!=null)
						    userList.add(u);
                        else
                            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "One user could not be found.",n.get(JsonKeys.USER_ID).asLong()));
					}
                    else
                        return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "One user that was specified did not contain an id."));
				}
				//set the list for the group created from the content of the json body
				if(JsonKeys.debugging) Logger.debug("Adding users to the group: "+userList);
                    group.setUsers(userList);

                }
			}
			group.save();
			if(JsonKeys.debugging)Logger.debug("group="+group);

			return ok(JsonUtil.prepareJsonStatus(OK, "Usergroup has been created!",group.getId()));
		} catch (IllegalArgumentException e) {
		    e.printStackTrace();
			return badRequest(JsonUtil
					.prepareJsonStatus(
							BAD_REQUEST, "Body did contain elements that are not allowed/expected. A group can contain: " + JsonKeys.GROUP_JSON_ELEMENTS));
		}
	}

	/**
	 * Grabs the group by its id, deletes the reference to this group from all members and updates them and then deletes the group.
	 * @param id of a user
	 * @return ok when deletion is successful, notFound if the user does not exist.
	 */
	public Result deleteUserGroup(long id){
	    try {
            UserGroup group = UserGroup.find.byId(id);
            group.update();
            group.delete();
            return ok(JsonUtil.prepareJsonStatus(OK, "The group has been deleted."));
        }
        catch (NullPointerException e){
            e.printStackTrace();
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND,"Group with the id could not be found",id));
        }
	}
}
