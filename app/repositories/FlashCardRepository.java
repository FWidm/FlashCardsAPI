package repositories;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.*;
import play.Logger;
import util.JsonKeys;
import util.RequestKeys;
import util.UserOperations;
import util.Permissions;
import util.exceptions.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static play.mvc.Controller.request;

/**
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
     * @param email of the user that wants to delete
     * @param id    of a card
     * @return deleted card object
     */
    public static FlashCard deleteFlashCard(String email, long id) throws NullPointerException, IllegalArgumentException, NotAuthorizedException {
        User author = User.find.where().eq(JsonKeys.USER_EMAIL, email).findUnique();

        FlashCard card = FlashCard.find.byId(id);
        if (author.hasPermission(UserOperations.DELETE_CARD, card))
            card.delete();
        else
            throw new NotAuthorizedException("This user is not authorized to delete this card.");

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
        requestObject.setTags(new ArrayList<>());
        String information = "";
        User author = User.find.where().eq(JsonKeys.USER_EMAIL, email).findUnique();
        requestObject.setAuthor(author);
        //We expect just id's to set answers/questions/authors - we then check the db for the id's and retrieve all values
        // we nee ourselves.
        if (json.has(JsonKeys.FLASHCARD_ANSWERS)) {
            JsonNode answersNode = json.findValue(JsonKeys.FLASHCARD_ANSWERS);
            if (JsonKeys.debugging) Logger.debug("answersNode=" + answersNode);
            for (JsonNode node : answersNode) {
                if (node.has(JsonKeys.ANSWER_ID)) {
                    throw new InvalidInputException("An answerId is not accepted while creating new cards, " +
                            "please provide a complete answer object with the following components: "
                            + JsonKeys.ANSWER_JSON_ELEMENTS);
                }
            }
            requestObject.setAnswers(retrieveAnswers(author, json));
        }

        if (json.has(JsonKeys.FLASHCARD_QUESTION)) {

            if (json.get(JsonKeys.FLASHCARD_QUESTION).has(JsonKeys.QUESTION_ID)) {
                throw new InvalidInputException("A questionId is not accepted while creating " +
                        "new cards, please provide a complete question object with the following components: " + JsonKeys.QUESTION_JSON_ELEMENTS);
            } else {
                try {
                    Question q = Question.parseQuestion(author, json.get(JsonKeys.FLASHCARD_QUESTION));
                    q.save();
                    requestObject.setQuestion(q);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        List<Tag> tags = TagRepository.retrieveOrCreateTags(json);

        if (json.has(JsonKeys.FLASHCARD_TAGS)) {

            // TODO: 07.01.2017 revisit this process
            if (tags.contains(null)) {
                if (JsonKeys.debugging) Logger.debug(">> null!");
                information += " One or more tag ids where invalid!";
                tags.remove(null);
            }
        }

        if (json.has(JsonKeys.FLASHCARD_MULTIPLE_CHOICE)) {
            requestObject.setMultipleChoice(json.findValue(JsonKeys.FLASHCARD_MULTIPLE_CHOICE).asBoolean());
        }
        Logger.debug("tags="+tags);
        FlashCard card = new FlashCard(requestObject);
        if (JsonKeys.debugging) Logger.debug("Tags=" + card.getTags().size());
        //Logger.debug(""+card);
        card.save();
        card.setTags(tags);
        card.update();
        if (information != "") {
            throw new PartiallyModifiedException("FlashCard has been created! Additional information: " + information, card.getId());
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
    public static FlashCard updateFlashCard(long id, String email, JsonNode json, Map<String, String[]> urlParams) throws InvalidInputException, ParameterNotSupportedException, NullPointerException, NotAuthorizedException {
        ObjectMapper mapper = new ObjectMapper();
        boolean appendMode = false;

        Question oldQuestion = null;
        List<Answer> oldAnswerList = null;
        User author = User.find.where().eq(JsonKeys.USER_EMAIL, email).findUnique();
        FlashCard toUpdate = FlashCard.find.byId(id);
        boolean hasPermission=author.hasPermission(UserOperations.EDIT_CARD, toUpdate);

        //When using put we need to be able to edit everything inside our card.
        if (request().method().equals("PUT") && !hasPermission)
            throw new NotAuthorizedException("This user is not authorized to edit this card.");

        if (urlParams.containsKey(RequestKeys.APPEND)) {
            appendMode = Boolean.parseBoolean(urlParams.get(RequestKeys.APPEND)[0]);
        }
        if (JsonKeys.debugging) Logger.debug("Appending mode enabled? " + appendMode);

        User requestOwner = User.find.where().eq(JsonKeys.USER_EMAIL, email).findUnique();


        if (request().method().equals("PUT") && (!json.has(JsonKeys.FLASHCARD_ANSWERS) || !json.has(JsonKeys.FLASHCARD_QUESTION)
                || !json.has(JsonKeys.AUTHOR) || !json.has(JsonKeys.FLASHCARD_MULTIPLE_CHOICE) || !json.has(JsonKeys.FLASHCARD_TAGS))) {
            if (JsonKeys.debugging)
                Logger.debug(!json.has(JsonKeys.FLASHCARD_ANSWERS) + " " + !json.has(JsonKeys.FLASHCARD_QUESTION)
                        + " " + !json.has(JsonKeys.AUTHOR) + " " + !json.has(JsonKeys.FLASHCARD_MULTIPLE_CHOICE) + " " + json.has(JsonKeys.FLASHCARD_TAGS));
            throw new IllegalArgumentException(
                    "The Update method needs all details of the card, such as name, " +
                            "description and a user group (array of users or null).");
        }


        if (json.has(JsonKeys.FLASHCARD_ANSWERS)) {
            oldAnswerList = FlashCard.find.byId(id).getAnswers();
            oldAnswerList.forEach(a -> Logger.debug("old: " + a));
            if (appendMode) {
                List<Answer> mergedAnswers = new ArrayList<>();

                mergedAnswers.addAll(toUpdate.getAnswers());
                mergedAnswers.addAll(retrieveAnswers(author, json));

                toUpdate.setAnswers(mergedAnswers);
            } else if (hasPermission) {
                List<Answer> newAnswers = retrieveAnswers(author, json);
                newAnswers.forEach(a -> Logger.debug("new: " + a));

                toUpdate.setAnswers(newAnswers);
            } else
                throw new NotAuthorizedException("This user is not authorized to edit this card. " +
                        "You cannot replace the answers without having the a rating above " +
                        Permissions.RATING_EDIT_CARD + " points or being the owner of the card. " +
                        "Please append new tags with '?append=true'");


        }


        if (json.has(JsonKeys.FLASHCARD_QUESTION)) {
            if (hasPermission) {
                if (json.get(JsonKeys.FLASHCARD_QUESTION).has(JsonKeys.QUESTION_ID)) {
                    throw new IllegalArgumentException("A questionId is not accepted while creating new cards," +
                            " please provide a complete question object with the following components: "
                            + JsonKeys.QUESTION_JSON_ELEMENTS);
                } else {
                    try {
                        Question q = Question.parseQuestion(author, json.get(JsonKeys.FLASHCARD_QUESTION));
                        q.save();
                        oldQuestion = toUpdate.getQuestion();
                        Logger.debug("Deleted oldQuestion: " + oldQuestion);
                        toUpdate.setQuestion(q);


                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            } else
                throw new NotAuthorizedException("This user is not authorized to edit this card. You cannot modify the" +
                        " question without having the a rating above " + Permissions.RATING_EDIT_CARD + " points " +
                        "or being the owner of the card.");
        }

        if (json.has(JsonKeys.AUTHOR)) {
            if (hasPermission) {
                User u = mapper.convertValue(json.findValue(JsonKeys.AUTHOR), User.class);
                author = User.find.byId(u.getId());
                toUpdate.setAuthor(author);
            } else
                throw new NotAuthorizedException("This user is not authorized to edit this card. You cannot modify the " +
                        "author without having a rating above " + Permissions.RATING_EDIT_CARD +
                        " points or being the owner of the card.");
        } else if (json.has(JsonKeys.FLASHCARD_TAGS)) {
            if (appendMode) {
                Logger.debug("Appending...:");
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
            } else if (hasPermission) {
                Logger.debug("User is the author. He can put.");
                toUpdate.setTags(TagRepository.retrieveOrCreateTags(json));

            } else
                throw new NotAuthorizedException("This user is not authorized to edit this card. You cannot replace the " +
                        "tags without having a rating above " + Permissions.RATING_EDIT_CARD + " points. " +
                        "Please append new tags with '?append=true'");
        }

        if (json.has(JsonKeys.FLASHCARD_MULTIPLE_CHOICE)) {
            if (hasPermission) {
                toUpdate.setMultipleChoice(json.findValue(JsonKeys.FLASHCARD_MULTIPLE_CHOICE).asBoolean());
            } else
                throw new NotAuthorizedException("This user is not authorized to edit this card. You cannot modify the " +
                        "multiple choice status without having the a rating above " + Permissions.RATING_EDIT_CARD +
                        " points or being the owner of the card.");
        }

        toUpdate.update();
        //delete old/replaced objects if no appendmode is enabled
        if (oldQuestion != null)
            oldQuestion.delete();


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
    public static Question getQuestion(long id) throws NullPointerException {
        return FlashCard.find.byId(id).getQuestion();
    }

    /**
     * Gets the author of a specific card.
     *
     * @param id of a card
     * @return author of the card including a http result ok OR not found if nothing was found
     */
    public static User getAuthor(long id) throws NullPointerException {
        return FlashCard.find.byId(id).getAuthor();
    }

    /**
     * A method that allows us to retrieve answers for a specific card under the URI /cards/:id/answers
     *
     * @param id        of a card
     * @param urlParams request parameters
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
     * @param id        of a card
     * @param urlParams request params
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
     *
     * @param author author of the answer
     * @param json   request body
     * @return list of answers
     * @throws ParameterNotSupportedException if answer ids are provided, throw an error as this should not be possible.
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
                throw new ParameterNotSupportedException();
            } else {
                try {
                    Answer tmpA = parseAnswer(node);
                    if (author != null) {
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
        Logger.debug("Found " + answers.size() + " new answers!");
        return answers;
    }

    /**
     * Parses an answer from the given JsonNode node.
     *
     * @param node the json node to parse
     * @return answer
     * @throws URISyntaxException if the uri is malformed
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
        if (node.has(JsonKeys.ANSWER_HINT)) {
            answer.setHintText(node.get(JsonKeys.ANSWER_HINT).asText());
        }
        if (node.has(JsonKeys.ANSWER_CORRECT)) {
            answer.setCorrect(node.get(JsonKeys.ANSWER_CORRECT).asBoolean());
        } else {

        }
        return answer;
    }


}
