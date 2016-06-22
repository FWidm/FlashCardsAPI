package controllers;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.User;
import models.UserGroup;
import play.mvc.*;
import util.JsonKeys;
import util.JsonWrap;

public class UserGroupController extends Controller {

	/**
	 * Returns all groups in the JSON format.
	 * 
	 * @return
	 */
	public Result getUserGroupList() {
		List<UserGroup> g = UserGroup.find.all();
		return ok(JsonWrap.getJson(g));
	}

	/**
	 * Returns a specific UserGroup in the JSON format.
	 * 
	 * @param id
	 *            - GroupID of the group we want to get.
	 * @return
	 */
	public Result getUserGroup(long id) {
		UserGroup group = UserGroup.find.byId(id);
		if (group == null)
			return notFound(JsonWrap.prepareJsonStatus(NOT_FOUND,
					"The group with the id=" + id + " could not be found."));

		return ok(JsonWrap.getJson(group));
	}

	/**
	 * Updates the specified UserGroup. The Json Body can contain "name",
	 * "description" as Strings or "users" as array of UserIds. If anything else
	 * is sent no update will be made. Example Body: { "name": "345",
	 * "description": "345", "users": [{"JsonKeys.USER_ID": 4}, {"JsonKeys.USER_ID": 5}, ...] }
	 * 
	 * @param id GroupID of the group we want to update
	 * @return either ok or bad_request with an explanation
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public Result updateUserGroup(Long id) {
        System.out.println(request().method());
		String information = "";
		JsonNode json = request().body().asJson();
		ObjectMapper mapper = new ObjectMapper();

        //Check whether the request was a put and if it was check if a param is missing, if that is the case --> bad req.
        if(request().method().equals("PUT") && (!json.has("name") || !json.has("description") || !json.has("users"))){
            return badRequest(JsonWrap.prepareJsonStatus(BAD_REQUEST,
                    "The Update method needs all details of the group, such as name, " +
                            "description and a user group (array of users or null). An attribute was missing for id="
                            + id + "."));
        }

		try {
			UserGroup requestGroup = mapper.convertValue(json, UserGroup.class);
			UserGroup toUpdate = UserGroup.find.byId(id);

			System.out.println("Update group with details: " + requestGroup
					+ "\n JSON Size=" + json.size());

			if (json.size() == 0)
				return badRequest(JsonWrap.prepareJsonStatus(BAD_REQUEST,
						"No Json body was found. This is required to update the group with id="
								+ id + "."));
			// check for new values
			if (json.has("name")) {
				toUpdate.setName(requestGroup.getName());
			}
			if (json.has("description")) {
				toUpdate.setDescription(requestGroup.getDescription());
			}
			if (json.has("users")) {
				JsonNode users = json.findValue("users");

				System.out.println("Users=" + users + " isArray? "
						+ users.isArray());
				// Loop through all objects in the values associated with the
				// "users" key.
				for (JsonNode n : users) {
					// when a user id is found we will get the object and
					// update the usergroup.
					if (n.has(JsonKeys.USER_ID)) {
						User u = User.find.byId(n.get(JsonKeys.USER_ID).asLong());
						u.setGroup(toUpdate);
						u.update();
					} else {
						information += "No ID found in the json node=" + n
								+ ". ";
					}

				}
			}
			toUpdate.update();
			return ok(JsonWrap.prepareJsonStatus(200, "Group with id=" + id
					+ " has been succesfully changed. " + information));
		} catch (IllegalArgumentException e) {
			return badRequest(JsonWrap
					.prepareJsonStatus(
							BAD_REQUEST,
							"Body did contain elements that are not allowd in a group. No Update could be made to the group with id="
									+ id
									+ ". Expected/Available Updates are name:String, description:String and users:Array with elements that contain user-ids"));
		}
	}

	/**
	 * Adds a new UserGroup and modifies the groups of all users that were found in the body's "users" array element via id.
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
			List<User> userList = null;
			if (json.has("users")) {
				//create a new list of users
				userList = new ArrayList<User>();
				//get the specific nods in the json
				JsonNode users = json.findValue("users");
				System.out.println("Users=" + users);
				// Loop through all objects in the values associated with the
				// "users" key.
				for (JsonNode n : users) {
					// when a user id is found we will get the object and add them to the userList.
                    Long l=n.get(JsonKeys.USER_ID).asLong();
                    System.out.println("User id="+l+" found for node="+n);
					if (n.has(JsonKeys.USER_ID)) {
						User u = User.find.byId(l);
						userList.add(u);
					} 
				}
                //set the list for the group created from the content of the json body
                System.out.println("Adding users to the group: "+userList);
			}

            UserGroup group = new UserGroup(requestGroup);
            group.save();
            group.setUsers(userList);
			System.out.println(group);

			return ok(JsonWrap.prepareJsonStatus(OK, "Usergroup with the id="
					+ group.getId() + " has been created!"));
		} catch (IllegalArgumentException e) {
			return badRequest(JsonWrap
					.prepareJsonStatus(
							BAD_REQUEST,
							"Body did contain elements that are not allowd in a group. Expected/Available Updates are name:String, description:String and users:Array with elements that contain user-ids"));
		}
	}
	
	/**
	 * Grabs the group by its id, deletes the reference to this group from all members and updates them and then deletes the group.
	 * @param id
	 * @return
	 */
	public Result deleteUserGroup(long id){
		UserGroup group = UserGroup.find.byId(id);
		String information="";
		//check if any conflicts may exist if the group is deleted and reset the user's association with the group before deleting.
		for(User u : group.getUsers()){
			System.out.println("Detaching user u="+u+" from the group!");
			u.setGroup(null);
			u.update();
			information+=" userID="+u.getId()+"; ";
		}
		group.delete();
		return ok(JsonWrap.prepareJsonStatus(OK, "The group with the id=" + id
				+ " has been deleted. Additionally members with the following ids are now without groups: "+information));
	}
}
