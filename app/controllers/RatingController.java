package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.rating.Rating;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import repositories.RatingRepository;
import util.ActionAuthenticator;
import util.JsonKeys;
import util.JsonUtil;
import util.RequestKeys;
import util.exceptions.DuplicateKeyException;
import util.exceptions.InvalidInputException;
import util.exceptions.NotAuthorizedException;
import util.exceptions.ObjectNotFoundException;

import java.util.List;
import java.util.Map;


/**
 * @author Fabian Widmann
 */
public class RatingController extends Controller {
    /**
     * Returns a list of all Ratings per default. When needed, the caller can pre-filter the list via:
     * {@link RequestKeys#FLASHCARD_RATING}, {@link RequestKeys#ANSWER_RATING}, {@link RequestKeys#ANSWER_ID},
     * {@link RequestKeys#FLASHCARD_ID}, {@link RequestKeys#USER_ID}.
     *
     * @return a (filtered) list of Ratings
     */
    public Result getRatingList() {
        Map<String, String[]> urlParams = Controller.request().queryString();
        List<Rating> ratingList = RatingRepository.getRatings(urlParams);
        return ok(JsonUtil.toJson(ratingList));
    }

    /**
     * Retrieves a rating by its id.
     *
     * @param id of our rating
     * @return either the card or a notfound with an error status
     */
    public Result getRating(long id) {
        try {
            Rating card = RatingRepository.getRating(id);
            return ok(JsonUtil.toJson(card));
        } catch (ObjectNotFoundException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, e.getMessage(), e.getObjectId()));
        }
    }

    /**
     * Creates a new Rating object for either type (Answer/Cardrating)
     *
     * @return created/badRequest
     */
    @Security.Authenticated(ActionAuthenticator.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result addRating() {
        try {
            JsonNode json = request().body().asJson();
            Rating rating = RatingRepository.addRating(json);
            return created(JsonUtil.prepareJsonStatus(CREATED, "Rating has been created.", rating.getId()));

        } catch (DuplicateKeyException e) {
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, e.getMessage(), e.getObjectId()));
        } catch (InvalidInputException e) {
            e.printStackTrace();
            if (JsonKeys.debugging) {
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS + " | cause: " + e.getCause()));
            } else {
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS));
            }
        } catch (ObjectNotFoundException e) {
            //Duplicate keys
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * Update ratings.
     *
     * @param id of a rating
     * @return ok/badRequest/unauthorized
     */
    @Security.Authenticated(ActionAuthenticator.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result changeRating(Long id) {
        JsonNode json = request().body().asJson();
        try {
            Rating rating = RatingRepository.changeRating(id, request().username(), json);
            return ok(JsonUtil.prepareJsonStatus(OK, "Rating has been changed.", rating.getId()));
        } catch (InvalidInputException e) {
            e.printStackTrace();
            if (JsonKeys.debugging && !e.getMessage().contains("did contain")) {
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS + " | cause: " + e.getCause()));
            } else {
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS));
            }
        } catch (Exception e) {
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, e.getMessage()));
        } catch (NotAuthorizedException e) {
            return unauthorized(JsonUtil.prepareJsonStatus(UNAUTHORIZED, e.getMessage(), id));
        }
    }

    /**
     * Deletes a rating by it's id, compensates the rating of affected users/cards/answers automagically.
     *
     * @param id of a rating
     * @return noContent if successful, notFound if not found
     */
    public Result deleteRating(Long id) {
        try {
            Rating rating = RatingRepository.deleteRating(id);
            return noContent();
        } catch (ObjectNotFoundException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, e.getMessage(), e.getObjectId()));
        }
    }
}
