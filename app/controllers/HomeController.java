package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import models.rating.AnswerRating;
import models.rating.CardRating;
import models.rating.Rating;
import play.libs.Json;
import play.mvc.*;

import util.ActionAuthenticator;
import util.JsonKeys;
import util.JsonUtil;
import views.html.*;

import java.util.*;

import static com.avaje.ebean.Expr.like;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public  Result testRating(){
        User u = new User("Test", "test" + Math.random() + "@example.com", "habla", 0);
        u.save();
        Answer a = new Answer("hello", "world", User.find.byId(1l));
        a.save();
        FlashCard f = new FlashCard(User.find.byId(1l), false, null);
        f.save();
        AnswerRating r = new AnswerRating(u, a, -1);
        CardRating r2 = new CardRating(u, f, -1);

        if (!AnswerRating.exists(u, a)) {
            r.save();
        } else
            System.out.println("nope answer ");

        if (!CardRating.exists(u, f)) {
            r2.save();
        } else
            System.out.println("nope card ");


        System.out.println(AnswerRating.find.where().eq(JsonKeys.USER_ID, u.getId()).findList());
//        Rating.find.all().forEach((t)->System.out.println(t+" class="+t.getClass().getName()));
        User.find.all().forEach((user) -> System.out.println("\t uid="+user.getId() + " rating of this user="+user.getRating()+": Ratings from this user: " + Rating.find.where().eq(JsonKeys.USER_ID, user.getId()).findList().size()));
        System.out.println();
        //clean up.
/*        if (AnswerRating.exists(u, a)) {
            r.delete();
        }
        if (CardRating.exists(u, f)) {
            r2.delete();
        }
        f.delete();
        a.delete();
        u.delete();*/
        return ok(index.render("Test done."));
    }
    /**
     * Creates one user with two tokens, attempts to delete both tokens.
     *
     * @return
     */
    public Result testTokens() {
        String output = "";
        User u = new User("Test", "test" + Math.random() + "@example.com", "habla", 0);

        u.save();
        output += u + System.lineSeparator();

        AuthToken authToken = new AuthToken(u);
        authToken.save();
        output += "Token=" + authToken + System.lineSeparator();

        u.addAuthToken(authToken);
        authToken = new AuthToken(u);
        authToken.save();
        output += "Token=" + authToken + System.lineSeparator();

        u.addAuthToken(authToken);
        u.deleteTokens();
        u.delete();
        System.out.println(output);
        return ok(index.render(output));
    }

    public Result testCards() {
        User tmp = User.find.where().eq("email", "hello1@world.com").findUnique();
        if (tmp == null) {
            tmp = new User("hello", "hello1@world.com", "passwörd", 1);
            tmp.save();
        }


        List<String> tags = new ArrayList<>();
        tags.add("Tag1");
        tags.add("Tag2");

        FlashCard fc = new FlashCard(tmp, false, tags);
        fc.save();
        System.out.println("Flashcard created: " + fc);
        Question q = new Question("Question", tmp);
        q.save();

        fc.setQuestion(q);

        Answer a = new Answer("Answer", "No hint available - 404", tmp);
        a.save();
        fc.addAnswer(a);

        for (int i = 0; i < Math.random() * 10 + 1; i++) {
            Tag t = new Tag("Tag " + i + ": " + new Date());
            t.save();
            fc.addTag(t);
        }

        //we only ned to call update from one side it'll call he other side as well.
        fc.update();

        System.out.println("Flashcard added Question and Answer: " + fc);
        System.out.println("_____________________");
        System.out.println("Question for Card no. " + fc.getId() + ": " + fc.getQuestion());
        System.out.println("IsMultiChoice? " + fc.isMultipleChoice());


        System.out.println("Card tags: " + fc.getTags());
        List<Tag> fc_tags = FlashCard.find.byId(fc.getId()).getTags();

        return ok(index.render("Card test done!"));
    }

    public Result testGroups() {
        List<User> l = new ArrayList<User>();
        l.add(new User("aaa", "a@b.com", "abbbbb", -1));
        l.add(new User("bbb", "b@a.com", "baaaaa", -2));

        for (User u : l) {
            u.save();
        }

        UserGroup g = new UserGroup("y", "y", l);
        g.save();

        for (User u : l) {
            u.setGroup(g);
            u.update();
        }
        System.out.println("Created group g=" + g + " with users" + l);
        System.out.println("__________________________________");
        System.out.println("Querying the groups");
        List<UserGroup> groups = UserGroup.find.all();
        for (UserGroup group : groups) {
            List<User> users = User.find.where().eq(JsonKeys.GROUP_ID, group.getId()).findList();
            System.out.println("Finding users for group=" + group + " users are: " + users);
        }
        UserGroup tmpGroup;
        return ok(index.render("Group test done!"));
    }

    /**
     * Checks the credentials in the body - users password and email and returns a token if valid or forbidden if invalid.
     *
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result login() {
        JsonNode json = request().body().asJson();
        if (json.has(JsonKeys.USER_PASSWORD) && json.has(JsonKeys.USER_EMAIL)) {
            String pass = json.get(JsonKeys.USER_PASSWORD).asText();
            String email = json.get(JsonKeys.USER_EMAIL).asText();
            User logInTo = User.find.where().and(like(JsonKeys.USER_EMAIL, email), like(JsonKeys.USER_PASSWORD, pass)).findUnique();
            System.out.println("Login attempt with email=" + email + " User found=" + logInTo);
            if (logInTo != null) {
                ObjectNode result = Json.newObject();
                result.put(JsonKeys.STATUS_CODE, OK);
                result.put(JsonKeys.DESCRIPTION, "Login succeeded.");
                AuthToken token = new AuthToken(logInTo);
                token.save();
                logInTo.addAuthToken(token);
                result.put(JsonKeys.TOKEN, token.getToken());
                return ok(result);
            }
        }
        return forbidden(JsonUtil.prepareJsonStatus(FORBIDDEN, "Login failed, check email and password for errors."));
    }

    @Security.Authenticated(ActionAuthenticator.class)
    public Result auth() {
        return ok(request().username());
    }

    public Result test() {
        return ok(JsonUtil.prepareJsonStatus(OK, "hello world"));
    }
}
