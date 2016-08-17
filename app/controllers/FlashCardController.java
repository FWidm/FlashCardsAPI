package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.*;
import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import util.JsonKeys;
import util.JsonUtil;
import util.exceptions.ParameterNotSupportedException;
import util.RequestKeys;

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
 *         <p>
 *         This class handles all operations for Flashcards.
 *         Used routes are:
 *         ip:9000/cards                       - GET; POST
 *         /cards/:id                   - GET; PUT; PATCH
 *         /cards/:id/answers?size=x    - GET
 *         /cards/:id/question          - GET
 *         /cards/:id/author            - GET
 */
public class FlashCardController {
    /**
     * Retrieves all Flashcards.
     *
     * @return HTTPResult
     */
    public Result getFlashCardList() {
        List<FlashCard> flashCardList = FlashCard.find.all();
        return ok(JsonUtil.getJson(flashCardList));
    }

    /**
     * Retrieves everything from a flashcard with the given id.
     *
     * @param id of a card
     * @return HTTPResult
     */
    public Result getFlashCard(long id) {
        try {
            FlashCard card = FlashCard.find.byId(id);
            return ok(JsonUtil.getJson(card));
        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no card with id=" + id + " exists."));
        }
    }

    /**
     * Deletes the specific Flashcard including questions and answers.
     *
     * @param id of a card
     * @return HTTPResult
     */
    public Result deleteFlashCard(long id) {
        try {
            FlashCard.find.byId(id).delete();

            return ok(JsonUtil.prepareJsonStatus(OK, "The card with the id=" + id
                    + " has been deleted. This includes questions and answers. All Tags for this card were disconnected and persist."));
        } catch (NullPointerException e) {
            System.err.println(e.getMessage());
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no card with id=" + id + " exists."));
        }
    }

