package controllers;

import java.util.List;

import models.*;
import play.api.mvc.Flash;
import play.data.validation.Constraints;
import util.JsonKeys;
import util.JsonWrap;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserController extends Controller {
    public Result getUserIndex(){
        return ok(JsonWrap.prepareJsonStatus(OK,"ok!"));
    }

    /**
     * Return all users in the database.
     * @return HTTP Status OK with a list of all users.
     */
	public Result getUserList() {
		List<User> u = User.find.all();
		return ok(JsonWrap.getJson(u));
	}



	/**
	 * Either PATCHes single values or PUTs all values into the entity with the specified id.
	 * @return HTTP Status ok when everything works out or badRequest if not.
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public Result updateUser(Long id) {
        try {
            JsonNode json = request().body().asJson();

            if (request().method().equals("PUT") && !json.has(JsonKeys.USER_EMAIL) || !json.has(JsonKeys.RATING)
                    || !json.has(JsonKeys.USER_NAME) || !json.has(JsonKeys.USER_GROUP) || !json.has(JsonKeys.USER_PASSWORD))
                return badRequest(JsonWrap.prepareJsonStatus(BAD_REQUEST,
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
                    return badRequest(JsonWrap
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
                System.out.println("groupnode: " + json.get(JsonKeys.USER_GROUP));
                Long groupId = json.get(JsonKeys.USER_GROUP).get(JsonKeys.GROUP_ID).asLong();
                UserGroup group = UserGroup.find.byId(groupId);
                System.out.println("setting group to gid=" + groupId + ", group=" + group);
                u.setGroup(group);

            }
            u.update();
            return ok(JsonWrap.prepareJsonStatus(OK, "User with id=" + id
                    + " has been succesfully changed."));
        }
        catch (IllegalArgumentException e) {
            return badRequest(JsonWrap
                    .prepareJsonStatus(
                            BAD_REQUEST,
                            "Body did contain elements that are not allowed/expected. A user can contain: "+JsonKeys.USER_JSON_ELEMENTS));
        }

	}

    /**
     * Returns the user with a specific ID.
     * @param id
     * @return HTTP Status Result OK if found or NOT_FOUND if not found.
     */
	public Result getUser(Long id) {
		// Find a task by ID
		User u = User.find.byId(id);
		if (u == null)
			return notFound(JsonWrap.prepareJsonStatus(NOT_FOUND,
					"The user with the id=" + id + " could not be found."));
		System.out.println(u+ "| USER_NAME Key="+JsonKeys.USER_NAME);
		return ok(JsonWrap.getJson(u));
	}

    /**
     * Returns the user with a specific (unique!) Email.
     * @param email
     * @return OK when found, NOT_FOUND if it doesnt exist.
     */
	public Result getUserByEmail(String email) {
		// Find a task by ID
		User u = User.find.where().eq(JsonKeys.USER_EMAIL, email).findUnique();
        if (u == null)
            return notFound(JsonWrap.prepareJsonStatus(NOT_FOUND,
                    "The user with the email=" + email + " could not be found."));
		System.out.println(u);
		return ok(JsonWrap.getJson(u));
	}

    /**
     * Deletes a user with the given id.
     * @param id
     * @return OK
     */
	public Result deleteUser(Long id) {
        List<Answer> givenAnswers= Answer.find.where().eq("author_id", id).findList();
        System.out.println("Answers from the user has size="+givenAnswers.size());

        for(Answer a: givenAnswers){
            System.out.println(">> Trying to delete answer a="+a+" where author was: "+a.getAuthor());
            a.delete();
        }


        List<FlashCard> cards=FlashCard.find.where().eq("author_id",id).findList();
        System.out.println("Created cards list has size="+cards.size());

        for(FlashCard c: cards){
            System.out.println(">> Trying to delete card c="+c+" where author was: "+c.getAuthor());
            c.delete();
        }


        List<Question> questions=Question.find.where().eq("author_id",id).findList();
        System.out.println("Questions from the user has size="+questions.size());
        for(Question q: questions){
            System.out.println(">> Trying to delete question q="+q+" where author was: "+q.getAuthor());
            q.delete();
        }

//        User u= User.find.byId(id);
//
//        UserGroup group = u.getGroup();
//        u.setGroup(null);
//        u.update();
//        group.removeUser(User.find.byId(id));
		User.find.ref(id).delete();

		return ok(JsonWrap.prepareJsonStatus(OK, "The user with the id=" + id
				+ " has been deleted. This includes questions, answers and cards mady by this user."));
	}

	/**
	 * Adds a new user to the database, throws an error if the email, name or
	 * password are missing.
	 * 
	 * @return
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public  Result addUser() {
        try {
            JsonNode json = request().body().asJson();
            ObjectMapper mapper = new ObjectMapper();
            if (json.has(JsonKeys.USER_GROUP)) {
                return forbidden(JsonWrap.prepareJsonStatus(FORBIDDEN,
                        "The user could not be created, a user group has to be set via PATCH or PUT. It may not be content of POST."));
            }
            User tmp = mapper.convertValue(json, User.class);

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
                    return created(JsonWrap.prepareJsonStatus(CREATED, "User with id=" + u.getId()
                            + " has been created."));
                }
            }
            return forbidden(JsonWrap.prepareJsonStatus(FORBIDDEN,
                    "The user could not be created, please specify email, name, password. " +
                            "Email has to be valid (e.g. a@b.com)"));
        }
        catch (IllegalArgumentException e) {
            return badRequest(JsonWrap
                    .prepareJsonStatus(
                            BAD_REQUEST,
                            "Body did contain elements that are not allowed/expected. A user can contain: "+JsonKeys.USER_JSON_ELEMENTS));
        }
	}

}
