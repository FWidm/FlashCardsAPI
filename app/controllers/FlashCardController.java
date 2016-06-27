package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.*;
import org.apache.http.auth.AUTH;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import util.JsonKeys;
import util.JsonWrap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static play.mvc.Controller.request;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.mvc.Results.*;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 *         on 17/06/16.
 */
public class FlashCardController {
    /**
     * Retrieves all Flashcards.
     * @return
     */
    public Result getFlashCardList() {
        List<FlashCard> flashCardList = FlashCard.find.all();
        return ok(JsonWrap.getJson(flashCardList));
    }

    /**
     * Retrieves everything from a flashcard with the given id.
     * @param id
     * @return
     */
    public Result getFlashCard(long id) {
        FlashCard card = FlashCard.find.byId(id);
        return ok(JsonWrap.getJson(card));
    }

    /**
     * Deletes the specific Flashcard including questions and answers.
     * @param id
     * @return
     */
    public Result deleteFlashCard(long id){
        FlashCard.find.byId(id).delete();

        return ok(JsonWrap.prepareJsonStatus(OK, "The card with the id=" + id
                + " has been deleted. This includes questions and answers"));
    }

    /**
     * Adds a new Flashcard, expects a question (if an id is specified, we load it from the db, else we create a new one),
     * answers (if id is given --> DB, else create new), author (must specify id), isMultiplechoice flag.
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result addFlashCard(){
        // TODO: 27/06/16 write answer part completely
        JsonNode json = request().body().asJson();
        ObjectMapper mapper = new ObjectMapper();
        FlashCard requestObject = mapper.convertValue(json, FlashCard.class);

        List<Answer> answers = null;

        //We expect just id's to set answers/questions/authors - we then check the db for the id's and retrieve all values
        // we nee ourselves.
        if (json.has("answers")) {
            //create a new list
            answers = new ArrayList<Answer>();
            //get the specific nods in the json
            JsonNode answersNode = json.findValue("answers");
            // Loop through all objects in the values associated with the
            // "users" key.
            for (JsonNode node : answersNode) {
                // when a user id is found we will get the object and add them to the userList.
                System.out.println("Node="+node);
                if (node.has(JsonKeys.ANSWER_ID)) {
                    Answer found = Answer.find.byId(node.get(JsonKeys.ANSWER_ID).asLong());
                    System.out.println(">> answer: "+found);
                    answers.add(found);
                }
                else{
                    // TODO: 20/06/16 create new Answers from the json if no id was sent.
                }
            }
            requestObject.setAnswers(answers);
        }


        if(json.has("question")){
            if(json.get("question").has(JsonKeys.QUESTION_ID)){
                Question question= Question.find.byId(json.findValue("question").findValue(JsonKeys.QUESTION_ID).asLong());
                requestObject.setQuestion(question);
            }
            else{
                try {
                    Question q=parseQuestion(json.get("question"));
                    q.save();
                    requestObject.setQuestion(q);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(requestObject);

        if(json.has("author")){
            User author = User.find.byId(json.get("author").get(JsonKeys.USER_ID).asLong());
            requestObject.setAuthor(author);
        }

        /*if(json.has("tags")){
            //todo: implement tags as json array.
        }*/

        if(json.has("multipleChoice")){
            requestObject.setMultipleChoice(json.findValue("multipleChoice").asBoolean());
        }
        FlashCard card = new FlashCard(requestObject);
        card.save();

