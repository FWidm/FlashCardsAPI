package repositories;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.*;
import play.Logger;
import util.JsonKeys;
import util.RequestKeys;
import util.UserOperations;
import util.exceptions.InvalidInputException;
import util.exceptions.ObjectNotFoundException;
import util.exceptions.ParameterNotSupportedException;
import util.exceptions.PartiallyModifiedException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static play.mvc.Controller.request;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
public class FlashCardRepository {
    /**
     * Retrieves all Flashcards.
     *
     * @return list of cards
     */
    public static List<FlashCard> getFlashCardList() {
        List<FlashCard> flashCardList = FlashCard.find.all();
        return flashCardList;
    }

    /**
     * Retrieves everything from a flashcard with the given id.
     *
     * @param id of a card
     * @return card
     */
    public static FlashCard getFlashCard(long id) throws NullPointerException {
        FlashCard card = FlashCard.find.byId(id);
        return card;
    }

    /**
     * Deletes the specific Flashcard including questions and answers.
     *
     * @param id of a card
     * @return deleted card object
     */
    public static FlashCard deleteFlashCard(long id) throws NullPointerException {

        FlashCard card = FlashCard.find.byId(id);
        card.delete();

        return card;
    }

    /**
     * Adds a new Flashcard, expects a question (if an id is specified, we load it from the db, else we create a new one),
     * answers (if id is given --> DB, else create new), author (must specify id), isMultiplechoice flag.
     *
     * @return Card
     */
    public static FlashCard addFlashCard(String email, JsonNode json) throws InvalidInputException, ParameterNotSupportedException, PartiallyModifiedException {
        ObjectMapper mapper = new ObjectMapper();
        FlashCard requestObject = mapper.convertValue(json, FlashCard.class);
        String information = "";
        User author = User.find.where().eq(JsonKeys.USER_EMAIL,email).findUnique();
        requestObject.setAuthor(author);
        //We expect just id's to set answers/questions/authors - we then check the db for the id's and retrieve all values
        // we nee ourselves.
        if (json.has(JsonKeys.FLASHCARD_ANSWERS)) {
            // TODO: 10.08.2016 rewrite this part, it is ugly

            JsonNode answersNode = json.findValue(JsonKeys.FLASHCARD_ANSWERS);
            if (JsonKeys.debugging) Logger.debug("answersNode=" + answersNode);
            for (JsonNode node : answersNode) {
                if (node.has(JsonKeys.ANSWER_ID)) {
                    throw new InvalidInputException("An answerId is not accepted while creating new cards, " +
                            "please provide a complete answer object with the following components: "
                            + JsonKeys.ANSWER_JSON_ELEMENTS);
                }
            }
            requestObject.setAnswers(retrieveAnswers(author,json));
        }

        if (json.has(JsonKeys.FLASHCARD_QUESTION)) {

            if (json.get(JsonKeys.FLASHCARD_QUESTION).has(JsonKeys.QUESTION_ID)) {
                throw new InvalidInputException("A questionId is not accepted while creating " +
                        "new cards, please provide a complete question object with the following components: " + JsonKeys.QUESTION_JSON_ELEMENTS);
            } else {
                try {
                    Question q = Question.parseQuestion(author,json.get(JsonKeys.FLASHCARD_QUESTION));
                    q.save();
                    requestObject.setQuestion(q);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }

        if (json.has(JsonKeys.FLASHCARD_TAGS)) {

            List<Tag> tags = TagRepository.retrieveOrCreateTags(json);
            // TODO: 07.01.2017 revisit this process
            if (tags.contains(null)) {
                if (JsonKeys.debugging) Logger.debug(">> null!");
                information += " One or more tag ids where invalid!";
                tags.remove(null);
            }
            requestObject.setTags(tags);

        }

        if (json.has(JsonKeys.FLASHCARD_MULTIPLE_CHOICE)) {
            requestObject.setMultipleChoice(json.findValue(JsonKeys.FLASHCARD_MULTIPLE_CHOICE).asBoolean());
        }
        FlashCard card = new FlashCard(requestObject);
        if (JsonKeys.debugging) Logger.debug("Tags=" + card.getTags().size());
        card.save();
        if(information!=""){
            throw new PartiallyModifiedException("FlashCard has been created! Additional information: "+information,card.getId());
        }
        return card;
    }


    /**
     * Update a  Flashcard either completely via put or partially via patch.
     * Allows the user to append instead of replace by adding the URL Parameter "?append=(true|false)" which is false by default.
     * with append only two update operations for attributes change:
     * - Tags that would be duplicates are merged by hand, before adding we check if the element is contained and don't
     * add in that case. This is to remove duplicate tags completely. Tags have unique names, which allows checking for
     * duplicates before creating new tags.
     * - Answers can be complete duplicates
     *
     * @return httpResult
     */
    public static FlashCard updateFlashCard(long id,String email, JsonNode json, Map<String,String[]> urlParams) throws InvalidInputException, ParameterNotSupportedException, NullPointerException {
        ObjectMapper mapper = new ObjectMapper();
        boolean appendMode = false;
        int answersSize = -1;
        Question oldQuestion = null;
        List<Answer> oldAnswerList=null;
        User author = User.find.where().eq(JsonKeys.USER_EMAIL,email).findUnique();

        if (urlParams.containsKey(RequestKeys.APPEND)) {
            appendMode = Boolean.parseBoolean(urlParams.get(RequestKeys.APPEND)[0]);
        }
        if (JsonKeys.debugging) Logger.debug("Appending mode enabled? " + appendMode);

        FlashCard toUpdate = FlashCard.find.byId(id);
        User requestOwner=User.find.where().eq(JsonKeys.USER_EMAIL,email).findUnique();

        if(!requestOwner.checkRights(UserOperations.EDIT_CARD,toUpdate))
            throw new IllegalArgumentException("The logged in user does not have the rights to edit this card.");

        if (request().method().equals("PUT") && (!json.has(JsonKeys.FLASHCARD_ANSWERS) || !json.has(JsonKeys.FLASHCARD_QUESTION)
                || !json.has(JsonKeys.AUTHOR) || !json.has(JsonKeys.FLASHCARD_MULTIPLE_CHOICE) || !json.has(JsonKeys.FLASHCARD_TAGS))) {
            if (JsonKeys.debugging)
                Logger.debug(!json.has(JsonKeys.FLASHCARD_ANSWERS) + " " + !json.has(JsonKeys.FLASHCARD_QUESTION)
                        + " " + !json.has(JsonKeys.AUTHOR) + " " + !json.has(JsonKeys.FLASHCARD_MULTIPLE_CHOICE) + " " + json.has(JsonKeys.FLASHCARD_TAGS));
            throw new InvalidInputException(
                    "The Update method needs all details of the card, such as name, " +
                            "description and a user group (array of users or null).");
        }


        if (json.has(JsonKeys.FLASHCARD_ANSWERS)) {
            oldAnswerList=FlashCard.find.byId(id).getAnswers();

            if (appendMode) {
                List<Answer> mergedAnswers = new ArrayList<>();

                mergedAnswers.addAll(toUpdate.getAnswers());
                mergedAnswers.addAll(retrieveAnswers(author,json));

                toUpdate.setAnswers(mergedAnswers);
            } else {
                toUpdate.setAnswers(retrieveAnswers(author,json));
            }
        }

        if (json.has(JsonKeys.FLASHCARD_QUESTION)) {
            if (json.get(JsonKeys.FLASHCARD_QUESTION).has(JsonKeys.QUESTION_ID)) {
                throw new IllegalArgumentException("A questionId is not accepted while creating new cards," +
                        " please provide a complete question object with the following components: "
                        + JsonKeys.QUESTION_JSON_ELEMENTS);
            } else {
                try {
                    Question q = Question.parseQuestion(null,json.get(JsonKeys.FLASHCARD_QUESTION));
                    q.save();
                    oldQuestion = toUpdate.getQuestion();
                    Logger.debug("Deleted oldQuestion: "+oldQuestion);
                    toUpdate.setQuestion(q);



                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }

        if (json.has(JsonKeys.AUTHOR)) {
            User u = mapper.convertValue(json.findValue(JsonKeys.AUTHOR), User.class);
            author = User.find.byId(u.getId());
            toUpdate.setAuthor(author);
        }

        if (json.has(JsonKeys.FLASHCARD_TAGS)) {
            if (appendMode) {
                List<Tag> mergedTags = new ArrayList<>();
                mergedTags.addAll(toUpdate.getTags());
                for (Tag t : TagRepository.retrieveOrCreateTags(json)) {
                    if (!mergedTags.contains(t)) {
                        mergedTags.add(t);
                    }
                }
//                    mergedTags.addAll(JsonUtil.retrieveOrCreateTags(json));
                toUpdate.setTags(mergedTags);
                if (JsonKeys.debugging) Logger.debug("append: " + mergedTags);
            } else {
                toUpdate.setTags(TagRepository.retrieveOrCreateTags(json));
            }
        }

        if (json.has(JsonKeys.FLASHCARD_MULTIPLE_CHOICE)) {
            toUpdate.setMultipleChoice(json.findValue(JsonKeys.FLASHCARD_MULTIPLE_CHOICE).asBoolean());
        }

        toUpdate.update();
        //delete old/replaced objects if no appendmode is enabled
        if(oldQuestion!=null)
            oldQuestion.delete();
        if(oldAnswerList!=null && !appendMode) {
            Logger.debug("oldList=" + oldAnswerList);
            for(Answer answer: oldAnswerList){
                Logger.debug("delete answer with id="+answer.getId());
                answer.delete();
            }
        }

        if (JsonKeys.debugging)
            Logger.debug("updated");

        return toUpdate;
    }

    /**
     * A method that allows us to retrieve the question for a specific card under the URI /cards/:id/question
     *
     * @param id of a card
     * @return httpresult
     */
    public static Question getQuestion(long id)  throws NullPointerException{
        return FlashCard.find.byId(id).getQuestion();
    }

    /**
     * Gets the author of a specific card.
     *
     * @param id of a card
     * @return author of the card including a http result ok OR not found if nothing was found
     */
    public static User getAuthor(long id) throws NullPointerException{
        return FlashCard.find.byId(id).getAuthor();
    }

    /**
     * A method that allows us to retrieve answers for a specific card under the URI /cards/:id/answers
     *
     * @param id of a card
     * @param urlParams
     * @return answers of the card including a http result ok OR not found if nothing was found
     */
    public static List<Answer> getAnswers(long id, Map<String, String[]> urlParams) throws NullPointerException, ObjectNotFoundException {
        int answersSize = -1;
        String sortBy = "";
        if (urlParams.containsKey(RequestKeys.SIZE)) {
            try {
                answersSize = Integer.parseInt(urlParams.get(RequestKeys.SIZE)[0]);
            } catch (NumberFormatException e) {
                //number format equals illegalargument -> bad request
                throw new IllegalArgumentException("Parameter size=" + urlParams.get("size")[0] + " has to be a valid integer.");
            }
        }
        if (urlParams.containsKey(RequestKeys.SORT_BY)) {
            sortBy = urlParams.get(RequestKeys.SORT_BY)[0];
            if (JsonKeys.debugging) Logger.debug("sortBy found=" + sortBy);
        }
        if (JsonKeys.debugging) Logger.debug("answers size=" + answersSize);
        List<Answer> answerList;
        try {
            // TODO: 27/06/16 handle multichoice etc.
            answerList = Answer.find.where().eq(JsonKeys.ANSWER_CARD_ID, id).orderBy(sortBy).setMaxRows(answersSize).findList();


        } catch (Exception e) {
            //convert nullpointer to our exception with an additional overriden constructor to return the id.
            throw new ObjectNotFoundException("Error, no card with the given exists.", id);
        }
        return answerList;
    }

    /**
     * Retreive all Tags or the first n Elements from the Sublist when adding ?size=x to the url, where x must be an integer.
     *
     * @param id of a card
     * @param urlParams
     * @return list of Tags as json to the caller
     */
    public static List<Tag> getTags(long id, Map<String, String[]> urlParams) throws IllegalArgumentException, NullPointerException, ObjectNotFoundException {
        int answersSize = -1;
        if (urlParams.containsKey(RequestKeys.SIZE)) {
            try {
                answersSize = Integer.parseInt(urlParams.get(RequestKeys.SIZE)[0]);
            } catch (NumberFormatException e) {
                //number format equals illegalargument -> bad request
                throw new IllegalArgumentException("Parameter size=" + urlParams.get("size")[0] + " has to be a valid integer.");
            }
        }
        if (JsonKeys.debugging) Logger.debug("tags size=" + answersSize);
        List<Tag> tagList;
        try {
            tagList = FlashCard.find.byId(id).getTags();
            //Return a sublist from 0 to either the size of answers OR the cap we get via parameter.
            if (answersSize > 0)
                tagList = tagList.subList(0, Math.min(answersSize, tagList.size()));

        } catch (Exception e) {
            //convert nullpointer to our exception with an additional overriden constructor to return the id.
            throw new ObjectNotFoundException("Error, no card with the given exists.", id);
        }
        return tagList;
    }


    /**
     * Retrieve all answers, passes the author as argument if the author is responsible for all created answers.
     * If no Author is specified (Null) we will parse the authors from the sent json.
     * @param author
     * @param json
     * @return
     * @throws ParameterNotSupportedException
     */
    private static List<Answer> retrieveAnswers(User author, JsonNode json) throws ParameterNotSupportedException {
        List<Answer> answers = new ArrayList<>();

        //get the specific nods in the json
        JsonNode answersNode = json.findValue(JsonKeys.FLASHCARD_ANSWERS);
        // Loop through all objects in the values associated with the
        // "users" key.
        for (JsonNode node : answersNode) {
            // when a user id is found we will get the object and add them to the userList.
            System.out.println("Node=" + node);
            if (node.has(JsonKeys.ANSWER_ID)) {
                // TODO: 10.09.2016 do we want to support loading existing answers or can it stay this way? e.g. only new answers are allowed for a flashcard.
                throw new ParameterNotSupportedException();
            } else {
                try {
                    Answer tmpA = parseAnswer(node);
                    if(author!=null) {
                        tmpA.setAuthor(author);
                        System.out.println(">> set author!");
                    }
                    System.out.println(">> answer: " + tmpA);
                    answers.add(tmpA);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        return answers;
    }

    /**
     * Parses an answer from the given JsonNode node.
     *
     * @param node the json node to parse
     * @return answer
     * @throws URISyntaxException
     */
    private static Answer parseAnswer(JsonNode node) throws URISyntaxException {
        User author = null;
        String answerText = null;
        String hintText = null;
        if (node.has(JsonKeys.ANSWER_HINT)) {
            hintText = node.get(JsonKeys.ANSWER_HINT).asText();
        }
        if (node.has(JsonKeys.AUTHOR)) {
            if (node.get(JsonKeys.AUTHOR).has(JsonKeys.USER_ID)) {
                long uid = node.get(JsonKeys.AUTHOR).get(JsonKeys.USER_ID).asLong();
                author = User.find.byId(uid);
//                System.out.println("Search for user with id=" + uid + " details=" + author);
            }
        }
        if (node.has(JsonKeys.ANSWER_TEXT)) {
            answerText = node.get(JsonKeys.ANSWER_TEXT).asText();
        }
        Answer answer = new Answer(answerText, hintText, author);

        if (node.has(JsonKeys.URI)) {
            answer.setUri(new URI(node.get(JsonKeys.URI).asText()));
        }
        if (node.has(JsonKeys.RATING)) {
            answer.setRating(node.get(JsonKeys.RATING).asInt());
        }
        if(node.has(JsonKeys.ANSWER_HINT)){
            answer.setHintText(node.get(JsonKeys.ANSWER_HINT).asText());
        }
        if(node.has(JsonKeys.ANSWER_CORRECT)){
            answer.setCorrect(node.get(JsonKeys.ANSWER_CORRECT).asBoolean());
        }
        else{

        }
        return answer;
    }


}
