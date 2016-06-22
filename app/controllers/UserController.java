package controllers;

import java.util.List;

import models.Answer;
import models.FlashCard;
import models.Question;
import play.api.mvc.Flash;
import play.data.validation.Constraints;
import util.JsonWrap;
import models.User;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserController extends Controller {
    public Result getUserIndex(){
        return ok(JsonWrap.prepareJsonStatus(OK,"ok!"));
    }

	public Result getUserList() {
		List<User> u = User.find.all();
		return ok(JsonWrap.getJson(u));
	}

    /**
     * Partially updates the user when a patch method is used for updating. More infos here: https://tools.ietf.org/html/rfc5789#section-2.1
     * Expects
     * @param id
     * @return
     */
	@BodyParser.Of(BodyParser.Json.class)
	public Result partiallyUpdateUser(Long id){
		JsonNode json = request().body().asJson();
        boolean modified = false;
		if (json.size() == 0)
			return badRequest(JsonWrap.prepareJsonStatus(BAD_REQUEST,
					"No Json body was found. This is required to update the user with id="
							+ id + "."));

		ObjectMapper mapper = new ObjectMapper();
		User requestData = mapper.convertValue(json, User.class);

		// get the specific user
		User u = User.find.byId(id);
		// check for new values
		if (json.has("email")) {
            modified=true;
			User tmpUser = User.find.where()
					.eq("email", requestData.getEmail()).findUnique();
			System.out.println("does email exist? " + tmpUser);
			if (tmpUser == null)
				u.setEmail(requestData.getEmail());
			else
				return badRequest(JsonWrap
						.prepareJsonStatus(
								BAD_REQUEST,
								"The server can't fulfill the request, as the specified email is already in use. " +
                                        "Try again with a different email."));
		}
		Constraints.EmailValidator emailValidator=new Constraints.EmailValidator();
		Constraints.MinLengthValidator minLengthValidator=new Constraints.MinLengthValidator();
		if (json.has("password") && minLengthValidator.isValid(requestData.getPassword())) {
			u.setPassword(requestData.getPassword());
            modified=true;
		}
		if (json.has("rating")){
			u.setRating(requestData.getRating());
            modified=true;
        }

        if (json.has("name") && minLengthValidator.isValid(requestData.getPassword())) {
            u.setName(requestData.getName());
            modified=true;
        }
		if (json.has("group") && emailValidator.isValid(requestData.getEmail())){
			u.setGroup(requestData.getGroup());
            modified=true;
        }
        if(modified) {
            u.update();
            return ok(JsonWrap.prepareJsonStatus(OK, "User with id=" + id
                    + " has been partially changed."));
        }
        else
            return badRequest(JsonWrap.prepareJsonStatus(BAD_REQUEST,
                    "No updates for any values could be applied."
                            + id + "."));

	}

	/**
	 * Adds a new user to the database, throws an error if the email, name or
	 * password are missing.
	 * 
	 * @return
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public Result updateUser(Long id) {
		JsonNode json = request().body().asJson();

		if(!json.has("email") || !json.has("rating") || !json.has("name") ||
                !json.has("group") || !json.has("password"))
			return badRequest(JsonWrap.prepareJsonStatus(BAD_REQUEST,
					"The Update method needs all details of the user, such as email, " +
                            "rating, name, group and password! An attribute was missing for id="
							+ id + "."));
		
		ObjectMapper mapper = new ObjectMapper();
		User requestData = mapper.convertValue(json, User.class);
		
		// get the specific user
		User u = User.find.byId(id);

		// check for new values
		if (json.has("email")) {
			User tmpUser = User.find.where()
					.eq("email", requestData.getEmail()).findUnique();
			System.out.println("does email exist? " + tmpUser);
			if (tmpUser == null)
				u.setEmail(requestData.getEmail());
			else
				return forbidden(JsonWrap
						.prepareJsonStatus(
								FORBIDDEN,
								"The server can't fulfill the request, as the specified email " +
                                        "is already in use. Try again with a different email."));
		}
        Constraints.EmailValidator emailValidator=new Constraints.EmailValidator();
        Constraints.MinLengthValidator minLengthValidator=new Constraints.MinLengthValidator();
		if (json.has("password") && minLengthValidator.isValid(requestData.getPassword())) {
			u.setPassword(requestData.getPassword());
		}
		if (json.has("rating"))
			u.setRating(requestData.getRating());
		if (json.has("name") && minLengthValidator.isValid(requestData.getPassword()))
			u.setName(requestData.getName());
		if (json.has("group") && emailValidator.isValid(requestData.getEmail()))
			u.setGroup(requestData.getGroup());
		u.update();
		return ok(JsonWrap.prepareJsonStatus(OK, "User with id=" + id
				+ " has been succesfully changed."));

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
		System.out.println(u);
		return ok(JsonWrap.getJson(u));
	}

    /**
     * Returns the user with a specific (unique!) Email.
     * @param email
     * @return OK when found, NOT_FOUND if it doesnt exist.
     */
	public Result getUserByEmail(String email) {
		// Find a task by ID
		User u = User.find.where().eq("email", email).findUnique();
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

        //TODO: check if it is neccesary to delete other things in the db. or make those entries cascade.
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
		JsonNode json = request().body().asJson();
		ObjectMapper mapper = new ObjectMapper();

		User tmp = mapper.convertValue(json, User.class);
        //Checks if the constraints for @email are met via it's isValid method.
        Constraints.EmailValidator emailValidator=new Constraints.EmailValidator();
        Constraints.MinLengthValidator minLengthValidator=new Constraints.MinLengthValidator();
		System.out.println("json=" + json + " - obj=" + tmp);
		if (emailValidator.isValid(tmp.getEmail()) && minLengthValidator.isValid(tmp.getName())
                && minLengthValidator.isValid(tmp.getPassword())) {
			// if this entry with specified email does not exist, create, else
			// throw an error.
			if (User.find.where().eq("email", tmp.getEmail()).findUnique() == null) {
				User u=new User(tmp);
				u.save();
				return created(JsonWrap.prepareJsonStatus(CREATED, "User with id="+u.getId()
						 + " has been created."));
			}
		}
		return forbidden(JsonWrap.prepareJsonStatus(FORBIDDEN,
				"The user could not be created, please specify email, name, password. " +
                        "Email has to be valid (e.g. a@b.com)"));
	}

}