    /**
     * Adds a new Flashcard, expects a question (if an id is specified, we load it from the db, else we create a new one),
     * answers (if id is given --> DB, else create new), author (must specify id), isMultiplechoice flag.
     *
     * @return HTTPResult
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result addFlashCard() {
        try {
            JsonNode json = request().body().asJson();
            ObjectMapper mapper = new ObjectMapper();
            FlashCard requestObject = mapper.convertValue(json, FlashCard.class);
            String information = "";
            //We expect just id's to set answers/questions/authors - we then check the db for the id's and retrieve all values
            // we nee ourselves.
            if (json.has(JsonKeys.FLASHCARD_ANSWERS)) {
                // TODO: 10.08.2016 rewrite this part, it is ugly

                JsonNode answersNode = json.findValue(JsonKeys.FLASHCARD_ANSWERS);
                if(JsonKeys.debugging)Logger.debug("answersNode=" + answersNode);
                for (JsonNode node : answersNode) {
                    if (node.has(JsonKeys.ANSWER_ID)) {
                        return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "An answerId is not accepted while creating new cards, please provide a complete answer object with the following components: " + JsonKeys.ANSWER_JSON_ELEMENTS));
                    }
                }
                requestObject.setAnswers(JsonUtil.retrieveAnswers(json));
            }

            if (json.has(JsonKeys.FLASHCARD_QUESTION)) {

                if (json.get(JsonKeys.FLASHCARD_QUESTION).has(JsonKeys.QUESTION_ID)) {
                    return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "A questionId is not accepted while creating " +
                            "new cards, please provide a complete question object with the following components: " + JsonKeys.QUESTION_JSON_ELEMENTS));
                } else {
                    try {
                        if(JsonKeys.debugging)Logger.debug("HELLO!");
                        Question q = Question.parseQuestion(json.get(JsonKeys.FLASHCARD_QUESTION));
                        q.save();
                        requestObject.setQuestion(q);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (json.has(JsonKeys.AUTHOR)) {
                User author = User.find.byId(json.get(JsonKeys.AUTHOR).get(JsonKeys.USER_ID).asLong());
                requestObject.setAuthor(author);
            }

            if (json.has(JsonKeys.FLASHCARD_TAGS)) {

                List<Tag> tags = JsonUtil.retrieveTags(json);
                if (tags.contains(null)) {
                    if(JsonKeys.debugging)Logger.debug(">> null!");
                    information += " One or more tag ids where invalid!";
                    tags.remove(null);
                }
                requestObject.setTags(tags);

            }

            if (json.has(JsonKeys.FLASHCARD_MULTIPLE_CHOICE)) {
                requestObject.setMultipleChoice(json.findValue(JsonKeys.FLASHCARD_MULTIPLE_CHOICE).asBoolean());
            }
            FlashCard card = new FlashCard(requestObject);
            if(JsonKeys.debugging)Logger.debug("Tags=" + card.getTags().size());
            card.save();

            return ok(JsonUtil.prepareJsonStatus(OK, "FlashCard with id=" + card.getId() + " has been created!" + information, card.getId()));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return badRequest(JsonUtil
                    .prepareJsonStatus(
                            BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS));
        } catch (ParameterNotSupportedException e) {
            e.printStackTrace();
            return badRequest(JsonUtil
                    .prepareJsonStatus(
                            BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS));
        }
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
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateFlashCard(long id) {
        JsonNode json = request().body().asJson();
        ObjectMapper mapper = new ObjectMapper();
        boolean appendMode = false;
        Map<String, String[]> urlParams = Controller.request().queryString();
        int answersSize = -1;

        if (urlParams.containsKey(RequestKeys.APPEND)) {
            appendMode = Boolean.parseBoolean(urlParams.get(RequestKeys.APPEND)[0]);
        }
        if(JsonKeys.debugging)Logger.debug("Appending mode enabled? " + appendMode);
        try {
            FlashCard toUpdate = FlashCard.find.byId(id);

            if (request().method().equals("PUT") && (!json.has(JsonKeys.FLASHCARD_ANSWERS) || !json.has(JsonKeys.FLASHCARD_QUESTION)
                    || !json.has(JsonKeys.AUTHOR) || !json.has(JsonKeys.FLASHCARD_MULTIPLE_CHOICE) || !json.has(JsonKeys.FLASHCARD_TAGS))) {
                if(JsonKeys.debugging)Logger.debug(!json.has(JsonKeys.FLASHCARD_ANSWERS) + " " + !json.has(JsonKeys.FLASHCARD_QUESTION)
                        + " " + !json.has(JsonKeys.AUTHOR) + " " + !json.has(JsonKeys.FLASHCARD_MULTIPLE_CHOICE) + " " + json.has(JsonKeys.FLASHCARD_TAGS));
                return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST,
                        "The Update method needs all details of the card, such as name, " +
                                "description and a user group (array of users or null).",id));
            }


            if (json.has(JsonKeys.FLASHCARD_ANSWERS)) {
                if (appendMode) {
                    List<Answer> mergedAnswers = new ArrayList<>();
                    mergedAnswers.addAll(toUpdate.getAnswers());
                    mergedAnswers.addAll(JsonUtil.retrieveAnswers(json));
                    toUpdate.setAnswers(mergedAnswers);
                } else {
                    toUpdate.setAnswers(JsonUtil.retrieveAnswers(json));
                }
            }

            if (json.has(JsonKeys.FLASHCARD_QUESTION)) {
                if (json.get(JsonKeys.FLASHCARD_QUESTION).has(JsonKeys.QUESTION_ID)) {
                    return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "A questionId is not accepted while creating new cards," +
                            " please provide a complete question object with the following components: " + JsonKeys.QUESTION_JSON_ELEMENTS));
                } else {
                    try {
                        Question q = Question.parseQuestion(json.get(JsonKeys.FLASHCARD_QUESTION));
                        q.save();
                        toUpdate.setQuestion(q);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (json.has(JsonKeys.AUTHOR)) {
                User u = mapper.convertValue(json.findValue(JsonKeys.AUTHOR), User.class);
                User author = User.find.byId(u.getId());
                toUpdate.setAuthor(author);
            }

            if (json.has(JsonKeys.FLASHCARD_TAGS)) {
                if (appendMode) {
                    List<Tag> mergedTags = new ArrayList<>();
                    mergedTags.addAll(toUpdate.getTags());
                    for (Tag t : JsonUtil.retrieveTags(json)) {
                        if (!mergedTags.contains(t)) {
                            mergedTags.add(t);
                        }
                    }
//                    mergedTags.addAll(JsonUtil.retrieveTags(json));
                    toUpdate.setTags(mergedTags);
                    if(JsonKeys.debugging)Logger.debug("append: " + mergedTags);
                } else {
                    toUpdate.setTags(JsonUtil.retrieveTags(json));
                }
            }

            if (json.has(JsonKeys.FLASHCARD_MULTIPLE_CHOICE)) {
                toUpdate.setMultipleChoice(json.findValue(JsonKeys.FLASHCARD_MULTIPLE_CHOICE).asBoolean());
            }

            toUpdate.update();
            if(JsonKeys.debugging)Logger.debug("updated");

            return ok(JsonUtil.prepareJsonStatus(OK, "FlashCard with id=" + id + " has been updated!"));
        } catch (NullPointerException e) {
            e.printStackTrace();
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no card with id=" + id + " exists."));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return badRequest(JsonUtil
                    .prepareJsonStatus(
                            BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS));
        } catch (ParameterNotSupportedException e) {
            e.printStackTrace();
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST,"An answerId is not accepted while creating new cards, " +
                    "please provide a complete list of answers object with the following components: "+JsonKeys.QUESTION_JSON_ELEMENTS));

        }

    }

    /**
     * A method that allows us to retrieve the question for a specific card under the URI /cards/:id/question
     *
     * @param id of a card
     * @return httpresult
     */
    public Result getQuestion(long id) {
        Question ret;
        try {
            ret = FlashCard.find.byId(id).getQuestion();
        } catch (Exception e) {
            System.err.println(e);
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no card with id=" + id + " exists."));
        }
        return ok(JsonUtil.getJson(ret));
    }

