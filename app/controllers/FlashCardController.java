package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigException;
import models.*;
import play.mvc.BodyParser;
import play.mvc.Controller;
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
 *
 * This class handles all operations for Flashcards.
 * Used routes are:
 *  ip:9000/cards                       - GET; POST
 *         /cards/:id                   - GET; PUT; PATCH
 *         /cards/:id/answers?size=x    - GET
 *         /cards/:id/question          - GET
 *         /cards/:id/author            - GET
 */
public class FlashCardController {
    /**
     * Retrieves all Flashcards.
     * @return HTTPResult
     */
    public Result getFlashCardList() {
        List<FlashCard> flashCardList = FlashCard.find.all();
        return ok(JsonWrap.getJson(flashCardList));
    }

    /**
     * Retrieves everything from a flashcard with the given id.
     * @param id of a card
     * @return HTTPResult
     */
    public Result getFlashCard(long id) {
        try{
            FlashCard card = FlashCard.find.byId(id);
            return ok(JsonWrap.getJson(card));
        }
        catch (NullPointerException e){
            return notFound(JsonWrap.prepareJsonStatus(NOT_FOUND,"Error, no card with id="+id+" exists."));
        }
    }

    /**
     * Deletes the specific Flashcard including questions and answers.
     * @param id of a card
     * @return HTTPResult
     */
    public Result deleteFlashCard(long id){
        try {
            FlashCard fc = FlashCard.find.byId(id);
            List<Tag> tags = fc.getTags();
            //Get all tags and unlink them from this card. Tag still exists to this point.
            for (Tag tmptag:tags){
                tmptag.removeFlashCard(fc);
                if(tmptag.getCards().size()==0){
                    // TODO: 01/07/16 do we want to delete if no reference to the tag exists?
                }
                System.out.println("Removing link to tag="+tmptag);
            }

            FlashCard.find.byId(id).delete();

            return ok(JsonWrap.prepareJsonStatus(OK, "The card with the id=" + id
                    + " has been deleted. This includes questions and answers. All Tags for this card were disconnected and persist."));
        }catch (NullPointerException e){
            return notFound(JsonWrap.prepareJsonStatus(NOT_FOUND,"Error, no card with id="+id+" exists."));
        }
    }

