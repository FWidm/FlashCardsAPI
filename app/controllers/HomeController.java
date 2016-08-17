package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import models.rating.AnswerRating;
import models.rating.CardRating;
import models.rating.Rating;
import play.Logger;
import play.api.mvc.Flash;
import play.libs.Json;
import play.mvc.*;

import util.ActionAuthenticator;
import util.FileTypeChecker;
import util.JsonKeys;
import util.JsonUtil;
import views.html.*;

import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static com.avaje.ebean.Expr.like;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * displays the flashcardslogo.
     */
    public Result index() {
        java.io.File file = new java.io.File("_Docs/img/flash_icon.png");
        Logger.debug(file.getAbsolutePath());

        return ok(file);
    }

    /**
     * Decoes the given Image to a file and saves it to the _Docs/img/usr/ directory.
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result imageUpload() {
        JsonNode json = request().body().asJson();
        if (json.has("image")) {
            String imageDataBytes = json.get("image").asText().substring(json.get("image").asText().indexOf(",") + 1);

            Logger.debug(imageDataBytes);
            byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(imageDataBytes);
            File f = new File("_Docs/img/usr/" + new Date().getTime()+ FileTypeChecker.getFileType(json.get("image").asText()));
            try {
                Files.write(f.toPath(), imageBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            f.delete();
            return ok(f);
        }
        return notFound();
    }

    public Result testRating() {
        User u = new User("Test", "test" + Math.random() + "@example.com", "habla", 0);
        u.save();
        Answer a = new Answer("hello", "world", User.find.byId(u.getId()));
        a.save();
        FlashCard f = new FlashCard(User.find.byId(u.getId()), false, null);
        f.save();
        AnswerRating r = new AnswerRating(u, a, -1);
        CardRating r2 = new CardRating(u, f, -1);

        if (!AnswerRating.exists(u, a)) {
            r.save();
        } else
            Logger.debug("nope answer ");

        if (!CardRating.exists(u, f)) {
            r2.save();
        } else
            Logger.debug("nope card ");


        Logger.debug(""+AnswerRating.find.where().eq(JsonKeys.USER_ID, u.getId()).findList());
//        Rating.find.all().forEach((t)->Logger.debug(t+" class="+t.getClass().getName()));
        User.find.all().forEach((user) -> Logger.debug("\t uid=" + user.getId() + " rating of this user=" + user.getRating() + ": Ratings from this user: " + Rating.find.where().eq(JsonKeys.USER_ID, user.getId()).findList().size()));
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
        Logger.debug(output);
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
        Logger.debug("Flashcard created: " + fc);
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

        Logger.debug("Flashcard added Question and Answer: " + fc);
        Logger.debug("_____________________");
        Logger.debug("Question for Card no. " + fc.getId() + ": " + fc.getQuestion());
        Logger.debug("IsMultiChoice? " + fc.isMultipleChoice());


        Logger.debug("Card tags: " + fc.getTags());
        List<Tag> fc_tags = FlashCard.find.byId(fc.getId()).getTags();

        return ok(index.render("Card test done!"));
    }

    public Result testGroups() {
        List<User> l = new ArrayList<User>();
        l.add(new User("aaa","b"+(int)(Math.random()*100)+"@test.com", "abbbbb", -1));
        l.add(new User("bbb", "a"+(int)(Math.random()*100)+"@test.com", "baaaaa", -2));

        for (User u : l) {
            u.save();
        }

        UserGroup g1 = new UserGroup("x", "x", l);
        UserGroup g2 = new UserGroup("y", "y", l);
        g1.save();
        g2.save();
        List<UserGroup> ug=new ArrayList<UserGroup>();
        ug.add(g1);
        ug.add(g2);

        for (User u : l) {
            u.setUserGroups(ug);
            u.update();
        }


        Logger.debug("__________________________________");
        Logger.debug("Querying the groups");
        List<User> users = User.find.all();
        // set group
        for (User u:users) {
            u.getUserGroups().forEach((group)-> Logger.debug(u.getId()+":"+group));
        }

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
            Logger.debug("Login attempt with email=" + email + " User found=" + logInTo);
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
    public Result testCardDeck() {
        User tmp = User.find.where().eq("email", "hello1@world.com").findUnique();
        if (tmp == null) {
            tmp = new User("hello", "hello1@world.com", "passwörd", 1);
            tmp.save();
        }
        CardDeck deck = new CardDeck("Deck 1", "This is a test");
        deck.save();
        List<FlashCard> flashCardList = new ArrayList<>();
        for(int i=0; i<60; i++){
            List<String> tags = new ArrayList<>();
            tags.add("Tag-"+i);

            FlashCard fc = new FlashCard(tmp, false, tags);
            fc.save();
            Question q = new Question("Question-"+i, tmp);
            q.save();
            fc.setQuestion(q);
            for(int j=0; j<20; j++){
                Answer a = new Answer("Answer-"+i+"|"+j, "none", tmp);
                a.save();
                fc.addAnswer(a);
            }

            fc.setDeck(deck);
            fc.update();

            flashCardList.add(fc);
        }

        return ok(JsonUtil.getJson(CardDeck.find.byId(deck.getId())));
    }
    @Security.Authenticated(ActionAuthenticator.class)
    public Result auth() {
        return ok(request().username());
    }

    public Result test() {
        return ok(JsonUtil.prepareJsonStatus(OK, "hello world"));
    }
}