    /**
     * Gets the author of a specific card.
     *
     * @param id of a card
     * @return author of the card including a http result ok OR not found if nothing was found
     */
    public Result getAuthor(long id) {
        User ret;
        try {
            ret = FlashCard.find.byId(id).getAuthor();
        } catch (Exception e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no card with id=" + id + " exists."));
        }
        return ok(JsonUtil.getJson(ret));
    }

    /**
     * A method that allows us to retrieve answers for a specific card under the URI /cards/:id/answers
     *
     * @param id of a card
     * @return answers of the card including a http result ok OR not found if nothing was found
     */
    public Result getAnswers(long id) {
        Map<String, String[]> urlParams = Controller.request().queryString();
        int answersSize = -1;
        String sortBy="";
        if (urlParams.containsKey(RequestKeys.SIZE)) {
            try {
                answersSize = Integer.parseInt(urlParams.get(RequestKeys.SIZE)[0]);
            } catch (NumberFormatException e) {
                return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST,
                        "Parameter size=" + urlParams.get("size")[0] + " could not be parsed to integer."));
            }
        }
        if (urlParams.containsKey(RequestKeys.SORT_BY)) {
                sortBy = urlParams.get(RequestKeys.SORT_BY)[0];
                if(JsonKeys.debugging)Logger.debug("sortBy found="+sortBy);
        }
        if(JsonKeys.debugging)Logger.debug("answers size=" + answersSize);
        List<Answer> ret;
        try {
            // TODO: 27/06/16 handle multichoice etc.
            ret = Answer.find.where().eq(JsonKeys.ANSWER_CARD_ID,id).orderBy(sortBy).setMaxRows(answersSize).findList();


        } catch (Exception e) {
            System.err.println(e);
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no card with id=" + id + " exists."));
        }
        return ok(JsonUtil.getJson(ret));
    }

    /**
     * Retreive all Tags or the first n Elements from the Sublist when adding ?size=x to the url, where x must be an integer.
     *
     * @param id of a card
     * @return list of Tags as json to the caller
     */
    public Result getTags(long id) {
        Map<String, String[]> urlParams = Controller.request().queryString();
        int answersSize = -1;
        if (urlParams.containsKey(RequestKeys.SIZE)) {
            try {
                answersSize = Integer.parseInt(urlParams.get(RequestKeys.SIZE)[0]);
            } catch (NumberFormatException e) {
                System.err.println(e);
                return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST,
                        "Parameter size=" + urlParams.get("size")[0] + " has to be a valid integer."));
            }
        }
        if(JsonKeys.debugging) Logger.debug("tags size=" + answersSize);
        List<Tag> ret;
        try {
            ret = FlashCard.find.byId(id).getTags();
            //Return a sublist from 0 to either the size of answers OR the cap we get via parameter.
            if (answersSize > 0)
                ret = ret.subList(0, Math.min(answersSize, ret.size()));

        } catch (Exception e) {
            e.printStackTrace();
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no card with id=" + id + " exists."));
        }
        return ok(JsonUtil.getJson(ret));
    }


}