        return ok(JsonWrap.prepareJsonStatus(OK, "FlashCard with id="+0+" has been created!"));
    }

    /**
     * Parses a question from the given JsonNode node.
     * @param node
     * @return
     * @throws URISyntaxException
     */
    private Question parseQuestion(JsonNode node) throws URISyntaxException {
        User author=null;
        String questionText=null;
        if(node.has("author")){
            if(node.get("author").has(JsonKeys.USER_ID)){
                long uid=node.get("author").get(JsonKeys.USER_ID).asLong();
                author=User.find.byId(uid);
                System.out.println("Search for user with id="+uid+" details="+author);
            }
        }
        if(node.has("question")){
            questionText=node.get("question").asText();
        }
        Question question=new Question(questionText, author);

        if(node.has("mediaURI")){
            question.setMediaURI(new URI(node.get("mediaURI").asText()));
        }
        return question;
    }
    /**
     * Update a  Flashcard either completely via put or partially via patch.
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateFlashCard(long id){
        // TODO: 27/06/16 write answer part completely
        JsonNode json = request().body().asJson();
        ObjectMapper mapper = new ObjectMapper();
        FlashCard requestObject = mapper.convertValue(json, FlashCard.class);
        FlashCard toUpdate = FlashCard.find.byId(id);

        if(request().method().equals("PUT") && (!json.has("answers") || !json.has("question")
                || !json.has("author") || !json.has("multipleChoice") || json.has("tags"))){
            return badRequest(JsonWrap.prepareJsonStatus(BAD_REQUEST,
                    "The Update method needs all details of the group, such as name, " +
                            "description and a user group (array of users or null). An attribute was missing for id="
                            + id + "."));
        }

        List<Answer> answers = null;

        //We expect just id's to set answers/questions/authors - we then check the db for the id's and retrieve all values
        // we nee ourselves.
        if (json.has("answers")) {
            //create a new list
            answers = new ArrayList<Answer>();
            //get the specific nodes in the json
            JsonNode answersNode = json.findValue("answers");
            // Loop through all objects in the values associated with the
            // "users" key.
            for (JsonNode node : answersNode) {
                // when a user id is found we will get the object and add them to the userList.
                if (node.has("id")) {
                    Answer found = Answer.find.byId(node.get("id").asLong());
                    answers.add(found);
                }
            }
            toUpdate.setAnswers(answers);
        }

        if(json.has("question")){
            if(json.get("question").has(JsonKeys.QUESTION_ID)){
                Question question= Question.find.byId(json.findValue("question").findValue(JsonKeys.QUESTION_ID).asLong());
                toUpdate.setQuestion(question);
            }
            else{
                try {
                    Question q=parseQuestion(json.get("question"));
                    q.save();
                    toUpdate.setQuestion(q);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }

        if(json.has("author")){
            User u=mapper.convertValue(json.findValue("author"), User.class);
            User author = User.find.byId(u.getId());
            toUpdate.setAuthor(author);
        }

        /*if(json.has("tags")){
            //todo: implement tags as json array.
        }*/

        if(json.has("multipleChoice")){
            toUpdate.setMultipleChoice(json.findValue("multipleChoice").asBoolean());
        }

        toUpdate.update();

        return ok(JsonWrap.prepareJsonStatus(OK, "FlashCard with id="+id+" has been updated!"));

    }

    /**
     * A method that allows us to retrieve the question for a specific card under the URI /cards/:id/question
     * @param id
     * @return
     */
    public Result getQuestion(long id){
        Question ret=null;
        try{
            ret=FlashCard.find.byId(id).getQuestion();
        }catch (Exception e){
            return notFound(JsonWrap.prepareJsonStatus(NOT_FOUND,"No card with the id="+id+" does exist."));
        }
        return ok(JsonWrap.getJson(ret));
    }

    /**
     * Gets the author of a specific card.
     * @param id
     * @return
     */
    public Result getAuthor(long id){
        User ret=null;
        try{
            ret=FlashCard.find.byId(id).getAuthor();
        }catch (Exception e){
            return notFound(JsonWrap.prepareJsonStatus(NOT_FOUND,"No card with the id="+id+" does exist."));
        }
        return ok(JsonWrap.getJson(ret));
    }

    /**
     * A method that allows us to retrieve answers for a specific card under the URI /cards/:id/answers
     * @param id
     * @return
     */
    public Result getAnswers(long id){
        Map<String, String[]> urlParams = Controller.request().queryString();
        int answersSize=10;
        if(urlParams.containsKey("size")){
            try {
                answersSize = Integer.parseInt(urlParams.get("size")[0]);
            }catch(NumberFormatException e){
                return badRequest(JsonWrap.prepareJsonStatus(BAD_REQUEST,
                        "Parameter size="+urlParams.get("size")[0]+" could not be parsed to integer."));
            }
        }
        System.out.println("answers size="+answersSize);
        List<Answer> ret=null;
        try{
            // TODO: 27/06/16 Allow sorting by date, rating o.A., handle multichoice etc.
            ret=FlashCard.find.byId(id).getAnswers().subList(0,answersSize);
        }catch (Exception e){
            return notFound(JsonWrap.prepareJsonStatus(NOT_FOUND,"Error, no card with id="+id+" or answers exist."));
        }
        return ok(JsonWrap.getJson(ret));
    }



}
