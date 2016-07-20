package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.User;
import models.UserGroup;
import play.data.Form;
import play.mvc.*;
import util.JsonKeys;
import util.JsonWrap;

public class UserGroupController extends Controller {

	/**
	 * Returns all groups in the JSON format. Optional URL Paramerer is "?empty" which can either be true or false and
	 * only returns groups that contain or do not contain users.
	 *
	 * @return
	 */
	public Result getUserGroupList() {
		Map<String, String[]> urlParams = Controller.request().queryString();
		if(urlParams.keySet().contains("empty")){
			System.out.println("only print empty or nonempty groups");
			//only print the first val we get for the key, this is possible as /groups?empty=true&empty=false could return
			//multiple values.
			if(urlParams.get("empty")[0].equals("true")){
				// TODO: 27/06/16 check why the opposite of UserGroup.find.where().isNotNull(JsonKeys.GROUP_USERS).findList() does not work for this. Always returns 0.
				List<UserGroup> nonEmptyGroups=UserGroup.find.where().isNotNull(JsonKeys.GROUP_USERS).findList();
				List<UserGroup> emptyGroups=UserGroup.find.all();
				emptyGroups.removeAll(nonEmptyGroups);
				return ok(JsonWrap.getJson(emptyGroups));
			}
			else{
				return ok(JsonWrap.getJson(UserGroup.find.where().isNotNull(JsonKeys.GROUP_USERS).findList()));
			}
		}
		for(UserGroup ug:UserGroup.find.all()){
			System.out.print("id="+ug.getId()+" | users size="+ug.getUsers().size()+" | users is null? "+(ug.getUsers()==null)+" | users=");
			for(User u: ug.getUsers()){
				System.out.print("["+u+"];");
			}
			System.out.println();
		}
		return ok(JsonWrap.getJson(UserGroup.find.all()));
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
	 * JsonKeys.GROUP_DESCRIPTION as Strings or JsonKeys.GROUP_USERS as array of UserIds. If anything else
	 * is sent no update will be made. Example Body: { "name": "345",
	 * JsonKeys.GROUP_DESCRIPTION: "345", JsonKeys.GROUP_USERS: [{"JsonKeys.USER_ID": 4}, {"JsonKeys.USER_ID": 5}, ...] }
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
		if(request().method().equals("PUT") && (!json.has(JsonKeys.GROUP_NAME) || !json.has(JsonKeys.GROUP_DESCRIPTION) || !json.has(JsonKeys.GROUP_USERS))){
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
			if (json.has(JsonKeys.GROUP_NAME)) {
				toUpdate.setName(requestGroup.getName());
			}
			if (json.has(JsonKeys.GROUP_DESCRIPTION)) {
				toUpdate.setDescription(requestGroup.getDescription());
			}
			if (json.has(JsonKeys.GROUP_USERS)) {
				JsonNode users = json.findValue(JsonKeys.GROUP_USERS);

				System.out.println("Users=" + users + " isArray? "
						+ users.isArray());
				// Loop through all objects in the values associated with the
				// JsonKeys.GROUP_USERS key.
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
					+ " has been successfully changed. " + information));
		} catch (IllegalArgumentException e) {
			return badRequest(JsonWrap
					.prepareJsonStatus(
							BAD_REQUEST, "Body did contain elements that are not allowed/expected. A group can contain: " + JsonKeys.GROUP_JSON_ELEMENTS));
		}
		catch (NullPointerException e){
			return notFound(JsonWrap.prepareJsonStatus(NOT_FOUND,"Error, no group with id="+id+" exists."));
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
			List<User> userList = null;
			if (json.has(JsonKeys.GROUP_USERS)) {
				//create a new list of users
				userList = new ArrayList<User>();
				//get the specific nods in the json
				JsonNode users = json.findValue(JsonKeys.GROUP_USERS);
				System.out.println("Users=" + users);
				// Loop through all objects in the values associated with the
				// JsonKeys.GROUP_USERS key.
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
							BAD_REQUEST, "Body did contain elements that are not allowed/expected. A group can contain: " + JsonKeys.GROUP_JSON_ELEMENTS));
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
