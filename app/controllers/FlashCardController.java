package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.*;
import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import repositories.FlashCardRepository;
import util.JsonKeys;
import util.JsonUtil;
import util.exceptions.InvalidInputException;
import util.exceptions.ObjectNotFoundException;
import util.exceptions.ParameterNotSupportedException;
import util.RequestKeys;
import util.exceptions.PartiallyModifiedException;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static play.mvc.Controller.request;
import static play.mvc.Http.Status.*;
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
        return ok(JsonUtil.toJson(FlashCardRepository.getFlashCardList()));
    }

    /**
     * Retrieves everything from a flashcard with the given id.
     *
     * @param id of a card
     * @return HTTPResult
     */
    public Result getFlashCard(long id) {
        try {
            return ok(JsonUtil.toJson(FlashCardRepository.getFlashCard(id)));
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
            FlashCard deleted = FlashCardRepository.deleteFlashCard(id);

            return ok(JsonUtil.prepareJsonStatus(OK, "The card with the id=" + deleted.getId()
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
        JsonNode json = request().body().asJson();
        try {
            FlashCard addedCard = FlashCardRepository.addFlashCard(json);
            return created(JsonUtil.prepareJsonStatus(CREATED, "FlashCard has been created!", addedCard.getId()));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            if(JsonKeys.debugging){
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS+" | cause: "+e.getCause()));
            }
            else {
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS));
            }

        } catch (ParameterNotSupportedException e) {
            e.printStackTrace();
            if(JsonKeys.debugging){
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS+" | cause: "+e.getCause()));
            }
            else {
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS));
            }
        } catch (InvalidInputException e) {
            e.printStackTrace();
            return badRequest(JsonUtil
                    .prepareJsonStatus(BAD_REQUEST, e.getMessage()));
        } catch (PartiallyModifiedException e) {
            return created(JsonUtil
                    .prepareJsonStatus(CREATED, e.getMessage(),e.getObjectId()));
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
        Map<String, String[]> urlParams = Controller.request().queryString();

        try {
            FlashCard updatedCard = FlashCardRepository.updateFlashCard(id,json,urlParams);
            return ok(JsonUtil.prepareJsonStatus(OK, "FlashCard has been updated!",updatedCard.getId()));
        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no card with id=" + id + " exists."));
        } catch (IllegalArgumentException e){
            if(JsonKeys.debugging){
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS+" | cause: "+e.getCause()));
            }
            else {
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS));
            }
        } catch (ParameterNotSupportedException e) {
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "An answerId is not accepted while creating new cards, " +
                    "please provide a complete list of answers object with the following components: " + JsonKeys.QUESTION_JSON_ELEMENTS));
        } catch (InvalidInputException e) {
            e.printStackTrace();
            if(JsonKeys.debugging && !e.getCause().getMessage().contains("Body did contain")){
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS+" | cause: "+e.getCause()));
            }
            else {
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS));
            }
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
            ret = FlashCardRepository.getQuestion(id);
        } catch (Exception e) {
            System.err.println(e);
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no card with id=" + id + " exists."));
        }
        return ok(JsonUtil.toJson(ret));
    }

    /**
     * Gets the author of a specific card.
     *
     * @param id of a card
     * @return author of the card including a http result ok OR not found if nothing was found
     */
    public Result getAuthor(long id) {
        User ret;

        ret = FlashCardRepository.getAuthor(id);
        if (ret == null) {
            Logger.debug("Getting author=" + ret);
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no card with id=" + id + " exists."));

        }

        return ok(JsonUtil.toJson(ret));
    }

    /**
     * A method that allows us to retrieve answers for a specific card under the URI /cards/:id/answers
     *
     * @param id of a card
     * @return answers of the card including a http result ok OR not found if nothing was found
     */
    public Result getAnswers(long id) {
        Map<String, String[]> urlParams = Controller.request().queryString();
        List<Answer> ret;
        try {
                ret = FlashCardRepository.getAnswers(id,urlParams);

        } catch (Exception e) {
            System.err.println(e);
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no card with id=" + id + " exists."));
        }
        return ok(JsonUtil.toJson(ret));
    }

    /**
     * Retreive all Tags or the first n Elements from the Sublist when adding ?size=x to the url, where x must be an integer.
     *
     * @param id of a card
     * @return list of Tags as json to the caller
     */
    public Result getTags(long id) {
        Map<String, String[]> urlParams = Controller.request().queryString();

        List<Tag> ret;
        try {
            ret = FlashCardRepository.getTags(id,urlParams);
        } catch (Exception e) {
            e.printStackTrace();
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no card with id=" + id + " exists."));
        }
        return ok(JsonUtil.toJson(ret));
    }


}
