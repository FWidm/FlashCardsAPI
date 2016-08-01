package controllers;

import java.util.List;
import java.util.Map;

import models.*;
import play.data.validation.Constraints;
import util.JsonKeys;
import util.JsonUtil;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.RequestKeys;

public class UserController extends Controller {
    public Result getUserIndex() {
        return ok(JsonUtil.prepareJsonStatus(OK, "ok!"));
    }

    /**
     * Return all users in the database.
     *
     * @return HTTP Status OK with a list of all users.
     */
    public Result getUserList() {
        Map<String, String[]> urlParams = Controller.request().queryString();

        if (urlParams.containsKey(RequestKeys.EMAIL)) {
            String email = urlParams.get(RequestKeys.EMAIL)[0];
            User u = User.find.where().eq(JsonKeys.USER_EMAIL, email).findUnique();
            if (u == null)
                return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND,
                        "The user with the email=" + email + " could not be found."));
            System.out.println(u);
            return ok(JsonUtil.getJson(u));
        }

        if (urlParams.containsKey(RequestKeys.NAME)) {
            String name = urlParams.get(RequestKeys.NAME)[0];
            List<User> u = User.find.where().eq(JsonKeys.USER_NAME, name).findList();
            if (u == null)
                return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND,
                        "The user with the name=" + name + " could not be found."));
            System.out.println(u);
            return ok(JsonUtil.getJson(u));
        } else {
            List<User> u = User.find.all();
            return ok(JsonUtil.getJson(u));
        }
    }


    /**
     * Either PATCHes single values or PUTs all values into the entity with the specified id.
     *
     * @return HTTP Status ok when everything works out or badRequest if not.
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateUser(Long id) {
        try {
            JsonNode json = request().body().asJson();
            System.out.println("Update method=" + request().method());
            if (request().method().equals("PUT") && (!json.has(JsonKeys.USER_EMAIL) || !json.has(JsonKeys.RATING)
                    || !json.has(JsonKeys.USER_NAME) || !json.has(JsonKeys.USER_GROUP) || !json.has(JsonKeys.USER_PASSWORD)))
                return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST,
                        "The Update method needs all details of the user, such as email, " +
                                "rating, name, group and password! An attribute was missing for id="
                                + id + "."));


            // get the specific user
            User u = User.find.byId(id);

            // check for new values
            Constraints.EmailValidator emailValidator = new Constraints.EmailValidator();

            if (json.has(JsonKeys.USER_EMAIL) && emailValidator.isValid(json.get(JsonKeys.USER_EMAIL).asText())) {
                User checkEmail = User.find.where()
                        .eq(JsonKeys.USER_EMAIL, json.get(JsonKeys.USER_EMAIL).asText()).findUnique();
                System.out.println("does email exist? " + checkEmail);
                if (checkEmail == null)
                    u.setEmail(json.get(JsonKeys.USER_EMAIL).asText());
                else
                    return badRequest(JsonUtil
                            .prepareJsonStatus(
                                    BAD_REQUEST,
                                    "The server can't fulfill the request, as the specified email " +
                                            "is already in use. Try again with a different email."));
            }
            Constraints.MinLengthValidator minLengthValidator = new Constraints.MinLengthValidator();
            if (json.has(JsonKeys.USER_PASSWORD) && minLengthValidator.isValid(json.get(JsonKeys.USER_PASSWORD).asText())) {
                u.setPassword(json.get(JsonKeys.USER_PASSWORD).asText());
            }
            if (json.has(JsonKeys.RATING))
                u.setRating(json.get(JsonKeys.RATING).asInt());
            if (json.has(JsonKeys.USER_NAME) && minLengthValidator.isValid(json.get(JsonKeys.USER_NAME).asText()))
                u.setName(json.get(JsonKeys.USER_NAME).asText());
            if (json.has(JsonKeys.USER_GROUP)) {
                UserGroup group = null;
                System.err.println("Json Value=" + json.get(JsonKeys.USER_GROUP) + " is empty?" + json.get(JsonKeys.USER_GROUP).size());

                if (json.get(JsonKeys.USER_GROUP).size() > 0 && json.get(JsonKeys.USER_GROUP).has(JsonKeys.GROUP_ID)) {
                    Long groupId = json.get(JsonKeys.USER_GROUP).get(JsonKeys.GROUP_ID).asLong();
                    group = UserGroup.find.byId(groupId);
                    System.out.println("setting group to gid=" + groupId + ", group=" + group);
                }

                u.setGroup(group);

            }
            if (json.has(JsonKeys.USER_AVATAR)) {
                System.out.println(json.get(JsonKeys.USER_AVATAR));
                u.setAvatar(json.get(JsonKeys.USER_AVATAR).asText());
            }
            u.update();
            return ok(JsonUtil.prepareJsonStatus(OK, "User has been changed.", id));
        } catch (IllegalArgumentException e) {
            return badRequest(JsonUtil
                    .prepareJsonStatus(
                            BAD_REQUEST,
                            "Body did contain elements that are not allowed/expected. A user can contain: " + JsonKeys.USER_JSON_ELEMENTS));
        } catch (NullPointerException e) {
            e.printStackTrace();
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no user with the specified id exists.", id));
        }
    }

    /**
     * Returns the user with a specific ID.
     *
     * @param id
     * @return HTTP Status Result OK if found or NOT_FOUND if not found.
     */
    public Result getUser(Long id) {
        User u = User.find.byId(id);
        if (u == null)
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND,
                    "The user with the id=" + id + " could not be found."));
        System.out.println(u + "| USER_NAME Key=" + JsonKeys.USER_NAME);
        return ok(JsonUtil.getJson(u));
    }

    /**
     * Returns the user with a specific (unique!) Email.
     *
     * @param email
     * @return OK when found, NOT_FOUND if it doesnt exist.
     */
    public Result getUserByEmail(String email) {
        // Find a task by ID
        User u = User.find.where().eq(JsonKeys.USER_EMAIL, email).findUnique();
        if (u == null)
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND,
                    "The user with the email=" + email + " could not be found."));
        System.out.println(u);
        return ok(JsonUtil.getJson(u));
    }


    /**
     * Deletes a user with the given id.
     *
     * @param id
     * @return OK
     */
    public Result deleteUser(Long id) {
        try {
            User.find.ref(id).delete();

            return ok(JsonUtil.prepareJsonStatus(OK, "The user has been deleted. All produced content now will be unlinked from this account (author set to null).", id));
        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, user does not exist.", id));
        }
    }

    /**
     * Adds a new user to the database, throws an error if the email, name or
     * password are missing.
     *
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result addUser() {
        try {
            JsonNode json = request().body().asJson();
            ObjectMapper mapper = new ObjectMapper();

            if (json.has(JsonKeys.USER_GROUP)) {
                return forbidden(JsonUtil.prepareJsonStatus(FORBIDDEN,
                        "The user could not be created, a user group has to be set via PATCH or PUT. It may not be content of POST."));
            }
            User tmp = mapper.convertValue(json, User.class);
            if (json.has(JsonKeys.USER_AVATAR)) {
                tmp.setAvatar(json.get(JsonKeys.USER_AVATAR).asText());
            }
            //Checks if the constraints for @email are met via it's isValid method.
            Constraints.EmailValidator emailValidator = new Constraints.EmailValidator();
            Constraints.MinLengthValidator minLengthValidator = new Constraints.MinLengthValidator();
            System.out.println("json=" + json + " - obj=" + tmp);
            if (emailValidator.isValid(tmp.getEmail()) && minLengthValidator.isValid(tmp.getName())
                    && minLengthValidator.isValid(tmp.getPassword())) {
                // if this entry with specified email does not exist, create, else
                // throw an error.
                if (User.find.where().eq(JsonKeys.USER_EMAIL, tmp.getEmail()).findUnique() == null) {
                    User u = new User(tmp);

                    u.save();
                    return created(JsonUtil.prepareJsonStatus(CREATED, "User has been created.", u.getId()));
                }
            }
            return forbidden(JsonUtil.prepareJsonStatus(FORBIDDEN,
                    "The user could not be created, please specify email, name, password. " +
                            "Email has to be valid (e.g. a@b.com)"));
        } catch (IllegalArgumentException e) {
            return badRequest(JsonUtil
                    .prepareJsonStatus(
                            BAD_REQUEST,
                            "Body did contain elements that are not allowed/expected. A user can contain: " + JsonKeys.USER_JSON_ELEMENTS));
        }
    }

}
