package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.rating.Rating;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import repositories.RatingRepository;
import util.JsonUtil;
import util.RequestKeys;
import util.exceptions.DuplicateKeyException;
import util.exceptions.InvalidInputException;
import util.exceptions.ObjectNotFoundException;

import java.util.List;
import java.util.Map;


/**
 * @author Jonas Kraus
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
        List<Rating> ratingList= RatingRepository.getRatings(urlParams);
        return ok(JsonUtil.toJson(ratingList));
    }

    /**
     * Retrieves a rating by its id.
     *
     * @param id
     * @return either the card or a notfound with an error status
     */
    public Result getRating(long id) {
        try {
            Rating card = RatingRepository.getRating(id);
            return ok(JsonUtil.toJson(card));
        } catch (ObjectNotFoundException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, e.getMessage(),e.getObjectId()));
        }
    }

    /**
     * Creates a new Rating object for either type (Answer/Cardrating)
     *
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result addRating() {
        try {
            JsonNode json = request().body().asJson();
            Rating rating = RatingRepository.addRating(json);
            return created(JsonUtil.prepareJsonStatus(CREATED, "Rating has been created.",rating.getId()));

        }
        catch (DuplicateKeyException e) {
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST,e.getMessage(),e.getObjectId()));
        } catch (InvalidInputException e) {
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST,e.getMessage()));
        } catch (ObjectNotFoundException e) {
            //Duplicate keys
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST,e.getMessage()));
        }
    }
    @BodyParser.Of(BodyParser.Json.class)
    public Result changeRating(Long id){
        JsonNode json=request().body().asJson();
        try {
            Rating rating=RatingRepository.changeRating(id,json);
            return ok(JsonUtil.prepareJsonStatus(OK, "Rating has been changed.",rating.getId()));

        } catch (Exception e) {
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, e.getMessage()));
        }

    }

    /**
     * Deletes a rating by it's id, compensates the rating of affected users/cards/answers automagically.
     * @param id
     * @return
     */
    public Result deleteRating(Long id) {
        try {
            Rating rating=RatingRepository.deleteRating(id);
            return noContent();
        } catch (ObjectNotFoundException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, e.getMessage(),e.getObjectId()));
        }
    }
}
