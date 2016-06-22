package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.*;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import util.JsonKeys;
import util.JsonWrap;

import java.util.ArrayList;
import java.util.List;

import static play.mvc.Controller.request;
import static play.mvc.Results.noContent;
import static play.mvc.Results.ok;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 *         on 17/06/16.
 */
public class FlashCardController {

    public Result getFlashCardList() {
        List<FlashCard> flashCardList = FlashCard.find.all();
        return ok(JsonWrap.getJson(flashCardList));
    }

    /**
     * Adds a new Flashcard, expects a question (if an id is specified, we load it from the db, else we create a new one),
     * answers (if id is given --> DB, else create new), author (must specify id), isMultiplechoice flag.
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result addFlashCard(){

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
                if (node.has("id")) {
                    Answer found = Answer.find.byId(node.get("id").asLong());
                    answers.add(found);
                }
                else{
                    // TODO: 20/06/16 create new Answers from the json if no id was sent.
                }
            }
            requestObject.setAnswers(answers);
        }

        if(json.has("question")){
            Question question= Question.find.byId(json.findValue("question").findValue("id").asLong());
            // TODO: 20/06/16 create a new Question from the json if no id was sent.

            requestObject.setQuestion(question);
        }

        if(json.has("author")){
            User u=mapper.convertValue(json.findValue("author"), User.class);
            User author = User.find.byId(u.getId());
            requestObject.setAuthor(author);
        }

        /*if(json.has("tags")){
            //todo: implement tags as json array.
        }*/

        if(json.has("multipleChoice")){
            requestObject.setMultipleChoice(json.findValue("multipleChoice").asBoolean());
        }
        FlashCard card = new FlashCard(requestObject);
        System.out.println("Flash Card is "+card);
        System.out.println("Answers are "+card.getAnswers());
        System.out.println("Question is "+card.getQuestion());
        card.save();

        return ok();
    }

    public Result deleteFlashCard(Long id){
        FlashCard fc=FlashCard.find.byId(id);
        fc.delete();
        return noContent();
    }
}
