package controllers;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import models.UserGroup;
import play.Logger;
import play.mvc.*;
import repositories.UserGroupRepository;
import util.ActionAuthenticator;
import util.JsonKeys;
import util.JsonUtil;
import util.exceptions.InvalidInputException;
import util.exceptions.NotAuthorizedException;
import util.exceptions.ObjectNotFoundException;
import util.exceptions.PartiallyModifiedException;

public class UserGroupController extends Controller {

    /**
     * Returns all groups in the JSON format. Optional URL Paramerer is "?empty" which can either be true or false and
     * only returns groups that contain or do not contain users.
     *
     * @return ok including a json node that contains all groups
     */
    public Result getUserGroupList() {

        Map<String, String[]> urlParams = Controller.request().queryString();
        try{
            return ok(JsonUtil.toJson(UserGroupRepository.getGroups(urlParams)));

        }catch (NumberFormatException e){
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "Error while parsing the specified numbers, please recheck your request."));
        }
    }

    public Result getDecksFromGroup(Long id) {
        try {
            return ok(JsonUtil.toJson(UserGroupRepository.getDecks(id)));

        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND,
                    "The group with the id=" + id + " could not be found."));
        }
    }

    public Result getUsersInUserGroup(Long id) {
        try {
            return ok(JsonUtil.toJson(UserGroupRepository.getUsers(id)));

        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND,
                    "The group with the id=" + id + " could not be found."));
        }
    }

    /**
     * Returns a specific UserGroup in the JSON format.
     *
     * @param id - GroupID of the group we want to get.
     * @return ok plus the group or notFound when the group does not exist.
     */
    public Result getUserGroup(long id) {
        UserGroup group = UserGroupRepository.getGroup(id);
        if (group == null)
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND,
                    "The group with the id=" + id + " could not be found."));

        return ok(JsonUtil.toJson(group));
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
    @Security.Authenticated(ActionAuthenticator.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateUserGroup(Long id) {
        if (JsonKeys.debugging) Logger.debug(request().method());
        JsonNode json = request().body().asJson();
        Map<String, String[]> urlParams = Controller.request().queryString();
        String updateMethod = request().method();
        UserGroup userGroup;
        try {
            userGroup = UserGroupRepository.changeUserGroup(id, request().username(),json, urlParams, updateMethod);
        } catch (IllegalArgumentException e) {
            return badRequest(JsonUtil
                    .prepareJsonStatus(
                            BAD_REQUEST, "Body did contain elements that are not allowed/expected. A group can contain: " + JsonKeys.GROUP_JSON_ELEMENTS));
        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, group does not exist", id));
        } catch (InvalidInputException e) {
            //e.printStackTrace();
            if (JsonKeys.debugging) {
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, e.getMessage() + " | cause: " + e.getCause()));
            } else {
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, e.getMessage()));
            }
        } catch (PartiallyModifiedException e) {
            return ok(JsonUtil.prepareJsonStatus(OK, e.getMessage()));
        } catch (NotAuthorizedException e) {
            return unauthorized(JsonUtil.prepareJsonStatus(UNAUTHORIZED, e.getMessage(), id));
        }

        return ok(JsonUtil.prepareJsonStatus(200, "Group has been successfully changed. " + userGroup, id));
    }

    /**
     * Adds a new UserGroup and modifies the groups of all users that were found in the body's JsonKeys.GROUP_USERS array element via id.
     * Result is the creation of the group and changes to all users that will new be connected to this group instead.
     *
     * @return either ok with the id of the group, or bad_request with an explanation
     */
    @Security.Authenticated(ActionAuthenticator.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result addUserGroup() {
        JsonNode json = request().body().asJson();

        UserGroup userGroup;
        try {
            userGroup = UserGroupRepository.addUserGroup(json);
        } catch (IllegalArgumentException e) {
            //e.printStackTrace();
            if (JsonKeys.debugging) {
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, e.getMessage() + " | cause: " + e.getCause()));
            } else {
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, e.getMessage()));
            }
        } catch (ObjectNotFoundException e) {
            if (e.getObjectId() > 0) {
                return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, e.getMessage(), e.getObjectId()));
            }
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, e.getMessage()));
        }
        return created(JsonUtil.prepareJsonStatus(CREATED, "Usergroup has been created!", userGroup.getId()));

    }

/*    *//**
     * Grabs the group by its id, deletes the reference to this group from all members and updates them and then deletes the group.
     *
     * @param id of a user
     * @return ok when deletion is successful, notFound if the user does not exist.
     *//*
    @Security.Authenticated(ActionAuthenticator.class)
    public Result deleteUserGroup(long id) {
        try {
            UserGroupRepository.deleteUserGroup(id, request().username());
            return ok(JsonUtil.prepareJsonStatus(OK, "The group has been deleted."));
        } catch (NullPointerException e) {
            e.printStackTrace();
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Group with the id could not be found", id));
        } catch (NotAuthorizedException e) {
            return unauthorized(JsonUtil.prepareJsonStatus(UNAUTHORIZED, e.getMessage(), id));
        }
    }   */

    /*
     * @param id of a user
     * @return ok when deletion is successful, notFound if the user does not exist.
     */
    @Security.Authenticated(ActionAuthenticator.class)
    public Result unSubscribe(long id) {
        try {
            boolean isUnsubscribed = UserGroupRepository.unSubscribe(id, request().username());
            if(isUnsubscribed)
                return ok(JsonUtil.prepareJsonStatus(OK, "User unsubscribed from the group.",id));
            else
                return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "User is no member of this group.",id));
        } catch (NullPointerException e) {
            e.printStackTrace();
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Group with the id could not be found", id));
        }
    }


}
