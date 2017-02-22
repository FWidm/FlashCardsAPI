package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import models.msg.AbstractMessage;
import models.msg.DeckChallengeMessage;
import models.rating.AnswerRating;
import models.rating.CardRating;
import models.rating.Rating;
import play.Logger;
import play.libs.Json;
import play.mvc.*;
import repositories.UserRepository;
import util.ActionAuthenticator;
import util.JsonKeys;
import util.JsonUtil;
import util.RequestKeys;
import util.crypt.PasswordUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

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
/*        User x = new User("HELLO","w1@amazon.com","xyz",0);
        x.save();
        Logger.debug(String.valueOf(x.hasPermission(UserOperations.EDIT_CARD_QUESTION,y)));
        x.delete();*/

        return ok(file);
    }

    /**
     * Accepts a picture as multipart/formdata, saves them in /var/www/html/img/<year>/<date>/img*.<ext>.
     *
     * @return matching http result
     */
    @Security.Authenticated(ActionAuthenticator.class)
    public Result upload() {
        Http.MultipartFormData<File> body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart<File> picture = body.getFile("picture");
        if (picture != null) {
            String fileName = picture.getFilename();
            String contentType = picture.getContentType();
            String fileType = determineFileType(fileName);

            if (contentType.contains("image")) {
                File pictureFile = picture.getFile();
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                File directoryFile = new File("/var/www/html/img/" + year + "/" + month + "/");
                directoryFile.mkdirs();
                try {
                    directoryFile = File.createTempFile("img", "." + fileType, directoryFile);
                    Files.write(directoryFile.toPath(), Files.readAllBytes(pictureFile.toPath()));
                } catch (IOException e) {
                    return internalServerError(JsonUtil.prepareJsonStatus(INTERNAL_SERVER_ERROR, "Could not place file on the server"));
                }
                Logger.debug("Filepath:" + directoryFile.toPath());

                int i = 0;
                String host = "http://" + request().host();
                i = host.lastIndexOf(":");
                host = host.substring(0, i);
                String url = host + getUrl(directoryFile.toPath(), "img");


                try {
                    UploadedMedia mediaRecord = new UploadedMedia(new URI(url), UserRepository.findUserByEmail(request().username()), contentType);
                    Logger.debug("Uploaded file=" + mediaRecord);
                    mediaRecord.save();
                    return created(JsonUtil.toJson(mediaRecord));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "Request did not contain a 'picture' key or valid picture."));

    }

    /**
     * Strips unnecessary parts of the path and replaces backslashes with slashes.
     * (e.g: \img\2016\11\x.png -> /img/2016/11/x.png")
     *
     * @param path to the image
     * @return path minus everything before lastShowndirectory.
     */
    private String getUrl(Path path, String lastShownDirectory) {
        String url = null;
        url = path.toString();
        url = url.replace('\\', '/');
        int index = url.indexOf(lastShownDirectory);
        return url.substring(index - 1);
    }

    /**
     * Expects a filename including extension (i.e. abcd.jpeg) and returns the extension after the last dot.
     * (e.g. "abc.def.gh.ix" returns "ix")
     *
     * @param fileName name of the file
     * @return substring after the last dot in the filename
     */
    private String determineFileType(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

    /**
     * Checks the credentials in the body - users password and email and returns a token if valid or forbidden if invalid.
     *
     * @return appropriate http result
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result login() {
        Logger.debug("Login: json="+request().body().asText());
        JsonNode json = request().body().asJson();
        if (json.has(JsonKeys.USER_PASSWORD) && json.has(JsonKeys.USER_EMAIL)) {
            String pass = json.get(JsonKeys.USER_PASSWORD).asText();
            String email = json.get(JsonKeys.USER_EMAIL).asText();

            //User logInTo = User.find.where().and(like(JsonKeys.USER_EMAIL, email), like(JsonKeys.USER_PASSWORD, pass)).findUnique();
            User logInTo = User.find.where().like(JsonKeys.USER_EMAIL, email).findUnique();
            try {

                Logger.debug("Login attempt with email=" + email + " User found=" + logInTo + " valid? " + PasswordUtil.validatePassword(pass, logInTo.getPassword()));
                if (logInTo != null && PasswordUtil.validatePassword(pass, logInTo.getPassword())) {
                    ObjectNode result = Json.newObject();
                    result.put(JsonKeys.STATUS_CODE, OK);
                    result.put(JsonKeys.DESCRIPTION, "Login succeeded.");
                    Logger.debug("result=" + result);
                    AuthToken token = new AuthToken(logInTo);
                    token.save();
                    Logger.debug("Token=" + token);
                    logInTo.addAuthToken(token);
                    Logger.debug("Added authtoken to user");
                    result.put(JsonKeys.TOKEN, token.getToken());
                    Logger.debug("finished result node: " + result);
                    return ok(result);
                }

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
                return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "User does not exist."));
            }
        }
        return forbidden(JsonUtil.prepareJsonStatus(FORBIDDEN, "Login failed, check email and password for errors."));
    }



    @Security.Authenticated(ActionAuthenticator.class)
    public Result auth() {
        return ok(request().username());
    }

    /**
     * Invalidates the token that was sent in the request.
     * @return
     */
    @Security.Authenticated(ActionAuthenticator.class)
    public Result invalidateToken(){
        String tokenString="";
        String[] authTokenHeaderValues = request().headers().get(RequestKeys.TOKEN_HEADER);
        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
            String[] tokenHeader = authTokenHeaderValues[0].split(" ");
            if (tokenHeader.length == 2) {
                tokenString=tokenHeader[1];
            }
        }
        Logger.debug("Token Value: "+tokenString);
        AuthToken authToken = AuthToken.find.where().eq(JsonKeys.TOKEN, tokenString).findUnique();
        Logger.debug("Token: "+authToken);
        authToken.delete();
        return noContent();
    }

    public Result test() {
        return ok(JsonUtil.prepareJsonStatus(OK, "hello world"));
    }

    public Result heartbeat() {
        Map<String, Object> map = new HashMap<>();
        map.put("currentDate", "" + new Date());
        return ok(JsonUtil.convertToJsonNode(map));
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
        for (int i = 0; i < 60; i++) {
            List<String> tags = new ArrayList<>();
            tags.add("Tag-" + i);

            FlashCard fc = new FlashCard(tmp, false, tags);
            fc.save();
            Question q = new Question("Question-" + i, tmp);
            q.save();
            fc.setQuestion(q);
            for (int j = 0; j < 20; j++) {
                Answer a = new Answer("Answer-" + i + "|" + j, "none", tmp);
                a.save();
                fc.addAnswer(a);
            }

            fc.setDeck(deck);
            fc.update();

            flashCardList.add(fc);
        }

        return ok(JsonUtil.toJson(CardDeck.find.byId(deck.getId())));
    }

    public Result testCategories() {
        Category root = new Category("0");
        root.save();
        Category firstLevel = new Category("1", root);
        firstLevel.save();
        Category secondLevel = new Category("2", firstLevel);
        secondLevel.save();

        List<CardDeck> cardDeckList = generateDeckList(2, 5, 5);
        Category thirdLevel = new Category("3", secondLevel);
        thirdLevel.save();

        thirdLevel.setCardDecks(cardDeckList);
        for (CardDeck deck : cardDeckList) {
            deck.update();
        }
        thirdLevel.update();

        Logger.debug("Root=" + root + "parent=" + root.getParent());
        Logger.debug("1st Level=" + firstLevel + " parent=" + firstLevel.getParent());
        Logger.debug("2nd Level=" + secondLevel + " parent=" + secondLevel.getParent());
        Logger.debug("3rd Level=" + thirdLevel + " parent=" + thirdLevel.getParent() + "deck#=" + thirdLevel.getCardDecks().size());


        return ok(JsonUtil.toJson(thirdLevel));
    }

    private List<CardDeck> generateDeckList(int noDecks, int noCards, int noAnswers) {
        List<CardDeck> cardDeckList = new ArrayList<>();

        User tmp = User.find.where().eq("email", "hello1@world.com").findUnique();
        if (tmp == null) {
            tmp = new User("hello", "hello1@world.com", "passwörd", 1);
            tmp.save();
        }
        CardDeck deck;
        for (int x = 0; x < noDecks; x++) {
            deck = new CardDeck("Deck " + x, "This is a test");
            deck.save();
            List<FlashCard> flashCardList = new ArrayList<>();
            for (int i = 0; i < noCards; i++) {
                List<String> tags = new ArrayList<>();
                tags.add("Tag-" + i);

                FlashCard fc = new FlashCard(tmp, false, tags);
                fc.save();
                Question q = new Question("Question-" + i, tmp);
                q.save();
                fc.setQuestion(q);
                for (int j = 0; j < noAnswers; j++) {
                    Answer a = new Answer("Answer-" + i + "|" + j, "none", tmp);
                    a.save();
                    fc.addAnswer(a);
                }

                fc.setDeck(deck);
                fc.update();

                flashCardList.add(fc);
            }
            cardDeckList.add(deck);
        }
        Logger.debug("Finished creating " + cardDeckList.size() + " decks!");
        return cardDeckList;
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


        Logger.debug("" + AnswerRating.find.where().eq(JsonKeys.USER_ID, u.getId()).findList());
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
        return ok(JsonUtil.prepareJsonStatus(OK,"Rating test done!"));
    }

    /**
     * Creates one user with two tokens, attempts to delete both tokens.
     *
     * @return appropriate http result
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
        return ok(JsonUtil.prepareJsonStatus(OK,"Token test done! output="+output));
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

        return ok(JsonUtil.prepareJsonStatus(OK,"Card test done!"));
    }

    public Result testGroups() {
        List<User> l = new ArrayList<User>();
        l.add(new User("aaa", "b" + (int) (Math.random() * 100) + "@test.com", "abbbbb", -1));
        l.add(new User("bbb", "a" + (int) (Math.random() * 100) + "@test.com", "baaaaa", -2));

        for (User u : l) {
            u.save();
        }

        UserGroup g1 = new UserGroup("x", "x", l);
        UserGroup g2 = new UserGroup("y", "y", l);
        g1.save();
        g2.save();
        List<UserGroup> ug = new ArrayList<UserGroup>();
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
        for (User u : users) {
            u.getUserGroups().forEach((group) -> Logger.debug(u.getId() + ":" + group));
        }

        return ok(JsonUtil.prepareJsonStatus(OK,"Group test done!"));
    }


    public Result testMessages() {
        User user = UserRepository.findUserByEmail("email1@email.com");
        if (user == null) {
            user = new User("name", "email1@email.com", "password", 0);
            user.save();
        }

        CardDeck deck = CardDeck.find.where().eq(JsonKeys.CARDDECK_NAME, "deck").findUnique();
        if (deck == null) {
            deck = new CardDeck("deck");
            deck.save();
        }

        DeckChallengeMessage msg = new DeckChallengeMessage(user, "content", deck);
        msg.save();
        Logger.debug("msg=" + AbstractMessage.find.byId(1L));
        return ok(JsonUtil.toJson(DeckChallengeMessage.find.byId(1L)));
    }
}