    /**
     * Adds a new Flashcard, expects a question (if an id is specified, we load it from the db, else we create a new one),
     * answers (if id is given --> DB, else create new), author (must specify id), isMultiplechoice flag.
     * @return HTTPResult
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result addFlashCard(){
        try{
            JsonNode json = request().body().asJson();
            ObjectMapper mapper = new ObjectMapper();
            FlashCard requestObject = mapper.convertValue(json, FlashCard.class);

            List<Answer> answers;
            List<Tag> tags;
            //We expect just id's to set answers/questions/authors - we then check the db for the id's and retrieve all values
            // we nee ourselves.
            if (json.has(JsonKeys.FLASHCARD_ANSWERS)) {
                //create a new list
                answers = new ArrayList<>();
                //get the specific nods in the json
                JsonNode answersNode = json.findValue(JsonKeys.FLASHCARD_ANSWERS);
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
                        try {
                            Answer tmpA = Answer.parseAnswer(node);
                            System.out.println(">> answer: "+tmpA);
                            answers.add(tmpA);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                }
                requestObject.setAnswers(answers);
            }


            if(json.has(JsonKeys.FLASHCARD_QUESTION)){
                if(json.get(JsonKeys.FLASHCARD_QUESTION).has(JsonKeys.QUESTION_ID)){
                    Question question= Question.find.byId(json.findValue(JsonKeys.FLASHCARD_QUESTION).findValue(JsonKeys.QUESTION_ID).asLong());
                    requestObject.setQuestion(question);
                }
                else{
                    try {
                        Question q=Question.parseQuestion(json.get(JsonKeys.FLASHCARD_QUESTION));
                        q.save();
                        requestObject.setQuestion(q);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(json.has(JsonKeys.AUTHOR)){
                User author = User.find.byId(json.get(JsonKeys.AUTHOR).get(JsonKeys.USER_ID).asLong());
                requestObject.setAuthor(author);
            }

        if(json.has(JsonKeys.FLASHCARD_TAGS)){
                requestObject.setTags(retrieveTags(json));
        }

            if(json.has(JsonKeys.FLASHCARD_MULTIPLE_CHOICE)){
                requestObject.setMultipleChoice(json.findValue(JsonKeys.FLASHCARD_MULTIPLE_CHOICE).asBoolean());
            }
            FlashCard card = new FlashCard(requestObject);
            card.save();

            return ok(JsonWrap.prepareJsonStatus(OK, "FlashCard with id="+card.getId()+" has been created!"));
        }
        catch (IllegalArgumentException e) {
            return badRequest(JsonWrap
                    .prepareJsonStatus(
                            BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS));
        }
    }

    /**
     * Reads all Tags either via their id, or creates a new Tag when it does not exist at the moment.
     * @param json the root json Object
     * @return a list of Tags
     */
    private List<Tag> retrieveTags(JsonNode json){
        List<Tag> tags=new ArrayList<>();
        //get the specific nods in the json
        JsonNode tagNode = json.findValue(JsonKeys.FLASHCARD_TAGS);
        // Loop through all objects in the values associated with the
        // "users" key.
        for (JsonNode node : tagNode) {
            // when a user id is found we will get the object and add them to the userList.
            System.out.println("Node="+node);
            if (node.has(JsonKeys.TAG_ID)) {
                Tag found = Tag.find.byId(node.get(JsonKeys.TAG_ID).asLong());
                System.out.println(">> answer: "+found);
                tags.add(found);
            }
            else{
                try {
                    Tag tmpT = Tag.parseTag(node);
                    System.out.println(">> tag: "+tmpT);
                    tags.add(tmpT);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    return tags;
    }

    /**
     * Update a  Flashcard either completely via put or partially via patch.
     * @return httpResult
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateFlashCard(long id){
        JsonNode json = request().body().asJson();
        ObjectMapper mapper = new ObjectMapper();
        try {
            FlashCard toUpdate = FlashCard.find.byId(id);
            if(request().method().equals("PUT") && (!json.has(JsonKeys.FLASHCARD_ANSWERS) || !json.has(JsonKeys.FLASHCARD_QUESTION)
                    || !json.has(JsonKeys.AUTHOR) || !json.has(JsonKeys.FLASHCARD_MULTIPLE_CHOICE) || !json.has(JsonKeys.FLASHCARD_TAGS))){
                System.out.println(!json.has(JsonKeys.FLASHCARD_ANSWERS) +" "+ !json.has(JsonKeys.FLASHCARD_QUESTION)
                        +" "+ !json.has(JsonKeys.AUTHOR) +" "+ !json.has(JsonKeys.FLASHCARD_MULTIPLE_CHOICE) +" "+ json.has(JsonKeys.FLASHCARD_TAGS));
                return badRequest(JsonWrap.prepareJsonStatus(BAD_REQUEST,
                        "The Update method needs all details of the card, such as name, " +
                                "description and a user group (array of users or null). An attribute was missing for id="
                                + id + "."));
            }

            List<Answer> answers;

            if (json.has(JsonKeys.FLASHCARD_ANSWERS)) {
                //create a new list
                answers = new ArrayList<>();
                //get the specific nods in the json
                JsonNode answersNode = json.findValue(JsonKeys.FLASHCARD_ANSWERS);
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
                        try {
                            Answer tmpA = Answer.parseAnswer(node);
                            System.out.println(">> answer: "+tmpA);
                            answers.add(tmpA);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                }
                toUpdate.setAnswers(answers);
            }

            if(json.has(JsonKeys.FLASHCARD_QUESTION)){
                if(json.get(JsonKeys.FLASHCARD_QUESTION).has(JsonKeys.QUESTION_ID)){
                    Question question= Question.find.byId(json.findValue(JsonKeys.FLASHCARD_QUESTION).findValue(JsonKeys.QUESTION_ID).asLong());
                    toUpdate.setQuestion(question);
                }
                else{
                    try {
                        Question q=Question.parseQuestion(json.get(JsonKeys.FLASHCARD_QUESTION));
                        q.save();
                        toUpdate.setQuestion(q);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(json.has(JsonKeys.AUTHOR)){
                User u=mapper.convertValue(json.findValue(JsonKeys.AUTHOR), User.class);
                User author = User.find.byId(u.getId());
                toUpdate.setAuthor(author);
            }

        /*if(json.has(JsonKeys.FLASHCARD_TAGS)){
        }*/

            if(json.has(JsonKeys.FLASHCARD_MULTIPLE_CHOICE)){
                toUpdate.setMultipleChoice(json.findValue(JsonKeys.FLASHCARD_MULTIPLE_CHOICE).asBoolean());
            }

            toUpdate.update();

            return ok(JsonWrap.prepareJsonStatus(OK, "FlashCard with id="+id+" has been updated!"));
        }catch (NullPointerException e){
            return notFound(JsonWrap.prepareJsonStatus(NOT_FOUND,"Error, no card with id="+id+" exists."));
        }
        catch (IllegalArgumentException e) {
            return badRequest(JsonWrap
                    .prepareJsonStatus(
                            BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS));
        }

    }

    /**
     * A method that allows us to retrieve the question for a specific card under the URI /cards/:id/question
     * @param id of a card
     * @return httpresult
     */
    public Result getQuestion(long id){
        Question ret;
        try{
            ret=FlashCard.find.byId(id).getQuestion();
        }catch (Exception e){
            return notFound(JsonWrap.prepareJsonStatus(NOT_FOUND,"Error, no card with id="+id+" exists."));
        }
        return ok(JsonWrap.getJson(ret));
    }

    /**
     * Gets the author of a specific card.
     * @param id of a card
     * @return author of the card including a http result ok OR not found if nothing was found
     */
    public Result getAuthor(long id){
        User ret;
        try{
            ret=FlashCard.find.byId(id).getAuthor();
        }catch (Exception e){
            return notFound(JsonWrap.prepareJsonStatus(NOT_FOUND,"Error, no card with id="+id+" exists."));
        }
        return ok(JsonWrap.getJson(ret));
    }

    /**
     * A method that allows us to retrieve answers for a specific card under the URI /cards/:id/answers
     * @param id of a card
     * @return answers of the card including a http result ok OR not found if nothing was found
     */
    public Result getAnswers(long id){
        Map<String, String[]> urlParams = Controller.request().queryString();
        int answersSize=-1;
        if(urlParams.containsKey(JsonKeys.FLASHCARD_PARAM_SIZE)){
            try {
                answersSize = Integer.parseInt(urlParams.get("size")[0]);
            }catch(NumberFormatException e){
                return badRequest(JsonWrap.prepareJsonStatus(BAD_REQUEST,
                        "Parameter size="+urlParams.get("size")[0]+" could not be parsed to integer."));
            }
        }
        System.out.println("answers size="+answersSize);
        List<Answer> ret;
        try{
            // TODO: 27/06/16 Allow sorting by date, rating o.A., handle multichoice etc.
            ret=FlashCard.find.byId(id).getAnswers();
            //Return a sublist from 0 to either the size of answers OR the cap we get via parameter.
            if(answersSize>0)
                ret=ret.subList(0,Math.min(answersSize,ret.size()));


        }catch (Exception e){
            return notFound(JsonWrap.prepareJsonStatus(NOT_FOUND,"Error, no card with id="+id+" exists."));
        }
        return ok(JsonWrap.getJson(ret));
    }

    /**
     * Retreive all Tags or the first n Elements from the Sublist when adding ?size=x to the url, where x must be an integer.
     * @param id of a card
     * @return list of Tags as json to the caller
     */
    public Result getTags(long id) {
        Map<String, String[]> urlParams = Controller.request().queryString();
        int answersSize=-1;
        if(urlParams.containsKey(JsonKeys.FLASHCARD_PARAM_SIZE)){
            try {
                answersSize = Integer.parseInt(urlParams.get("size")[0]);
            }catch(NumberFormatException e){
                return badRequest(JsonWrap.prepareJsonStatus(BAD_REQUEST,
                        "Parameter size="+urlParams.get("size")[0]+" has to be a valid integer."));
            }
        }
        System.out.println("tags size="+answersSize);
        List<Tag> ret;
        try{
            ret=FlashCard.find.byId(id).getTags();
            //Return a sublist from 0 to either the size of answers OR the cap we get via parameter.
            if(answersSize>0)
                ret=ret.subList(0,Math.min(answersSize,ret.size()));

        }catch (Exception e){
            return notFound(JsonWrap.prepareJsonStatus(NOT_FOUND,"Error, no card with id="+id+" exists."));
        }
        return ok(JsonWrap.getJson(ret));
    }


}
